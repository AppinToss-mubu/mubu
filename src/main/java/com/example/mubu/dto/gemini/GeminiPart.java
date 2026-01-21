package com.example.mubu.dto.gemini;

import lombok.Getter;
import lombok.Setter;

//text 하나일 수도 있고 image 하나일 수도 있음
//Gemini는 항상 parts 배열을 요구함
@Getter
@Setter
public class GeminiPart {

    // 텍스트 요청일 때 사용
    private String text;

    // 이미지 요청일 때 사용
    private GeminiInlineData inlineData;
}
