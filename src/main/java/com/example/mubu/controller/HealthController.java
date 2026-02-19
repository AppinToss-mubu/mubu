package com.example.mubu.controller;

import com.example.mubu.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 표준 헬스 체크 엔드포인트
 * Fly.io 등 모니터링 도구에서 사용
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.ok("OK");
    }
}
