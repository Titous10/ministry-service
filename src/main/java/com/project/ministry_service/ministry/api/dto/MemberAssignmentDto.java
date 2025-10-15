package com.project.ministry_service.ministry.api.dto;

import com.project.ministry_service.common.enums.RoleName;
import lombok.Data;

import java.util.UUID;

@Data
public class MemberAssignmentDto {
    private UUID memberId;
    private RoleName role;
    private boolean committee;
}
