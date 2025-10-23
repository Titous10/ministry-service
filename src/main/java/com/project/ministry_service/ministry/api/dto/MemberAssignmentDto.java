package com.project.ministry_service.ministry.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.ministry_service.common.enums.RoleName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

import java.util.UUID;

@Data
@ToString
public class MemberAssignmentDto {
    @JsonProperty("id")
    @NotNull
    private String memberId;
    @NotNull
    private RoleName role;
    @NotNull
    private boolean committee;
}
