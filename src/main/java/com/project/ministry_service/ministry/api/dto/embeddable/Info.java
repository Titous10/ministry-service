package com.project.ministry_service.ministry.api.dto.embeddable;

import com.project.ministry_service.common.enums.MinistryType;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class Info {
    private String name;
    private MinistryType type;
    private double treasuryAmount;
    private String direction;
    private List<String> subUnits;
    private LocalDate anniversaryDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
