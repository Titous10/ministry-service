package com.project.ministry_service.ministry.application;

import com.project.ministry_service.ministry.api.dto.CreateMinistryRequest;
import com.project.ministry_service.ministry.api.dto.PotentialMemberDto;
import com.project.ministry_service.ministry.domain.model.Ministry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface MinistryService {
    @Transactional
    Ministry createMinistry(CreateMinistryRequest req);
    @Transactional
    Ministry updateMinistry(UUID id, CreateMinistryRequest req);

    @CircuitBreaker(name = "memberServiceClient", fallbackMethod = "potentialMembersFallback")
    List<PotentialMemberDto> getPotentialMembers(UUID ministryId);

    @CircuitBreaker(name = "memberServiceClient", fallbackMethod = "potentialMembersFallback")
    List<PotentialMemberDto> getPotentialMembers(String age, String gender, String maritalStatus);

    Optional<Map<String, Object>> highestRoleForMember(UUID memberId);
}
