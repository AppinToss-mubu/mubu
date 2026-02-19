package com.example.mubu.common.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 쇼핑몰 링크를 웹 URL로 정규화합니다.
 * 네이버 쇼핑 API 등에서 앱 딥링크(coupang://, 11st:// 등)가 올 수 있어
 * 토스 인앱 브라우저 등에서는 웹 URL만 열 수 있도록 변환합니다.
 */
public final class MallLinkUtils {

    private MallLinkUtils() {
    }

    /**
     * 앱 스킴 딥링크를 웹 URL로 변환합니다.
     * - coupang://...?rUrl=... → rUrl 디코딩값 (https)
     * - 그 외 알려진 앱 스킴은 추후 확장
     * - 이미 http(s)이거나 변환 불가면 원본 반환
     */
    public static String normalizeToWebUrl(String link) {
        if (link == null || link.isBlank()) {
            return link;
        }
        String trimmed = link.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }

        // coupang://mlp?rUrl=https%3A%2F%2F... 형태에서 웹 URL 추출
        if (trimmed.startsWith("coupang://")) {
            String web = extractCoupangWebUrl(trimmed);
            if (web != null) {
                return web;
            }
            return "https://www.coupang.com";
        }

        // 필요 시 11st, Gmarket 등 추가
        // if (trimmed.startsWith("intent://")) { ... }

        return trimmed;
    }

    private static String extractCoupangWebUrl(String coupangUrl) {
        try {
            int q = coupangUrl.indexOf('?');
            if (q == -1) {
                return null;
            }
            String query = coupangUrl.substring(q + 1);
            for (String param : query.split("&")) {
                int eq = param.indexOf('=');
                if (eq == -1) continue;
                String key = param.substring(0, eq);
                String value = param.substring(eq + 1);
                if ("rUrl".equals(key) && !value.isEmpty()) {
                    String decoded = URLDecoder.decode(value, StandardCharsets.UTF_8);
                    if (decoded.startsWith("http://") || decoded.startsWith("https://")) {
                        return decoded;
                    }
                }
            }
        } catch (Exception ignored) {
            // 파싱 실패 시 null
        }
        return null;
    }
}
