package com.project.ministry_service.ministry.api.dto;

import com.project.ministry_service.common.enums.MinistryType;
import com.project.ministry_service.ministry.domain.model.embeddable.Criteria;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateMinistryRequest {
    private String name;
    private MinistryType type;
    private LocalDate establishedDate;
    private UUID parentId;
    private LocalDate termStart;
    private LocalDate termEnd;
    private Criteria criteria;

    // actual members to persist (committee/assigned)
    private List<MemberAssignmentDto> membersToAssign;
}

