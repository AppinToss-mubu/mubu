package com.example.mubu.client;

import com.example.mubu.dto.gemini.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

//이미지(또는 이미지 정보)를 받아서 Gemini API에 요청하고 AI 응답을 Java 객체로 돌려준다

//Gemini API HTTP 호출 API Key 관리
//request/response 변환
//컨트롤러 / 서비스는 Gemini API 구조를 몰라도 되게 만드는 게 목표
// Gemini API 실제 호출 담당

@Component
public class GeminiClient {

    private final RestTemplate restTemplate;
    private final String apiKey;

    // Gemini API 기본 경로
    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models";

    public GeminiClient(@Value("${mubu.ai.gemini.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();

        // 1. 애플리케이션 시작 시점에 한 번 출력
        System.out.println(
                "Gemini API Key loaded: " +
                        apiKey.substring(0, 10) + "..."
        );
    }

    // Gemini generateContent 호출
    public GeminiResponse generateContent(String model, GeminiRequest request) {

        // 2. 실제 요청 직전에 다시 한 번 출력
        System.out.println(
                "Gemini API Key used for request: " +
                        apiKey.substring(0, 10) + "..."
        );

        // Gemini는 Authorization 헤더 ❌
        // query param ?key=API_KEY 사용
        String url = BASE_URL
                + "/" + model
                + ":generateContent"
                + "?key=" + apiKey;

        return restTemplate.postForObject(
                url,
                request,
                GeminiResponse.class
        );
    }
}
