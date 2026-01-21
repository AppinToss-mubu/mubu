package com.example.mubu.controller;

import com.example.mubu.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "IMAGE", description = "이미지 업로드 API")
@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Operation(
            summary = "이미지 업로드",
            description = "API-F01-R01-IMAGE | 이미지 파일을 업로드하고 imageId를 반환한다"
    )
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<ImageUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file
    ) {

        // 1. 기본 검증 (최소)
        if (file.isEmpty()) {
            return ApiResponse.fail("파일이 비어있습니다.");
        }

        // 2. 임시 imageId 생성
        String imageId = "img_" + UUID.randomUUID();

        // ⚠️ 지금은 저장하지 않는다 (요청 스코프에서만 사용)
        // MultipartFile은 메서드 종료 후 GC 대상

        // 3. 응답
        return ApiResponse.ok(new ImageUploadResponse(imageId));
    }

    // 내부 응답 DTO (지금은 컨트롤러 안에 둔다)
    public static class ImageUploadResponse {
        private final String imageId;

        public ImageUploadResponse(String imageId) {
            this.imageId = imageId;
        }

        public String getImageId() {
            return imageId;
        }
    }
}
