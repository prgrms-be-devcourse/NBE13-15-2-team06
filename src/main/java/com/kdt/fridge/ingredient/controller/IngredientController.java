package com.kdt.fridge.ingredient.controller;

import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.ingredient.dto.IngredientCreateRequest;
import com.kdt.fridge.ingredient.dto.IngredientResponse;
import com.kdt.fridge.ingredient.service.IngredientService;
import com.kdt.fridge.ingredient.dto.IngredientUpdateRequest;
import com.kdt.fridge.ingredient.dto.IngredientConsumeRequest;
import com.kdt.fridge.ingredient.dto.IngredientHistoryResponse;
import com.kdt.fridge.ingredient.dto.IngredientStatusUpdateRequest;
import com.kdt.fridge.ingredient.dto.IngredientMemoUpdateRequest;
import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IngredientResponse create(
            @PathVariable("groupId") long groupId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody IngredientCreateRequest request
    ) {
        if (userId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."
            );
        }

        return ingredientService.create(groupId,userId,request);
    }
    @GetMapping
    public List<IngredientResponse>findAll(
            @PathVariable("groupId")long groupId,
            @AuthenticationPrincipal Long userId,
            @RequestParam(name = "keyword",required = false)String keyword
    ){
        if(userId == null){
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,"로그인이 필요합니다."
            );
        }
        return ingredientService.findAll(groupId,userId,keyword);
    }
    @GetMapping("/{ingredientId}")
    public IngredientResponse getDetail(
            @PathVariable("groupId") long groupId,
            @PathVariable("ingredientId") long ingredientId,
            @AuthenticationPrincipal Long userId
    ) {
        if (userId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,"로그인이 필요합니다."
            );
        }
        return ingredientService.getDetail(groupId,ingredientId,userId);
    }
    @PatchMapping("/{ingredientId}")
    public IngredientResponse update(
            @PathVariable("groupId")long groupId,
            @PathVariable("ingredientId")long ingredientId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody IngredientUpdateRequest request
    ) {
        if (userId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."
            );
        }
        return ingredientService.update(groupId,ingredientId,userId,request);
    }
    @PostMapping("/{ingredientId}/consumptions")
    public ApiResponse<IngredientResponse> consume(
            @PathVariable("groupId") long groupId,
            @PathVariable("ingrdientId") long ingredientId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody IngredientConsumeRequest request
    ) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        IngredientResponse response = ingredientService.consume(
                groupId,ingredientId,userId,request.quantity()
        );
        return ApiResponse.of(HttpStatus.OK,"식재료를 소비했습니다.",response);
    }
    @PatchMapping("/{ingredientId}/status")
    public ApiResponse<IngredientResponse> updateStatus(
            @PathVariable("groupId") long groupId,
            @PathVariable("ingredientId") long ingredientId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody IngredientStatusUpdateRequest request
    ) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        IngredientResponse response = ingredientService.updateStatus(
                groupId,
                ingredientId,
                userId,
                request.status()
        );

        return ApiResponse.of(HttpStatus.OK, "식재료 상태를 변경했습니다.", response);
    }
    @GetMapping("/{ingredientId}/histories")
    public ApiResponse<List<IngredientHistoryResponse>> getHistories(
            @PathVariable("groupId")long groupId,
            @PathVariable("ingredientId")long ingredientId,
            @AuthenticationPrincipal Long userId
    ){
        if (userId == null){
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        List<IngredientHistoryResponse> histories =
                ingredientService.getHistories(groupId,ingredientId,userId);
        return ApiResponse.of(HttpStatus.OK,"식재료 변경 이력을 조회했습니다.",histories);
    }
    @PatchMapping("/{ingredientId}/memo")
    public ApiResponse<IngredientResponse> updateMemo(
            @PathVariable("groupId") long groupId,
            @PathVariable("ingredientId") long ingredientId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody IngredientMemoUpdateRequest request
    ) {
        if(userId == null){
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        IngredientResponse response = ingredientService.updateMemo(
                groupId,ingredientId,userId,request.memo()
        );

        return ApiResponse.of(HttpStatus.OK,"식재료 메모를 수정했습니다.",response);
    }
}