package com.project.ministry_service.ministry.domain.model;


import com.project.ministry_service.common.enums.RoleName;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "ministry_members", uniqueConstraints = {@UniqueConstraint(columnNames = {"ministry_id","member_id"})})
@Data
public class MinistryMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "ministry_id", columnDefinition = "uuid", nullable = false)
    private UUID ministryId;

    @Column(name = "member_id", columnDefinition = "uuid", nullable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    private RoleName role;

    private boolean committee;

    private LocalDate assignedDate = LocalDate.now();

    private boolean active = true;
}

