package com.example.mubu.common.exception;

import com.example.mubu.common.dto.ApiErrorResponse;
import com.example.mubu.exception.PriceCompareException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 전역 예외 처리
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 가격 비교 도메인 예외 처리 (400 Bad Request)
    @ExceptionHandler(PriceCompareException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handlePriceCompareException(
            PriceCompareException e
    ) {
        return new ApiErrorResponse(e.getMessage());
    }

    // 미구현 기능 예외 처리 (501 Not Implemented)
    @ExceptionHandler(UnsupportedOperationException.class)
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public ApiErrorResponse handleUnsupportedOperationException(
            UnsupportedOperationException e
    ) {
        return new ApiErrorResponse(e.getMessage());
    }

    // 기타 예외 처리 (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleException(
            Exception e
    ) {
        // 디버깅용 로그 추가
        e.printStackTrace();
        return new ApiErrorResponse("서버 오류가 발생했습니다.");
    }
}
