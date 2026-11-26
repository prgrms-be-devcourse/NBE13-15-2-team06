package com.kdt.fridge.fridgeGroup.controller;

import com.kdt.fridge.fridgeGroup.dto.FridgeCreateRequest;
import com.kdt.fridge.fridgeGroup.dto.FridgeResponse;
import com.kdt.fridge.fridgeGroup.dto.FridgeSummaryResponse;
import com.kdt.fridge.fridgeGroup.dto.FridgeUpdateRequest;
import com.kdt.fridge.fridgeGroup.dto.InviteCodeResponse;
import com.kdt.fridge.fridgeGroup.service.FridgeGroupService;
import com.kdt.fridge.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fridges")
public class FridgeGroupController {

    private final FridgeGroupService fridgeGroupService;

    // 냉장고 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FridgeResponse> create(@Valid @RequestBody FridgeCreateRequest request) {
        FridgeResponse response = fridgeGroupService.create(currentUserId(),request);
        return ApiResponse.of(HttpStatus.CREATED,"냉장고가 생성되었습니다", response);
    }

    // 내 냉장고 목록
    @GetMapping
    public ApiResponse<List<FridgeSummaryResponse>> getMyFridges() {
        List<FridgeSummaryResponse> response = fridgeGroupService.getMyAllFridges(currentUserId());
        return ApiResponse.of(HttpStatus.OK, "내 냉장고 목록을 조회했습니다", response);
    }

    // 냉장고 상세
    @GetMapping("/{fridgeId}")
    public ApiResponse<FridgeResponse> getFridge(@PathVariable Long fridgeId) {
        FridgeResponse response = fridgeGroupService.getFridge(currentUserId(), fridgeId);
        return ApiResponse.of(HttpStatus.OK, "냉장고를 조회했습니다", response);
    }

    // 냉장고 이름 수정 (ADMIN)
    @PatchMapping("/{fridgeId}")
    public ApiResponse<FridgeResponse> updateName(@PathVariable Long fridgeId,
                                                  @Valid @RequestBody FridgeUpdateRequest request) {
        FridgeResponse response = fridgeGroupService.updateName(currentUserId(), fridgeId, request);
        return ApiResponse.of(HttpStatus.OK, "냉장고 이름이 수정되었습니다", response);
    }

    // 초대 코드 조회 (ADMIN)
    @GetMapping("/{fridgeId}/invite-code")
    public ApiResponse<InviteCodeResponse> getInviteCode(@PathVariable Long fridgeId) {
        InviteCodeResponse response = fridgeGroupService.getInviteCode(currentUserId(), fridgeId);
        return ApiResponse.of(HttpStatus.OK, "초대 코드를 조회했습니다", response);
    }

    // TODO: 인증(JWT) 구조가 정해지면 이 메서드를 지우고 각 메서드에서 @AuthenticationPrincipal로 받는다.
    private Long currentUserId() {
        throw new UnsupportedOperationException("인증 연동 전");
    }
}
