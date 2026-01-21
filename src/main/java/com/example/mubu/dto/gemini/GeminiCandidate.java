package com.example.mubu.dto.gemini;

import lombok.Getter;
import lombok.Setter;

// Gemini가 생성한 "하나의 응답 후보 candidate"
@Getter
@Setter
public class GeminiCandidate {

    // 실제 모델 응답 내용
    // role = model, parts 안에 text가 들어있음
    private GeminiContent content;

    // 응답이 왜 끝났는지 이유
    // 보통 "STOP"
    private String finishReason;
}
