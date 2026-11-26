package com.kdt.fridge.histories.controller;

import com.kdt.fridge.global.response.ApiResponse;
import com.kdt.fridge.histories.dto.IngredientHistoryResponse;
import com.kdt.fridge.histories.service.IngredientHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class IngredientHistoryController {

    private final IngredientHistoryService ingredientHistoryService;

    // 냉장고의 변경 이력 조회 (구성원만)
    @GetMapping("/{groupId}/histories")
    public ApiResponse<List<IngredientHistoryResponse>> getHistories(@AuthenticationPrincipal Long userId,
                                                                     @PathVariable Long groupId) {
        List<IngredientHistoryResponse> response =
                ingredientHistoryService.getHistories(userId, groupId);
        return ApiResponse.of(HttpStatus.OK, "변경 이력을 조회했습니다", response);
    }
}