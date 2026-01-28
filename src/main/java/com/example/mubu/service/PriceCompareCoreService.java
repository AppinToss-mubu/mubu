package com.example.mubu.service;

import com.example.mubu.dto.ai.GeminiAnalyzeResult;
import com.example.mubu.dto.ai.PriceCompareResult;
import com.example.mubu.dto.naver.NaverShoppingItem;
import com.example.mubu.exception.PriceCompareException;
import org.springframework.stereotype.Service;

//핵심 로직
@Service
public class PriceCompareCoreService {

    private final AiAnalyzeService aiAnalyzeService;
    private final NaverShoppingService naverShoppingService;

    public PriceCompareCoreService(
            AiAnalyzeService aiAnalyzeService,
            NaverShoppingService naverShoppingService
    ) {
        this.aiAnalyzeService = aiAnalyzeService;
        this.naverShoppingService = naverShoppingService;
    }

    // 이미지 바이트 기반 가격 비교
    // Facade / 확장 API에서 공통 사용
    public PriceCompareResult compareByImageBytes(
            byte[] imageBytes,
            String mimeType
    ) {

        // 1. AI로 상품명 추출
        GeminiAnalyzeResult aiResult =
                aiAnalyzeService.analyze(imageBytes, mimeType);

        if (aiResult == null || aiResult.getText() == null) {
            throw new PriceCompareException("상품 인식에 실패했습니다.");
        }

        // 검색 키워드 정제 (추가)
        // AI가 제공한 한국어 검색 키워드가 있으면 우선 사용, 없으면 상품명에서 추출
        String searchKeyword = (aiResult.getSearchKeywordKr() != null && !aiResult.getSearchKeywordKr().isBlank())
                ? aiResult.getSearchKeywordKr()
                : extractProductName(aiResult.getText());

        // 2. 네이버 쇼핑 최저가 조회
        NaverShoppingItem lowestItem =
                naverShoppingService.findLowestPriceItem(searchKeyword);

        // 검색 결과가 없어도 성공 응답 반환
        if (lowestItem == null) {
            PriceCompareResult result = new PriceCompareResult(
                    aiResult.getText(),
                    aiResult.getText(), // productName은 AI 분석 텍스트 사용
                    0,  // lowestPrice = 0
                    "", // mallName
                    "", // link
                    ""  // image
            );
            // AI가 인식한 현지 가격/통화 정보 설정
            result.setLocalPrice(aiResult.getLocalPrice());
            result.setLocalCurrency(aiResult.getLocalCurrency());
            return result;
        }

        // 3. 응답 DTO 조합
        PriceCompareResult result = new PriceCompareResult(
                aiResult.getText(),
                lowestItem.getTitle(),
                Integer.parseInt(lowestItem.getLprice()),
                lowestItem.getMallName(),
                lowestItem.getLink(),
                lowestItem.getImage()
        );

        // 4. AI가 텍스트에서 인식한 현지 가격/통화가 있다면 함께 내려준다
        //    (없으면 null 그대로 유지)
        result.setLocalPrice(aiResult.getLocalPrice());
        result.setLocalCurrency(aiResult.getLocalCurrency());

        return result;
    }

    // 확장/표준 API용 메서드
    // imageId 기반 비교 (추후 구현)
    public PriceCompareResult compareByImageId(
            String imageId
    ) {
        throw new UnsupportedOperationException(
                "imageId 기반 비교는 아직 구현되지 않았습니다."
        );
    }

    // 검색 키워드 정제 메서드
    // "상품명: xxx" 형식에서 xxx만 추출
    private String extractProductName(String aiText) {
        // "상품명: xxx" 형식에서 xxx만 추출
        if (aiText.contains("상품명:")) {
            String[] lines = aiText.split("\n");
            for (String line : lines) {
                if (line.startsWith("상품명:")) {
                    return line.replace("상품명:", "").trim();
                }
            }
        }
        // 형식이 안 맞으면 원본 사용 (첫 줄만)
        String[] lines = aiText.split("\n");
        return lines.length > 0 ? lines[0].trim() : aiText.trim();
    }
}
