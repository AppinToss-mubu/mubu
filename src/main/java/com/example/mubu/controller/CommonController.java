package com.example.mubu.controller;

import com.example.mubu.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "COMMON", description = "공통 API")
@RestController
@RequestMapping("/common")
public class CommonController { // 1. 헬스체크 / 버전 조회 공통 API - F99-R01, F99-R02

    @Value("${mubu.service.version}")
    private String version;

    @Operation(
            summary = "헬스 체크",
            description = "API-F99-R01-COMMON"
    )
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.ok("OK");
    }

    @Operation(
            summary = "서비스 버전 조회",
            description = "API-F99-R02-COMMON"
    )
    @GetMapping("/version")
    public ApiResponse<String> version() {
        return ApiResponse.ok(version);
    }
}
