package com.example.mubu.service;

import com.example.mubu.dto.gemini.*;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

// Gemini 요청 DTO를 만들어주는 역할만 담당
@Service
public class GeminiRequestFactory {

    // 텍스트 + 이미지(byte[]) → GeminiRequest 생성
    public GeminiRequest create(
            String prompt,
            byte[] imageBytes,
            String mimeType
    ) {
        // 이미지 base64 인코딩
        String base64Image = Base64.getEncoder()
                .encodeToString(imageBytes);

        // 이미지 데이터 객체
        GeminiInlineData inlineData = new GeminiInlineData();
        inlineData.setMimeType(mimeType);
        inlineData.setData(base64Image);

        // 이미지 파트
        GeminiPart imagePart = new GeminiPart();
        imagePart.setInlineData(inlineData);

        // 텍스트 파트
        GeminiPart textPart = new GeminiPart();
        textPart.setText(prompt);

        // Gemini는 parts 배열을 요구함
        GeminiContent content = new GeminiContent();
        content.setRole("user");
        content.setParts(List.of(textPart, imagePart));

        // 최종 요청 객체
        GeminiRequest request = new GeminiRequest();
        request.setContents(List.of(content));

        return request;
    }
}
