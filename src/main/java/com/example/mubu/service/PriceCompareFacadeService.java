package com.example.mubu.service;

import com.example.mubu.dto.ai.PriceCompareResult;
import org.springframework.stereotype.Service;

@Service
public class PriceCompareFacadeService {

    private final PriceCompareCoreService priceCompareCoreService;

    public PriceCompareFacadeService(
            PriceCompareCoreService priceCompareCoreService
    ) {
        this.priceCompareCoreService = priceCompareCoreService;
    }

    // Toss UX용 단일 진입점
    // 내부적으로 Core 로직 호출
    public PriceCompareResult compareWithImage(
            byte[] imageBytes,
            String mimeType
    ) {
        return priceCompareCoreService.compareByImageBytes(
                imageBytes,
                mimeType
        );
    }
}
