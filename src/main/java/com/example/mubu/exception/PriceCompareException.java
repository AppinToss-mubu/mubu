package com.example.mubu.exception;

// 가격 비교 실패 시 사용하는 공통 예외
public class PriceCompareException extends RuntimeException {

    public PriceCompareException(String message) {
        super(message);
    }
}
