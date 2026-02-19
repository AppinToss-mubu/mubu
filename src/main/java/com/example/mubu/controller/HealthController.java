package com.example.mubu.controller;

import com.example.mubu.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @GetMapping("/")
    public ApiResponse<String> root() {
        return ApiResponse.ok("MUBU API Server");
    }

    @GetMapping("/favicon.ico")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void favicon() {
        // 브라우저 favicon 요청 무시 (204 No Content)
    }
}
