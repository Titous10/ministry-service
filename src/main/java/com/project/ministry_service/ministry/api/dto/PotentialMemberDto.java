package com.project.ministry_service.ministry.api.dto;

import lombok.Data;

@Data
public class PotentialMemberDto {
    private String id;
    private String name;
    private String firstName;
    private String lastName;
    private String gender;
    private Integer age;
    private String maritalStatus;
    private String photoUrl;
    private String role;
    private String memberRole;
    private String phone;
}
