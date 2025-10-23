package com.project.ministry_service.ministry.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "ministry_hierarchy", uniqueConstraints = {@UniqueConstraint(columnNames = {"ancestor_id","descendant_id"})})
@Data
public class MinistryHierarchy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ancestor_id", columnDefinition = "uuid", nullable = false)
    private UUID ancestorId;

    @Column(name = "descendant_id", columnDefinition = "uuid", nullable = false)
    private UUID descendantId;

    @Column(nullable = false)
    private Integer depth;
}
