package com.project.ministry_service.common.util;

import com.project.ministry_service.common.enums.MinistryType;
import com.project.ministry_service.ministry.api.dto.MinistryDto;
import com.project.ministry_service.ministry.api.dto.embeddable.Info;
import com.project.ministry_service.ministry.api.dto.embeddable.MinistryMemberDto;
import com.project.ministry_service.ministry.domain.model.Ministry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class MinistryMapper {

    // ------------------ ENTITY → DTO ------------------
    public static MinistryDto toDto(Ministry ministry) {
        if (ministry == null) return null;

        MinistryDto dto = new MinistryDto();
        dto.setId(ministry.getId() != null ? ministry.getId().toString() : null);

        Info info = new Info();
        info.setName(ministry.getName());
        info.setType(ministry.getType() != null ? ministry.getType() : null);
        info.setStartDate(ministry.getTermStart() != null ? ministry.getTermStart() : null);
        info.setEndDate(ministry.getTermEnd() != null ? ministry.getTermEnd() : null);
        info.setAnniversaryDate(ministry.getEstablishedDate() != null ? ministry.getEstablishedDate() : null);
        info.setStatus(ministry.isActive() ? "active" : "inactive");
        info.setDirection(ministry.getParentId() != null ? ministry.getParentId().toString() : null);

        dto.setInfo(info);
        dto.setConfiguration(ministry.getCriteria());
        dto.setMinistryMemberDto(null); // optional, fill later if needed

        return dto;
    }

    public static List<MinistryDto> toDtoList(List<Ministry> ministries) {
        if (ministries == null) return null;
        return ministries.stream().map(MinistryMapper::toDto).collect(Collectors.toList());
    }

    // ------------------ DTO → ENTITY ------------------
    public static Ministry toEntity(MinistryDto dto) {
        if (dto == null) return null;

        Ministry ministry = new Ministry();
        // id ignored for creation
        ministry.setName(dto.getInfo() != null ? dto.getInfo().getName() : null);
        ministry.setType(dto.getInfo() != null && dto.getInfo().getType() != null
                ? dto.getInfo().getType()
                : null);
        ministry.setEstablishedDate(dto.getInfo() != null ? dto.getInfo().getAnniversaryDate() : null);
        ministry.setTermStart(dto.getInfo() != null ? dto.getInfo().getStartDate() : null);
        ministry.setTermEnd(dto.getInfo() != null ? dto.getInfo().getEndDate() : null);
        ministry.setCriteria(dto.getConfiguration());
        ministry.setActive(true); // default true
        // parentId ignored for creation
        return ministry;
    }

    public static Ministry toEntityWithId(MinistryDto dto) {
        if (dto == null) return null;

        Ministry ministry = toEntity(dto);
        ministry.setId(dto.getId() != null ? UUID.fromString(dto.getId()) : null);
        return ministry;
    }
}
