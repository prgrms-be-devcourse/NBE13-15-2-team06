package com.kdt.fridge.ingredient.service;

import com.kdt.fridge.ingredient.dto.IngredientCreateRequest;
import com.kdt.fridge.ingredient.dto.IngredientResponse;
import com.kdt.fridge.ingredient.entity.Ingredient;
import com.kdt.fridge.ingredient.repository.IngredientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final JdbcTemplate jdbcTemplate;

    public IngredientService(
            IngredientRepository ingredientRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.ingredientRepository = ingredientRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public IngredientResponse create(
            long groupId,
            long userId,
            IngredientCreateRequest request
    ) {
        Boolean member = jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM group_members
                    WHERE group_id = ? AND user_id = ?
                )
                """, Boolean.class, groupId, userId);

        if (!Boolean.TRUE.equals(member)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "냉장고 그룹 구성원이 아닙니다."
            );
        }

        Ingredient ingredient = new Ingredient(
                groupId,
                userId,
                request.name().strip(),
                request.quantity(),
                request.unit(),
                request.storageType(),
                Boolean.TRUE.equals(request.shared()),
                request.purchasedAt(),
                request.expiresAt(),
                request.notifyDaysBefore(),
                "STORED",
                request.memo(),
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );

        Ingredient savedIngredient = ingredientRepository.save(ingredient);
        return IngredientResponse.from(savedIngredient);
    }
    @Transactional(readOnly = true)
    public List<IngredientResponse> findAll(long groupId, long userId) {
        requireGroupMember(groupId, userId);

        List<Ingredient> ingredients =
                ingredientRepository.findVisibleIngredients(groupId, userId);

        List<IngredientResponse> responses = new ArrayList<>(ingredients.size());

        for (Ingredient ingredient : ingredients) {
            IngredientResponse response = IngredientResponse.from(ingredient);
            responses.add(response);
        }

        return responses;
    }

    private void requireGroupMember(long groupId, long userId) {
        Boolean member = jdbcTemplate.queryForObject("""
            SELECT EXISTS (
                SELECT 1 FROM group_members
                WHERE group_id = ? AND user_id = ?
            )
            """, Boolean.class, groupId, userId);

        if (!Boolean.TRUE.equals(member)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "냉장고 그룹 구성원이 아닙니다."
            );
        }
    }
}