package com.kdt.fridge.ingredient.controller;

import com.kdt.fridge.ingredient.dto.IngredientCreateRequest;
import com.kdt.fridge.ingredient.dto.IngredientResponse;
import com.kdt.fridge.ingredient.service.IngredientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
}