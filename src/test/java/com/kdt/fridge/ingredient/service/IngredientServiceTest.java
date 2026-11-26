package com.kdt.fridge.ingredient.service;

import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.ingredient.dto.IngredientResponse;
import com.kdt.fridge.ingredient.entity.Ingredient;
import com.kdt.fridge.ingredient.repository.IngredientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    private static final long GROUP_ID = 3L;
    private static final long INGREDIENT_ID = 15L;
    private static final long USER_ID = 7L;
    private static final long OWNER_ID = 8L;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private IngredientService ingredientService;
    private Ingredient ingredient;

    @BeforeEach
    void setUp() {
        ingredientService = new IngredientService(ingredientRepository, jdbcTemplate);

        ingredient = new Ingredient(
                GROUP_ID, OWNER_ID, "달걀", 5L, "개", "냉장",
                true, null, null, null, "STORED", null,
                LocalDateTime.of(2026, 11, 26, 10, 0)
        );

        when(jdbcTemplate.queryForObject(
                contains("FROM group_members"),
                eq(Boolean.class),
                eq(GROUP_ID),
                eq(USER_ID)
        )).thenReturn(true);

        when(ingredientRepository.findForUpdate(INGREDIENT_ID, GROUP_ID))
                .thenReturn(Optional.of(ingredient));
    }

    @Test
    void 일부_소비하면_잔량이_줄고_차감량을_이력에_전달한다() {
        IngredientResponse response =
                ingredientService.consume(GROUP_ID, INGREDIENT_ID, USER_ID, 2L);

        assertEquals(3L, response.quantity());
        assertEquals("STORED", response.status());
        verifyConsumptionHistory(-2L);
    }

    @Test
    void 전량_소비하면_잔량이_0이고_상태가_소비_완료로_바뀐다() {
        IngredientResponse response =
                ingredientService.consume(GROUP_ID, INGREDIENT_ID, USER_ID, 5L);

        assertEquals(0L, response.quantity());
        assertEquals("CONSUMED", response.status());
        verifyConsumptionHistory(-5L);
    }

    @Test
    void 잔량보다_많이_소비하면_수량이_유지되고_이력을_저장하지_않는다() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> ingredientService.consume(GROUP_ID, INGREDIENT_ID, USER_ID, 6L)
        );

        assertEquals(ErrorCode.INGREDIENT_INSUFFICIENT_QUANTITY, exception.getErrorCode());
        assertEquals(5L, ingredient.getQuantity());
        assertEquals("STORED", ingredient.getStatus());

        verify(jdbcTemplate, never()).update(
                contains("INSERT INTO ingredient_histories"),
                any(Object[].class)
        );
    }

    private void verifyConsumptionHistory(long quantityChange) {
        verify(jdbcTemplate).update(
                contains("INSERT INTO ingredient_histories"),
                eq(INGREDIENT_ID),
                eq(USER_ID),
                eq("CONSUME"),
                eq(quantityChange),
                any(LocalDateTime.class)
        );
    }
}
