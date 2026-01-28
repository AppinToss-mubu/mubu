package com.example.mubu.controller;

import com.example.mubu.dto.ai.PriceCompareResult;
import com.example.mubu.dto.price.PriceCompareRequest;
import com.example.mubu.dto.price.PriceCompareSummaryRequest;
import com.example.mubu.dto.price.PriceCompareSummaryResponse;
import com.example.mubu.repository.PriceCompareResultStore;
import com.example.mubu.service.NaverShoppingService;
import com.example.mubu.service.PriceCompareCoreService;
import com.example.mubu.service.PriceCompareFacadeService;
import com.example.mubu.service.PriceCompareSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(
        name = "PRICE",
        description = "AI 상품 인식 결과를 기반으로 한국 최저가를 조회하고 가격 비교 결과를 제공하는 API"
)
@RestController
@RequestMapping("/api/price")
public class PriceCompareController {

    private final PriceCompareFacadeService priceCompareFacadeService;
    private final PriceCompareCoreService priceCompareCoreService;
    private final NaverShoppingService naverShoppingService;

    // [ADD] 가격 비교 결과 임시 저장소
    private final PriceCompareResultStore resultStore;

    // [ADD] 결과 요약 계산 서비스
    private final PriceCompareSummaryService priceCompareSummaryService;

    public PriceCompareController(
            PriceCompareFacadeService priceCompareFacadeService,
            PriceCompareCoreService priceCompareCoreService,
            NaverShoppingService naverShoppingService,
            PriceCompareResultStore resultStore,
            PriceCompareSummaryService priceCompareSummaryService
    ) {
        this.priceCompareFacadeService = priceCompareFacadeService;
        this.priceCompareCoreService = priceCompareCoreService;
        this.naverShoppingService = naverShoppingService;
        this.resultStore = resultStore;
        this.priceCompareSummaryService = priceCompareSummaryService;
    }

    // Toss MiniApp 전용 API
    // 이미지 파일 1번 호출로 전체 비교 수행
    @Operation(
            summary = "이미지 기반 가격 비교 (Toss 전용)",
            description = """
            상품 이미지를 업로드하면
            AI 상품 인식 → 한국 최저가 조회 → 가격 비교 결과를
            단일 호출로 반환합니다.

            Toss MiniApp 환경에서 사용되는 전용 API입니다.
            """
    )
    @PostMapping(
            value = "/compare-with-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public PriceCompareResult compareWithImage(
            @RequestParam("file") MultipartFile file,
            // [ADD] Web / 외부앱에서 자체 생성한 imageId를 함께 보낼 수 있도록 선택 파라미터 추가
            //       - 없으면 서버에서 UUID 생성
            @RequestParam(value = "imageId", required = false) String clientImageId
    ) throws Exception {

        // MultipartFile → byte[]
        byte[] imageBytes = file.getBytes();

        // MIME 타입 추출
        String mimeType = file.getContentType();

        // Facade 서비스로 위임
        PriceCompareResult result =
                priceCompareFacadeService.compareWithImage(imageBytes, mimeType);

        // [ADD] Summary API 연계를 위한 imageId 생성
        //      - 클라이언트가 전달한 imageId가 있으면 그대로 사용
        //      - 없으면 서버에서 UUID 생성
        String imageId = (clientImageId != null && !clientImageId.isBlank())
                ? clientImageId
                : UUID.randomUUID().toString();

        // [ADD] imageId를 결과에 주입
        result.setImageId(imageId);

        // [ADD] 결과를 In-memory 저장소에 저장
        resultStore.save(imageId, result);

        return result;
    }

    // 확장/표준 API
    // imageId 기반, 향후 Web/App/외부 연동용
    @Operation(
            summary = "imageId 기반 가격 비교 (확장 API)",
            description = """
            업로드된 imageId를 기준으로 가격 비교를 수행합니다.

            Web/App/외부 연동을 위한 확장 API이며,
            현재는 구조만 정의되어 있습니다.
            """
    )
    @PostMapping("/compare")
    public PriceCompareResult compareByImageId(
            @RequestBody PriceCompareRequest request
    ) {
        return priceCompareCoreService.compareByImageId(
                request.getImageId()
        );
    }

    // F-04 결과요약
    @Operation(
            summary = "가격 비교 결과 요약",
            description = """
            가격 비교 결과(imageId)를 기준으로
            사용자가 입력한 현지 가격을 환율 변환하여
            절약 금액과 요약 문구를 반환합니다.

            공통 환경(UI 표시용) 보조 API입니다.
            """
    )
    @PostMapping("/result/summary")
    public PriceCompareSummaryResponse summarizeResult(
            @RequestBody PriceCompareSummaryRequest request
    ) {
        // [ADD] imageId로 기존 비교 결과 조회
        PriceCompareResult compareResult =
                resultStore.find(request.getImageId());

        if (compareResult == null) {
            throw new IllegalArgumentException(
                    "imageId에 해당하는 가격 비교 결과가 존재하지 않습니다."
            );
        }

        // [ADD] 요약 계산 서비스 위임
        return priceCompareSummaryService.summarize(
                compareResult,
                request
        );
    }

    //F-05 외부 쇼핑 링크
    @Operation(
            summary = "외부 쇼핑 링크 조회",
            description = """
            상품명을 기준으로
            네이버 쇼핑의 최저가 상품 상세 페이지 링크를 반환합니다.
            """
    )
    @GetMapping("/external/link")
    public String getExternalLink(
            @RequestParam String productName
    ) {
        return naverShoppingService
                .findLowestPriceItem(productName)
                .getLink();
    }
}
