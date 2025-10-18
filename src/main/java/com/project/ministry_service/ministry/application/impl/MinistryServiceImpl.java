package com.project.ministry_service.ministry.application.impl;

import com.project.ministry_service.client.MemberServiceFeignClient;
import com.project.ministry_service.common.enums.RoleName;
import com.project.ministry_service.common.util.MinistryMapper;
import com.project.ministry_service.ministry.api.dto.CreateMinistryRequest;
import com.project.ministry_service.ministry.api.dto.MemberAssignmentDto;
import com.project.ministry_service.ministry.api.dto.MemberDto;
import com.project.ministry_service.ministry.api.dto.MinistryDto;
import com.project.ministry_service.ministry.application.HierarchyJdbcService;
import com.project.ministry_service.ministry.application.MinistryService;
import com.project.ministry_service.ministry.domain.model.Ministry;
import com.project.ministry_service.ministry.domain.model.MinistryMember;
import com.project.ministry_service.ministry.domain.repository.MinistryMemberRepository;
import com.project.ministry_service.ministry.domain.repository.MinistryRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MinistryServiceImpl implements MinistryService {

    private final MinistryRepository ministryRepository;
    private final MinistryMemberRepository ministryMemberRepository;
    private final MemberServiceFeignClient memberServiceFeignClient;
    private final HierarchyJdbcService hierarchyJdbcService;
    private final MinistryMapper ministryMapper;

    private final Map<RoleName, Integer> rolePriority = Map.of(
            RoleName.PRESIDENT, 1,
            RoleName.VICE_PRESIDENT, 2,
            RoleName.TEACHER, 3,
            RoleName.SECRETARY, 4,
            RoleName.TREASURER, 5,
            RoleName.STUDENT, 6,
            RoleName.MEMBER, 7
    );

    public MinistryServiceImpl(MinistryRepository ministryRepository,
                               MinistryMemberRepository ministryMemberRepository,
                               MemberServiceFeignClient memberServiceFeignClient,
                               HierarchyJdbcService hierarchyJdbcService,
                               MinistryMapper ministryMapper) {
        this.ministryRepository = ministryRepository;
        this.ministryMemberRepository = ministryMemberRepository;
        this.memberServiceFeignClient = memberServiceFeignClient;
        this.hierarchyJdbcService = hierarchyJdbcService;
        this.ministryMapper = ministryMapper;
    }

    @Override
    @Transactional
    public Ministry createMinistry(CreateMinistryRequest req) {
        ministryRepository.findByName(req.getName()).ifPresent(m -> {
            throw new IllegalArgumentException("Ministry name exists");
        });

        Ministry m = new Ministry();
        m.setName(req.getName());
        m.setType(req.getType());
        m.setEstablishedDate(req.getEstablishedDate());
        m.setParentId(req.getParentId());
        m.setTermStart(req.getTermStart());
        m.setTermEnd(req.getTermEnd());
        m.setCriteria(req.getCriteria());
        m = ministryRepository.save(m);

        // maintain hierarchy rows
        hierarchyJdbcService.insertSelfAndParentAncestors(m.getId(), m.getParentId());

        // persist any provided assignments (membersToAssign)
        persistAssignments(m.getId(), req.getMembersToAssign());

        return m;
    }

    @Override
    @Transactional
    public Ministry updateMinistry(UUID id, CreateMinistryRequest req) {
        Ministry m = ministryRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Ministry not found"));

        // criteria not editable per requirement – only update non-criteria fields
        m.setName(req.getName());
        m.setType(req.getType());
        m.setEstablishedDate(req.getEstablishedDate());
        UUID oldParent = m.getParentId();
        m.setParentId(req.getParentId());
        m.setTermStart(req.getTermStart());
        m.setTermEnd(req.getTermEnd());
        m = ministryRepository.save(m);

        if (!Objects.equals(oldParent, req.getParentId())) {
            // move subtree under new parent efficiently
            hierarchyJdbcService.moveSubtree(m.getId(), req.getParentId());
        }

        // persist any provided assignments (merge semantics)
        persistAssignments(m.getId(), req.getMembersToAssign());

        return m;
    }

    public List<MinistryDto> getMinistries() {
        return ministryMapper.toDtoList(ministryRepository.findAll());
    }

    private void persistAssignments(UUID ministryId, List<MemberAssignmentDto> assignments) {
        if (assignments == null || assignments.isEmpty()) return;

        // merge semantics: update existing or insert new
        Set<String> incoming = assignments.stream().map(MemberAssignmentDto::getMemberId).collect(Collectors.toSet());
        List<MinistryMember> existing = ministryMemberRepository.findAllByMinistryId(ministryId);

        Map<String, MinistryMember> existingMap = existing.stream().collect(Collectors.toMap(MinistryMember::getMemberId, mm -> mm));

        for (MemberAssignmentDto a : assignments) {
            String memberId = a.getMemberId();
            MinistryMember mm = existingMap.get(memberId);
            if (mm != null) {
                mm.setRole(a.getRole());
                mm.setCommittee(a.isCommittee());
                ministryMemberRepository.save(mm);
            } else {
                MinistryMember newMm = new MinistryMember();
                newMm.setMinistryId(ministryId);
                newMm.setMemberId(memberId);
                newMm.setRole(a.getRole() != null ? a.getRole() : RoleName.MEMBER);
                newMm.setCommittee(a.isCommittee());
                ministryMemberRepository.save(newMm);
            }
        }
    }

    @Override
    @CircuitBreaker(name = "memberServiceClient", fallbackMethod = "potentialMembersFallback")
    public List<MemberDto> getPotentialMembers(String age, String gender, String maritalStatus) {
        List<Map<String, Object>> res = memberServiceFeignClient.getAllMembers(age, gender, maritalStatus);
        // convert to MemberDto (minimal mapping)
        List<MemberDto> list = new ArrayList<>();
        for (Map<String, Object> mobj : res) {
            MemberDto p = new MemberDto();
            p.setId(Objects.toString(mobj.get("id"), null));
            p.setPhotoUrl(Objects.toString(mobj.get("pictureUrl"), null));
            Map<String, Object> personalInfo = (Map<String, Object>) mobj.get("personalInfo");
            p.setFirstName(personalInfo != null ? Objects.toString(personalInfo.get("firstName"), null) : null);
            p.setLastName(personalInfo != null ? Objects.toString(personalInfo.get("lastName"), null) : null);
            p.setName(p.getFirstName().concat(" ").concat(p.getLastName()));
            LocalDate birthDate = (personalInfo != null ? LocalDate.parse(Objects.toString(personalInfo.get("birthdate"), null)) : null);
            if (birthDate != null) p.setAge(Period.between(birthDate, LocalDate.now()).getYears());
            p.setGender(personalInfo != null ? Objects.toString(personalInfo.get("gender"), null) : null);
            p.setMaritalStatus(personalInfo != null ? Objects.toString(personalInfo.get("maritalStatus"), null) : null);
            Map<String, Object> contact = (Map<String, Object>) mobj.get("contact");
            p.setPhone(contact != null ? Objects.toString(contact.get("mobilePhoneNumber"), null) : null);
            list.add(p);
        }
        return list;
    }

    private List<MemberDto> potentialMembersFallback(Throwable e) {
        // This is called automatically if the Feign client fails
        return Collections.emptyList();
    }

    @Override
    @CircuitBreaker(name = "memberServiceClient", fallbackMethod = "potentialMembersFallback")
    public List<MemberDto> getPotentialMembers(UUID ministryId) {
        Ministry m = ministryRepository.findById(ministryId).orElseThrow();
        Map<String, String> params = new HashMap<>();
        var c = m.getCriteria();
        if (c != null) {
            if (c.getGender() != null && !"ALL".equalsIgnoreCase(c.getGender())) params.put("gender", c.getGender());
            if (c.getAgeGroup() != null && !"ALL".equalsIgnoreCase(c.getAgeGroup()))
                params.put("ageGroup", c.getAgeGroup());
            if (c.getMaritalStatus() != null && !"ALL".equalsIgnoreCase(c.getMaritalStatus()))
                params.put("maritalStatus", c.getMaritalStatus());
        }

        List<Map<String, Object>> res = memberServiceFeignClient.searchMembers(params);
        // convert to MemberDto (minimal mapping)
        List<MemberDto> list = new ArrayList<>();
        for (Map<String, Object> mobj : res) {
            MemberDto p = new MemberDto();
            p.setId(Objects.toString(mobj.get("id"), null));
            p.setFirstName(Objects.toString(mobj.get("firstName"), null));
            p.setLastName(Objects.toString(mobj.get("lastName"), null));
            Object age = mobj.get("age");
            if (age != null) p.setAge(Integer.parseInt(age.toString()));
            p.setGender(Objects.toString(mobj.get("gender"), null));
            list.add(p);
        }
        return list;
    }

    private List<MemberDto> potentialMembersFallback(UUID ministryId, Throwable t) {
        return Collections.emptyList();
    }

    // highest role logic similar to earlier...
    @Override
    public Optional<Map<String, Object>> highestRoleForMember(String memberId) {
        List<MinistryMember> entries = ministryMemberRepository.findAllByMemberIdAndActiveTrue(memberId);
        if (entries.isEmpty()) return Optional.empty();

        Set<UUID> mids = entries.stream().map(MinistryMember::getMinistryId).collect(Collectors.toSet());
        Map<UUID, Ministry> ministries = ministryRepository.findAllById(mids).stream().collect(Collectors.toMap(Ministry::getId, x -> x));

        Map<UUID, Integer> depthCache = new HashMap<>();
        for (UUID mid : ministries.keySet()) depthCache.put(mid, computeDepth(mid, ministries));

        MinistryMember best = entries.stream().min(Comparator
                        .comparing((MinistryMember mm) -> depthCache.getOrDefault(mm.getMinistryId(), Integer.MAX_VALUE))
                        .thenComparing(mm -> rolePriority.getOrDefault(mm.getRole(), Integer.MAX_VALUE)))
                .orElseThrow(() -> new NoSuchElementException("No active MinistryMember found for member " + memberId));

        Ministry bestMin = ministries.get(best.getMinistryId());
        Map<String, Object> res = new HashMap<>();
        res.put("memberId", memberId);
        res.put("role", best.getRole());
        res.put("ministryId", bestMin.getId());
        res.put("ministryName", bestMin.getName());
        return Optional.of(res);
    }

    private int computeDepth(UUID mid, Map<UUID, Ministry> ministries) {
        int depth = 0;
        Ministry cur = ministries.get(mid);
        while (cur != null && cur.getParentId() != null) {
            depth++;
            cur = ministries.get(cur.getParentId());
            if (cur == null) cur = ministryRepository.findById(ministries.get(mid).getParentId()).orElse(null);
        }
        return depth;
    }
}