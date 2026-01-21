package com.example.mubu.repository;

import com.example.mubu.dto.ai.PriceCompareResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

// imageId 기준으로 가격 비교 결과를 임시 저장하는 In-memory 저장소
// - Summary API에서 재사용하기 위한 목적
// - 추후 DB / Redis로 교체 가능
@Component
public class PriceCompareResultStore {

    private final ConcurrentHashMap<String, StoredResult> store
            = new ConcurrentHashMap<>();

    // imageId 유효 시간 (30분)
    private static final long EXPIRY_TIME_MS = 30 * 60 * 1000;

    public void save(String imageId, PriceCompareResult result) {
        store.put(imageId, new StoredResult(result));
    }

    public PriceCompareResult find(String imageId) {
        StoredResult stored = store.get(imageId);

        if (stored == null) {
            throw new IllegalArgumentException(
                    "imageId에 해당하는 가격 비교 결과가 존재하지 않습니다."
            );
        }

        // imageId 만료 체크
        if (System.currentTimeMillis() - stored.createdAt > EXPIRY_TIME_MS) {
            store.remove(imageId);
            throw new IllegalArgumentException(
                    "imageId가 만료되었습니다. 다시 비교해주세요."
            );
        }

        return stored.result;
    }

    // 저장 시점 기록을 위한 내부 래퍼 클래스
    private static class StoredResult {
        private final PriceCompareResult result;
        private final long createdAt;

        private StoredResult(PriceCompareResult result) {
            this.result = result;
            this.createdAt = System.currentTimeMillis();
        }
    }
}
