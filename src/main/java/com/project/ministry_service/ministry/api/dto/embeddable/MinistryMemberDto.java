package com.project.ministry_service.ministry.api.dto.embeddable;


import com.project.ministry_service.ministry.api.dto.MemberDto;
import lombok.Data;


import java.util.List;

@Data
public class MinistryMemberDto {
        List<MemberDto> unit;
        List<MemberDto> committee;
        MemberDto leader;
}
