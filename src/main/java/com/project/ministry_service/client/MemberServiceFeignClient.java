package com.project.ministry_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Primary
@FeignClient(name = "member-service", url = "${member.service.base-url}/api/v1", fallback = MemberServiceFeignClientFallback.class)
public interface MemberServiceFeignClient {
    @GetMapping("/members/search1111")
    List<Map<String,Object>> searchMembers(@RequestParam Map<String,String> filters);

    @GetMapping("/members/search")
    List<Map<String,Object>> getAllMembers(@RequestParam String age, @RequestParam String gender, @RequestParam String maritalStatus);
}
