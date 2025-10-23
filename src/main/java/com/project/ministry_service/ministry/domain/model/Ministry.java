package com.project.ministry_service.ministry.domain.model;


import com.project.ministry_service.common.enums.MinistryType;
import com.project.ministry_service.ministry.domain.model.embeddable.Criteria;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "ministries", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@Data
public class Ministry {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    private MinistryType type;

    private LocalDate establishedDate;

    @Column(columnDefinition = "uuid")
    private UUID parentId;

    private LocalDate termStart;
    private LocalDate termEnd;

    @Embedded
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Criteria criteria;

    private boolean active = true;
}

