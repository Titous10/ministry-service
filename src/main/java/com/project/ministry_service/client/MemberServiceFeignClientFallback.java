package com.project.ministry_service.client;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class MemberServiceFeignClientFallback implements MemberServiceFeignClient {
    @Override
    public List<Map<String, Object>> searchMembers(Map<String, String> params) {
        return Collections.emptyList();
    }
}
