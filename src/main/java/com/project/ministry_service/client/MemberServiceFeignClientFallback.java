package com.project.ministry_service.client;

import com.project.ministry_service.ministry.api.dto.MemberDto;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Component
public class MemberServiceFeignClientFallback implements MemberServiceFeignClient {
    @Override
    public List<Map<String,Object>> searchMembers(Map<String, String> filters) {
        return Collections.emptyList(); // fallback/mock
    }

    @Override
    public List<Map<String,Object>> getAllMembers(@RequestParam String age, @RequestParam String gender, @RequestParam String maritalStatus) {
        return Collections.emptyList(); // fallback/mock
    }

    @Override
    public List<MemberDto> getMembersByIds(Set<String> ids) {
        return new ArrayList<>();
    }
}
