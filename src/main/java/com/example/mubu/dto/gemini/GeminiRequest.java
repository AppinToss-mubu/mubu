package com.example.mubu.dto.gemini;

// Gemini API 요청의 최상위 객체
// Gemini는 contents 배열만 있으면 요청이 성립함

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GeminiRequest {

    // 사용자/시스템 메시지 묶음
    private List<GeminiContent> contents;
}
