package com.project.ministry_service.ministry.domain.repository;

import com.project.ministry_service.ministry.domain.model.MinistryHierarchy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MinistryHierarchyRepository extends JpaRepository<MinistryHierarchy, Long> {
    List<MinistryHierarchy> findAllByAncestorId(UUID ancestorId);
    List<MinistryHierarchy> findAllByDescendantId(UUID descendantId);
    void deleteAllByDescendantId(UUID descendantId);
}