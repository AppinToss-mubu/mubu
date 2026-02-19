package com.example.mubu.client;

import com.example.mubu.dto.naver.NaverShoppingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

//네이버 쇼핑 API HTTP 호출
//Header 인증 처리
//query / display / sort 파라미터 관리

@Component
public class NaverShoppingClient {

    private static final Logger log = LoggerFactory.getLogger(NaverShoppingClient.class);

    private static final String BASE_URL =
            "https://openapi.naver.com/v1/search/shop.json";

    private final RestTemplate restTemplate;
    private final String clientId;
    private final String clientSecret;

    public NaverShoppingClient(
            RestTemplate restTemplate,
            @Value("${mubu.naver.client-id}") String clientId,
            @Value("${mubu.naver.client-secret}") String clientSecret
    ) {
        this.restTemplate = restTemplate;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    // 네이버 쇼핑 검색 API 호출
    public NaverShoppingResponse search(
            String query,
            int display,
            String sort
    ) {

        // 쿼리 파라미터 조립
        String url = UriComponentsBuilder
                .fromHttpUrl(BASE_URL)
                .queryParam("query", query)
                .queryParam("display", display)
                .queryParam("sort", sort)
                .build()
                .toUriString();

        // 헤더 인증 처리
        var headers = new org.springframework.http.HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        var entity = new org.springframework.http.HttpEntity<>(headers);

        return restTemplate
                .exchange(
                        url,
                        org.springframework.http.HttpMethod.GET,
                        entity,
                        NaverShoppingResponse.class
                )
                .getBody();
    }
}
