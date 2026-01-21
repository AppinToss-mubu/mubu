package com.example.mubu.dto.gemini;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

// Gemini API 응답 최상위 객체
@Getter
@Setter
public class GeminiResponse {

    // Gemini가 생성한 응답 결과 목록
    // 보통 candidates[0] 하나만 사용함
    private List<GeminiCandidate> candidates;

    // 토큰 사용량 정보 (지금은 안 써도 됨)
    // 추후 비용 계산이나 로그용
    private GeminiUsageMetadata usageMetadata;
}
