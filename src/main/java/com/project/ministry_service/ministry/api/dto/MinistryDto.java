package com.project.ministry_service.ministry.api.dto;

import com.project.ministry_service.ministry.api.dto.embeddable.Info;
import com.project.ministry_service.ministry.api.dto.embeddable.MinistryMember;
import com.project.ministry_service.ministry.domain.model.embeddable.Criteria;
import lombok.Data;

@Data
public class MinistryDto {
    private String id;
    private Info info;
    private Criteria configuration;
    private MinistryMember ministryMember;
}
