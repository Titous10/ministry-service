package com.project.ministry_service.ministry.domain.repository;

import com.project.ministry_service.ministry.domain.model.MinistryMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface MinistryMemberRepository extends JpaRepository<MinistryMember, UUID> {
    List<MinistryMember> findAllByMemberIdAndActiveTrueAndCommitteeTrue(String memberId);
    List<MinistryMember> findAllByMemberIdAndActiveTrue(String memberId);

    List<MinistryMember> findAllByMinistryId(UUID ministryId);

    boolean existsByMinistryIdAndMemberId(UUID ministryId, String memberId);
    void deleteAllByMinistryId(UUID ministryId);

    List<MinistryMember> findAllByMinistryIdIn(Set<UUID> collect);
}