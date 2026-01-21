package com.example.mubu.common.dto;

// 공통 에러 응답 DTO
public class ApiErrorResponse {

    private boolean success;
    private String message;

    public ApiErrorResponse(String message) {
        this.success = false;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
