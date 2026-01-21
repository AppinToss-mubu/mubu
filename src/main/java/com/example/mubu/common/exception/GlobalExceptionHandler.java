package com.example.mubu.common.exception;

import com.example.mubu.common.dto.ApiErrorResponse;
import com.example.mubu.exception.PriceCompareException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 전역 예외 처리
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 가격 비교 도메인 예외 처리
    @ExceptionHandler(PriceCompareException.class)
    public ApiErrorResponse handlePriceCompareException(
            PriceCompareException e
    ) {
        return new ApiErrorResponse(e.getMessage());
    }

    // 미구현 기능 예외 처리
    @ExceptionHandler(UnsupportedOperationException.class)
    public ApiErrorResponse handleUnsupportedOperationException(
            UnsupportedOperationException e
    ) {
        return new ApiErrorResponse(e.getMessage());
    }

    // 기타 예외 처리
    @ExceptionHandler(Exception.class)
    public ApiErrorResponse handleException(
            Exception e
    ) {
        return new ApiErrorResponse("서버 오류가 발생했습니다.");
    }
}
