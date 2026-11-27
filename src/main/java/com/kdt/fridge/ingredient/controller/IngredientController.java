package com.kdt.fridge.ingredient.controller;

import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.global.response.ApiResponse;
import com.kdt.fridge.ingredient.dto.IngredientConsumeRequest;
import com.kdt.fridge.ingredient.dto.IngredientCreateRequest;
import com.kdt.fridge.ingredient.dto.IngredientHistoryResponse;
import com.kdt.fridge.ingredient.dto.IngredientMemoUpdateRequest;
import com.kdt.fridge.ingredient.dto.IngredientOwnershipUpdateRequest;
import com.kdt.fridge.ingredient.dto.IngredientResponse;
import com.kdt.fridge.ingredient.dto.IngredientStatusUpdateRequest;
import com.kdt.fridge.ingredient.dto.IngredientUpdateRequest;
import com.kdt.fridge.ingredient.service.IngredientService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    // 식재료 등록 API — POST /api/v1/groups/{groupId}/ingredients
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<IngredientResponse> create(
            @PathVariable("groupId") long groupId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody IngredientCreateRequest request
    ) {
        requireAuthenticatedUser(userId);

        IngredientResponse response = ingredientService.create(groupId, userId, request);
        return ApiResponse.of(HttpStatus.CREATED, "식재료를 등록했습니다.", response);
    }

    // 식재료 목록 조회 API — GET /api/v1/groups/{groupId}/ingredients
    // 이름 검색(keyword), 유통기한 필터(expiringWithinDays), 정렬(sort)을 선택할 수 있습니다.
    @GetMapping
    public ApiResponse<List<IngredientResponse>> findAll(
            @PathVariable("groupId") long groupId,
            @AuthenticationPrincipal Long userId,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "expiringWithinDays", required = false) Integer expiringWithinDays,
            @RequestParam(name = "sort", required = false) String sort
    ) {
        requireAuthenticatedUser(userId);

        List<IngredientResponse> responses = ingredientService.findAll(
                groupId, userId, keyword, expiringWithinDays, sort
        );
        return ApiResponse.of(HttpStatus.OK, "식재료 목록을 조회했습니다.", responses);
    }

    // 식재료 상세 조회 API — GET /api/v1/groups/{groupId}/ingredients/{ingredientId}
    @GetMapping("/{ingredientId}")
    public ApiResponse<IngredientResponse> getDetail(
            @PathVariable("groupId") long groupId,
            @PathVariable("ingredientId") long ingredientId,
            @AuthenticationPrincipal Long userId
    ) {
        requireAuthenticatedUser(userId);

        IngredientResponse response = ingredientService.getDetail(groupId, ingredientId, userId);
        return ApiResponse.of(HttpStatus.OK, "식재료를 조회했습니다.", response);
    }

    // 식재료 기본정보 수정 API — PATCH /api/v1/groups/{groupId}/ingredients/{ingredientId}
    @PatchMapping("/{ingredientId}")
    public ApiResponse<IngredientResponse> update(
            @PathVariable("groupId") long groupId,
            @PathVariable("ingredientId") long ingredientId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody IngredientUpdateRequest request
    ) {
        requireAuthenticatedUser(userId);

        IngredientResponse response = ingredientService.update(
                groupId, ingredientId, userId, request
        );
        return ApiResponse.of(HttpStatus.OK, "식재료 기본정보를 수정했습니다.", response);
    }

    // 식재료 일부 소비 API — POST /api/v1/groups/{groupId}/ingredients/{ingredientId}/consumptions
    // 요청 본문의 quantity를 소비할 수량으로 서비스에 전달합니다.
    @PostMapping("/{ingredientId}/consumptions")
    public ApiResponse<IngredientResponse> consume(
            @PathVariable("groupId") long groupId,
            @PathVariable("ingredientId") long ingredientId,
            @AuthenticationPrincipal Long userId,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody IngredientConsumeRequest request
    ) {
        requireAuthenticatedUser(userId);

        IngredientResponse response = ingredientService.consume(
                groupId, ingredientId, userId, request.quantity(), idempotencyKey
        );
        return ApiResponse.of(HttpStatus.OK, "식재료를 소비했습니다.", response);
    }

    // 식재료 상태 변경 API — PATCH /api/v1/groups/{groupId}/ingredients/{ingredientId}/status
    @PatchMapping("/{ingredientId}/status")
    public ApiResponse<IngredientResponse> updateStatus(
            @PathVariable("groupId") long groupId,
            @PathVariable("ingredientId") long ingredientId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody IngredientStatusUpdateRequest request
    ) {
        requireAuthenticatedUser(userId);

        IngredientResponse response = ingredientService.updateStatus(
                groupId, ingredientId, userId, request.status()
        );
        return ApiResponse.of(HttpStatus.OK, "식재료 상태를 변경했습니다.", response);
    }

    // 식재료 변경 이력 조회 API — GET /api/v1/groups/{groupId}/ingredients/{ingredientId}/histories
    @GetMapping("/{ingredientId}/histories")
    public ApiResponse<List<IngredientHistoryResponse>> getHistories(
            @PathVariable("groupId") long groupId,
            @PathVariable("ingredientId") long ingredientId,
            @AuthenticationPrincipal Long userId
    ) {
        requireAuthenticatedUser(userId);

        List<IngredientHistoryResponse> histories = ingredientService.getHistories(
                groupId, ingredientId, userId
        );
        return ApiResponse.of(HttpStatus.OK, "식재료 변경 이력을 조회했습니다.", histories);
    }

    // 식재료 메모 수정 API — PATCH /api/v1/groups/{groupId}/ingredients/{ingredientId}/memo
    @PatchMapping("/{ingredientId}/memo")
    public ApiResponse<IngredientResponse> updateMemo(
            @PathVariable("groupId") long groupId,
            @PathVariable("ingredientId") long ingredientId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody IngredientMemoUpdateRequest request
    ) {
        requireAuthenticatedUser(userId);

        IngredientResponse response = ingredientService.updateMemo(
                groupId, ingredientId, userId, request.memo()
        );
        return ApiResponse.of(HttpStatus.OK, "식재료 메모를 수정했습니다.", response);
    }

    // 식재료 공동 여부 변경 API — PATCH /api/v1/groups/{groupId}/ingredients/{ingredientId}/ownership
    @PatchMapping("/{ingredientId}/ownership")
    public ApiResponse<IngredientResponse> updateOwnership(
            @PathVariable("groupId") long groupId,
            @PathVariable("ingredientId") long ingredientId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody IngredientOwnershipUpdateRequest request
    ) {
        requireAuthenticatedUser(userId);

        IngredientResponse response = ingredientService.updateOwnership(
                groupId, ingredientId, userId, request.shared()
        );
        return ApiResponse.of(HttpStatus.OK, "식재료 공동 여부를 변경했습니다.", response);
    }

    // 위 API들이 서비스 호출 전에 공통으로 사용하는 로그인 확인 메서드입니다.
    // 사용자 ID가 없으면 팀 공통 예외를 던집니다.
    private void requireAuthenticatedUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}