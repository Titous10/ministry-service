package com.project.ministry_service.ministry.domain.model.embeddable;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Criteria {
    private String gender; // ALL, MALE, FEMALE
    private String ageGroup; // ALL, KID, ADULT (KID < 12, ADULT >= 12)
    private String maritalStatus; // ALL, MARRIED, SINGLE, ...

    // getters/setters
}
