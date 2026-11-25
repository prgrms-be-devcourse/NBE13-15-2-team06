package com.kdt.fridge.ingredient.controller;

import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.ingredient.dto.IngredientCreateRequest;
import com.kdt.fridge.ingredient.dto.IngredientResponse;
import com.kdt.fridge.ingredient.service.IngredientService;
import com.kdt.fridge.ingredient.dto.IngredientUpdateRequest;
import com.kdt.fridge.ingredient.dto.IngredientConsumeRequest;
import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.response.ApiResponse;
import com.kdt.fridge.ingredient.dto.IngredientStatusUpdateRequest;
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
            @AuthenticationPrincipal IngredientUserPrincipal principal,
            @Valid @RequestBody IngredientCreateRequest request
    ) {
        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."
            );
        }

        return ingredientService.create(groupId, principal.userId(), request);
    }
    @GetMapping
    public List<IngredientResponse>findAll(
            @PathVariable("groupId")long groupId,
            @AuthenticationPrincipal IngredientUserPrincipal principal
    ){
        if(principal == null){
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,"로그인이 필요합니다."
            );
        }
        return ingredientService.findAll(groupId,principal.userId());
    }
    @GetMapping("/{ingredientId}")
    public IngredientResponse getDetail(
            @PathVariable("groupId") long groupId,
            @PathVariable("ingredientId") long ingredientId,
            @AuthenticationPrincipal IngredientUserPrincipal principal
    ) {
        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,"로그인이 필요합니다."
            );
        }
        return ingredientService.getDetail(groupId,ingredientId,principal.userId());
    }
    @PatchMapping("/{ingredientId}")
    public IngredientResponse update(
            @PathVariable("groupId")long groupId,
            @PathVariable("ingredientId")long ingredientId,
            @AuthenticationPrincipal IngredientUserPrincipal principal,
            @Valid @RequestBody IngredientUpdateRequest request
    ) {
        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."
            );
        }
        return ingredientService.update(groupId,ingredientId,principal.userId(),request);
    }
    @PostMapping("/{ingredientId}consumptions")
    public ApiResponse<IngredientResponse> consume(
            @PathVariable("groupId") long groupId,
            @PathVariable("ingrdientid") long ingredientId,
            @AuthenticationPrincipal IngredientUserPrincipal principal,
            @Valid @RequestBody IngredientConsumeRequest request
    ) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        IngredientResponse response = ingredientService.consume(
                groupId,ingredientId,principal.userId(),request.quantity()
        );
        return ApiResponse.of(HttpStatus.OK,"식재료를 소비했습니다.",response);
    }
    @PatchMapping("/{ingredientId}/status")
    public ApiResponse<IngredientResponse> updateStatus(
            @PathVariable("groupId") long groupId,
            @PathVariable("ingredientId") long ingredientId,
            @AuthenticationPrincipal IngredientUserPrincipal principal,
            @Valid @RequestBody IngredientStatusUpdateRequest request
    ) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        IngredientResponse response = ingredientService.updateStatus(
                groupId,
                ingredientId,
                principal.userId(),
                request.status()
        );

        return ApiResponse.of(HttpStatus.OK, "식재료 상태를 변경했습니다.", response);
    }
}