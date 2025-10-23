package com.project.ministry_service.ministry.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.ministry_service.common.enums.RoleName;
import lombok.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {

    private String id;
    private String name;
    private String firstName;
    private String lastName;
    private String gender;
    private Integer age;
    private String maritalStatus;
    private String photoUrl;
    private RoleName role;
    private RoleName memberRole;
    private String phone;

    @JsonProperty("personalInfo")
    private void unpackPersonalInfo(Map<String, Object> personalInfo) {
        if (personalInfo == null) return;
        this.firstName = (String) personalInfo.get("firstName");
        this.lastName = (String) personalInfo.get("lastName");
        this.gender = (String) personalInfo.get("gender");
        this.maritalStatus = (String) personalInfo.get("maritalStatus");
        this.name = this.firstName.concat(" ").concat(this.lastName);

        // Calculate age
        Object birthdateObj = personalInfo.get("birthdate");
        if (birthdateObj != null) {
            String birthdateStr = birthdateObj.toString(); // "1964-04-13"
            LocalDate birthDate = LocalDate.parse(birthdateStr);
            this.age = Period.between(birthDate, LocalDate.now()).getYears();
        }
    }

    @JsonProperty("contact")
    private void unpackContact(Map<String, Object> contact) {
        if (contact == null) return;
        this.phone = (String) contact.get("mobilePhoneNumber");
    }

    // Map top-level pictureUrl
    @JsonProperty("pictureUrl")
    private void unpackPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
