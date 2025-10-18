package com.project.ministry_service.ministry.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.ministry_service.common.enums.MinistryType;
import com.project.ministry_service.ministry.domain.model.embeddable.Criteria;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@ToString
public class CreateMinistryRequest {
    @NotNull
    private String name;
    @NotNull
    private MinistryType type;
    @NotNull
    private LocalDate establishedDate;
    @JsonProperty("direction")
    private UUID parentId;
    @NotNull
    private LocalDate termStart;
    @NotNull
    private LocalDate termEnd;
    @NotNull
    private Criteria criteria;

    // actual members to persist (committee/assigned)
    @NotEmpty(message = "You must assign at least one member")
    @JsonProperty("members")
    private List<MemberAssignmentDto> membersToAssign;
}

