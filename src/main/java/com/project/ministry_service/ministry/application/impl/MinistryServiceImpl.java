package com.project.ministry_service.ministry.application.impl;

import com.project.ministry_service.client.MemberServiceFeignClient;
import com.project.ministry_service.common.enums.RoleName;
import com.project.ministry_service.common.util.MinistryMapper;
import com.project.ministry_service.ministry.api.dto.CreateMinistryRequest;
import com.project.ministry_service.ministry.api.dto.MemberAssignmentDto;
import com.project.ministry_service.ministry.api.dto.MemberDto;
import com.project.ministry_service.ministry.api.dto.MinistryDto;
import com.project.ministry_service.ministry.api.dto.embeddable.MinistryMemberDto;
import com.project.ministry_service.ministry.application.HierarchyJdbcService;
import com.project.ministry_service.ministry.application.MinistryService;
import com.project.ministry_service.ministry.domain.model.Ministry;
import com.project.ministry_service.ministry.domain.model.MinistryMember;
import com.project.ministry_service.ministry.domain.repository.MinistryHierarchyRepository;
import com.project.ministry_service.ministry.domain.repository.MinistryMemberRepository;
import com.project.ministry_service.ministry.domain.repository.MinistryRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class MinistryServiceImpl implements MinistryService {

    private final MinistryRepository ministryRepository;
    private final MinistryMemberRepository ministryMemberRepository;
    private final MemberServiceFeignClient memberServiceFeignClient;
    private final HierarchyJdbcService hierarchyJdbcService;
    private final MinistryMapper ministryMapper;

    private static final Map<RoleName, Integer> rolePriority = Map.ofEntries(
            // 🕊️ Top-Level Pastoral Leadership
            Map.entry(RoleName.SENIOR_PASTOR, 0),
            Map.entry(RoleName.LEAD_PASTOR, 1),
            Map.entry(RoleName.ASSOCIATE_PASTOR, 2),

            // 🧱 Executive & Governing Leadership
            Map.entry(RoleName.CHAIRMAN, 10),
            Map.entry(RoleName.VICE_CHAIRMAN, 11),
            Map.entry(RoleName.DEACON, 12),
            Map.entry(RoleName.DEACONESS, 13),

            // 🏛️ Departmental & Ministry Heads
            Map.entry(RoleName.PRESIDENT, 20),
            Map.entry(RoleName.VICE_PRESIDENT, 21),
            Map.entry(RoleName.SUPERINTENDENT, 22),
            Map.entry(RoleName.COORDINATOR, 23),
            Map.entry(RoleName.ASSISTANT_COORDINATOR, 24),

            // 🗂️ Administrative & Financial Staff
            Map.entry(RoleName.SECRETARY, 30),
            Map.entry(RoleName.ASSISTANT_SECRETARY, 31),
            Map.entry(RoleName.TREASURER, 32),
            Map.entry(RoleName.ASSISTANT_TREASURER, 33),
            Map.entry(RoleName.FINANCE_OFFICER, 34),
            Map.entry(RoleName.ADVISOR, 35),
            Map.entry(RoleName.REPRESENTATIVE, 36),

            // 💻 Technical & Operational Directors
            Map.entry(RoleName.IT_ADMINISTRATOR, 40),
            Map.entry(RoleName.MEDIA_DIRECTOR, 41),
            Map.entry(RoleName.PROGRAM_DIRECTOR, 42),
            Map.entry(RoleName.PUBLIC_RELATIONS_OFFICER, 43),

            // 🧑🏽‍🏫 Educational & Service Roles
            Map.entry(RoleName.TEACHER, 50),
            Map.entry(RoleName.USHER, 51),
            Map.entry(RoleName.GREETER, 52),

            // 🧰 Facilities & Maintenance
            Map.entry(RoleName.MAINTENANCE_WORKER, 60),
            Map.entry(RoleName.CUSTODIAN, 61),
            Map.entry(RoleName.GROUNDSKEEPER, 62),
            Map.entry(RoleName.BUS_DRIVER, 63),
            Map.entry(RoleName.MECHANIC, 64),

            // 🎨 Creative, Arts & Tech Team
            Map.entry(RoleName.SOFTWARE_DEVELOPER, 70),
            Map.entry(RoleName.ELECTRICAL_TECHNICIAN, 71),
            Map.entry(RoleName.DANCER, 72),
            Map.entry(RoleName.PROJECTIONIST, 73),
            Map.entry(RoleName.AUDIO_ENGINEER, 74),
            Map.entry(RoleName.VIDEO_OPERATOR, 75),
            Map.entry(RoleName.LIGHTING_TECHNICIAN, 76),
            Map.entry(RoleName.MEDIA_CONTENT_CREATOR, 77),
            Map.entry(RoleName.TECHNICAL_SUPPORT, 78),

            // 🎉 Event & General Roles
            Map.entry(RoleName.EVENT_PLANNER, 90),
            Map.entry(RoleName.SECURITY_OFFICER, 91),
            Map.entry(RoleName.VOLUNTEER, 92),
            Map.entry(RoleName.INTERN, 93),
            Map.entry(RoleName.TRAINEE, 94),
            Map.entry(RoleName.MEMBER, 95)
    );
    private final MinistryHierarchyRepository ministryHierarchyRepository;

    public MinistryServiceImpl(MinistryRepository ministryRepository,
                               MinistryMemberRepository ministryMemberRepository,
                               MemberServiceFeignClient memberServiceFeignClient,
                               HierarchyJdbcService hierarchyJdbcService,
                               MinistryMapper ministryMapper, MinistryHierarchyRepository ministryHierarchyRepository) {
        this.ministryRepository = ministryRepository;
        this.ministryMemberRepository = ministryMemberRepository;
        this.memberServiceFeignClient = memberServiceFeignClient;
        this.hierarchyJdbcService = hierarchyJdbcService;
        this.ministryMapper = ministryMapper;
        this.ministryHierarchyRepository = ministryHierarchyRepository;
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

    public List<MinistryDto> getMinistriesDetails() {
        // 1️⃣ Fetch all ministries once
        List<Ministry> ministries = ministryRepository.findAll();
        List<MinistryDto> ministryDtos = ministryMapper.toDtoList(ministries);

        if (ministries.isEmpty()) return List.of();

        // 2️⃣ Fetch all MinistryMembers in one query
        List<MinistryMember> allMembers = ministryMemberRepository.findAllByMinistryIdIn(
                ministries.stream().map(Ministry::getId).collect(Collectors.toSet())
        );

        // 3️⃣ Group MinistryMembers by ministryId
        Map<UUID, List<MinistryMember>> membersByMinistry = allMembers.stream()
                .collect(Collectors.groupingBy(MinistryMember::getMinistryId));

        // 4️⃣ Batch fetch all MemberDtos from Feign
        Set<String> allMemberIds = allMembers.stream()
                .map(MinistryMember::getMemberId)
                .collect(Collectors.toSet());

        Map<String, MemberDto> memberDtoMap = memberServiceFeignClient.getMembersByIds(allMemberIds).stream()
                .collect(Collectors.toMap(MemberDto::getId, dto -> dto));

        // 5️⃣ Populate MinistryMemberDto for each ministry
        ministryDtos.forEach(ministryDto -> {
            UUID ministryId = UUID.fromString(ministryDto.getId());
            List<MinistryMember> ministryMembers = membersByMinistry.getOrDefault(ministryId, List.of());

            MinistryMemberDto ministryMemberDto = new MinistryMemberDto();
            ministryMemberDto.setUnit(new ArrayList<>());
            ministryMemberDto.setCommittee(new ArrayList<>());

            MemberDto leader = null;
            int leaderPriority = Integer.MAX_VALUE;

            for (MinistryMember mm : ministryMembers) {
                MemberDto original = memberDtoMap.get(mm.getMemberId());
                if (original == null) continue;

                // Build a new DTO directly for this ministry
                MemberDto dto = MemberDto.builder()
                        .id(original.getId())
                        .name(original.getName())
                        .firstName(original.getFirstName())
                        .lastName(original.getLastName())
                        .gender(original.getGender())
                        .age(original.getAge())
                        .maritalStatus(original.getMaritalStatus())
                        .photoUrl(original.getPhotoUrl())
                        .phone(original.getPhone())
                        .role(mm.isCommittee() ? mm.getRole() : null)
                        .memberRole(!mm.isCommittee() ? mm.getRole() : null)
                        .build();

                if (mm.isCommittee()) {
                    ministryMemberDto.getCommittee().add(dto);
                    int priority = rolePriority.getOrDefault(mm.getRole(), Integer.MAX_VALUE);
                    if (priority < leaderPriority) {
                        leaderPriority = priority;
                        leader = dto;
                    }
                } else {
                    ministryMemberDto.getUnit().add(dto);
                }
            }

            ministryMemberDto.setLeader(leader);
            ministryDto.setMinistryMemberDto(ministryMemberDto);
        });

        return ministryDtos;
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

        // Fetch all active ministry memberships, prioritizing committee memberships
        List<MinistryMember> entries = ministryMemberRepository.findAllByMemberIdAndActiveTrueAndCommitteeTrue(memberId);
        if (entries.isEmpty()) entries = ministryMemberRepository.findAllByMemberIdAndActiveTrue(memberId);
        if (entries.isEmpty()) return Optional.empty();

        // Collect ministry IDs
        Set<UUID> ministryIds = entries.stream()
                .map(MinistryMember::getMinistryId)
                .collect(Collectors.toSet());

        // Fetch hierarchy info (descent with min-of-max-depth)
        Map<UUID, String> ministryHierarchyMap = ministryHierarchyRepository.findDescentWithMinOfMaxDepth(ministryIds)
                .stream()
                .collect(Collectors.toMap(
                        x -> (UUID) x[0],
                        x -> String.valueOf(x[2])
                ));

        // Pick the best member by hierarchy depth first, then role priority
        MinistryMember best = entries.stream()
                .min(Comparator
                        .comparing((MinistryMember mm) -> ministryHierarchyMap.containsKey(mm.getMinistryId()) ? 0 : Integer.MAX_VALUE)
                        .thenComparing(mm -> rolePriority.getOrDefault(mm.getRole(), Integer.MAX_VALUE))
                )
                .orElse(entries.get(0)); // fallback in case something goes wrong

        return Optional.of(Map.of(
                "memberId", best.getMemberId(),
                "role", best.getRole(),
                "ministryId", best.getMinistryId(),
                "ministryName", ministryHierarchyMap.get(best.getMinistryId())
        ));
    }
}