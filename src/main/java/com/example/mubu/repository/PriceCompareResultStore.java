package com.example.mubu.repository;

import com.example.mubu.dto.ai.PriceCompareResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

// imageId 기준으로 가격 비교 결과를 임시 저장하는 In-memory 저장소
// - Summary API에서 재사용하기 위한 목적
// - 추후 DB / Redis로 교체 가능
@Component
public class PriceCompareResultStore {

    private final ConcurrentHashMap<String, PriceCompareResult> store
            = new ConcurrentHashMap<>();

    public void save(String imageId, PriceCompareResult result) {
        store.put(imageId, result);
    }

    public PriceCompareResult find(String imageId) {
        return store.get(imageId);
    }
}
