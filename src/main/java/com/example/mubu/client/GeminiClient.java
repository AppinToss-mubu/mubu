package com.example.mubu.client;

import com.example.mubu.dto.gemini.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
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

        // burst limit 방지 - 2초 대기
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Gemini는 Authorization 헤더 ❌
        // query param ?key=API_KEY 사용
        String url = BASE_URL
                + "/" + model
                + ":generateContent"
                + "?key=" + apiKey;

        // 429 에러 시 재시도 로직 (exponential backoff)
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                return restTemplate.postForObject(
                        url,
                        request,
                        GeminiResponse.class
                );
            } catch (HttpClientErrorException e) {
                // 429 Too Many Requests 에러 처리
                if (e.getStatusCode().value() == 429) {
                    if (i < maxRetries - 1) {
                        int waitTime = (i + 1) * 5000; // 5초, 10초, 15초 대기
                        System.out.println(
                                "429 에러 발생. " + waitTime + "ms 후 재시도 (" + (i + 1) + "/" + maxRetries + ")"
                        );
                        try {
                            Thread.sleep(waitTime);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("재시도 대기 중 인터럽트 발생", ie);
                        }
                    } else {
                        System.out.println("최대 재시도 횟수 초과. 429 에러를 다시 던집니다.");
                        throw e;
                    }
                } else {
                    // 429가 아닌 다른 HTTP 에러는 즉시 던짐
                    throw e;
                }
            } catch (RestClientException e) {
                // RestClientException (네트워크 에러 등)은 재시도하지 않고 즉시 던짐
                throw e;
            }
        }

        // 이 코드는 실행되지 않아야 하지만, 컴파일러를 위한 fallback
        throw new RuntimeException("예상치 못한 오류: 재시도 로직을 통과했습니다.");
    }
}
