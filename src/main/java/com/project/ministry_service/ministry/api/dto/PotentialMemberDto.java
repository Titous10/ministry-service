package com.project.ministry_service.ministry.api.dto;

import lombok.Data;

@Data
public class PotentialMemberDto {
    private String id;
    private String firstName;
    private String lastName;
    private String gender;
    private Integer age;
}
