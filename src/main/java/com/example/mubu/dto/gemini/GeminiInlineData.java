package com.example.mubu.dto.gemini;

import lombok.Getter;
import lombok.Setter;

//이미지 자체를 Gemini에 전달하는 구조
//Base64 + MIME 타입
@Getter
@Setter
public class GeminiInlineData {

    // image/jpeg, image/png 등
    private String mimeType;

    // Base64 인코딩된 이미지 데이터
    private String data;
}
