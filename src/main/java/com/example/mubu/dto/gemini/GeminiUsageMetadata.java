package com.example.mubu.dto.gemini;

import lombok.Getter;
import lombok.Setter;

// Gemini API 토큰 사용량 정보
@Getter
@Setter
public class GeminiUsageMetadata {

    // 입력 프롬프트에서 사용된 토큰 수
    private int promptTokenCount;

    // 모델 응답 생성에 사용된 토큰 수
    private int candidatesTokenCount;

    // 전체 사용 토큰 수
    private int totalTokenCount;
}
