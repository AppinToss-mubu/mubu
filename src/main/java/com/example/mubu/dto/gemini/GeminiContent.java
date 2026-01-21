package com.example.mubu.dto.gemini;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

//contents[] 안에 들어가는 한 덩어리
//Gemini 기준으로 누가 말했는지 + 무엇을 보냈는지
@Getter
@Setter
public class GeminiContent {

    // "user" or "model"
    private String role;

    // 실제 메시지 내용 (텍스트 / 이미지)
    private List<GeminiPart> parts;
}
