package com.project.ministry_service.ministry.domain.repository;

import com.project.ministry_service.ministry.domain.model.Ministry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MinistryRepository extends JpaRepository<Ministry, UUID> {
    Optional<Ministry> findByName(String name);
}
