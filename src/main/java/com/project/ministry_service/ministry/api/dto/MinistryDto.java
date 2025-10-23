package com.project.ministry_service.ministry.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.ministry_service.ministry.api.dto.embeddable.Info;
import com.project.ministry_service.ministry.api.dto.embeddable.MinistryMemberDto;
import com.project.ministry_service.ministry.domain.model.embeddable.Criteria;
import lombok.Data;

@Data
public class MinistryDto {
    private String id;
    private Info info = new Info();
    private Criteria configuration;
    @JsonProperty("members")
    private MinistryMemberDto ministryMemberDto;
}
