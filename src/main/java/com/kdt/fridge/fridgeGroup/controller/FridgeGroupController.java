package com.kdt.fridge.fridgeGroup.controller;

import com.kdt.fridge.fridgeGroup.dto.FridgeCreateRequest;
import com.kdt.fridge.fridgeGroup.dto.FridgeResponse;
import com.kdt.fridge.fridgeGroup.dto.FridgeSummaryResponse;
import com.kdt.fridge.fridgeGroup.dto.FridgeUpdateRequest;
import com.kdt.fridge.fridgeGroup.dto.InviteCodeResponse;
import com.kdt.fridge.fridgeGroup.service.FridgeGroupService;
import com.kdt.fridge.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fridges")
@Tag(name = "Fridge", description = "냉장고 관리")
public class FridgeGroupController {

    private final FridgeGroupService fridgeGroupService;

    // 냉장고 생성
    @Operation(summary = "냉장고 생성",description = "냉장고 운영자 권한 부여, 초대 코드 발급")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FridgeResponse> create(@AuthenticationPrincipal Long userId,
                                              @Valid @RequestBody FridgeCreateRequest request) {
        FridgeResponse response = fridgeGroupService.create(userId, request);
        return ApiResponse.of(HttpStatus.CREATED, "냉장고가 생성되었습니다", response);
    }

    // 내 냉장고 목록
    @Operation(summary = "냉장고 목록 조회", description = "유저가 소속 되어있는 냉장고 조회")
    @GetMapping
    public ApiResponse<List<FridgeSummaryResponse>> getMyFridges(@AuthenticationPrincipal Long userId) {
        List<FridgeSummaryResponse> response = fridgeGroupService.getMyAllFridges(userId);
        return ApiResponse.of(HttpStatus.OK, "내 냉장고 목록을 조회했습니다", response);
    }

    // 냉장고 상세
    @Operation(summary = "냉장고 상세 조회", description = "소속된 냉장고의 정보, 내 역할, 멤버 수 조회. 멤버가 아니면 조회 불가")
    @GetMapping("/{fridgeId}")
    public ApiResponse<FridgeResponse> getFridge(@AuthenticationPrincipal Long userId,
                                                 @PathVariable Long fridgeId) {
        FridgeResponse response = fridgeGroupService.getFridge(userId, fridgeId);
        return ApiResponse.of(HttpStatus.OK, "냉장고를 조회했습니다", response);
    }

    // 냉장고 이름 수정 (ADMIN)
    @Operation(summary = "냉장고 이름 수정", description = "운영자(ADMIN)만 가능")
    @PatchMapping("/{fridgeId}")
    public ApiResponse<FridgeResponse> updateName(@AuthenticationPrincipal Long userId,
                                                  @PathVariable Long fridgeId,
                                                  @Valid @RequestBody FridgeUpdateRequest request) {
        FridgeResponse response = fridgeGroupService.updateName(userId, fridgeId, request);
        return ApiResponse.of(HttpStatus.OK, "냉장고 이름이 수정되었습니다", response);
    }

    // 초대 코드 조회 (ADMIN)
    @Operation(summary = "초대 코드 조회", description = "운영자(ADMIN)만 가능")
    @GetMapping("/{fridgeId}/invite-code")
    public ApiResponse<InviteCodeResponse> getInviteCode(@AuthenticationPrincipal Long userId,
                                                         @PathVariable Long fridgeId) {
        InviteCodeResponse response = fridgeGroupService.getInviteCode(userId, fridgeId);
        return ApiResponse.of(HttpStatus.OK, "초대 코드를 조회했습니다", response);
    }
}
