package com.kdt.fridge.ingredient.service;

import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.ingredient.dto.IngredientResponse;
import com.kdt.fridge.ingredient.entity.Ingredient;
import com.kdt.fridge.ingredient.repository.IngredientConsumptionRequestRepository;
import com.kdt.fridge.ingredient.repository.IngredientConsumptionRequestRepository.CompletedRequest;
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
import static org.mockito.Mockito.times;
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

    @Mock
    private IngredientConsumptionRequestRepository consumptionRequestRepository;

    private IngredientService ingredientService;
    private Ingredient ingredient;

    @BeforeEach
    void setUp() {
        ingredientService = new IngredientService(ingredientRepository, jdbcTemplate, consumptionRequestRepository);

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
                ingredientService.consume(GROUP_ID, INGREDIENT_ID, USER_ID, 2L, null);

        assertEquals(3L, response.quantity());
        assertEquals("STORED", response.status());
        verifyConsumptionHistory(-2L);
    }

    @Test
    void 전량_소비하면_잔량이_0이고_상태가_소비_완료로_바뀐다() {
        IngredientResponse response =
                ingredientService.consume(GROUP_ID, INGREDIENT_ID, USER_ID, 5L, null);

        assertEquals(0L, response.quantity());
        assertEquals("CONSUMED", response.status());
        verifyConsumptionHistory(-5L);
    }

    @Test
    void 잔량보다_많이_소비하면_수량이_유지되고_이력을_저장하지_않는다() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> ingredientService.consume(GROUP_ID, INGREDIENT_ID, USER_ID, 6L, null)
        );

        assertEquals(ErrorCode.INGREDIENT_INSUFFICIENT_QUANTITY, exception.getErrorCode());
        assertEquals(5L, ingredient.getQuantity());
        assertEquals("STORED", ingredient.getStatus());

        verify(jdbcTemplate, never()).update(
                contains("INSERT INTO ingredient_histories"),
                any(Object[].class)
        );
    }

    @Test
    void 같은_키를_재시도하면_소비_완료_상태에서도_첫_응답을_재사용한다() {
        String key = "egg-001";
        when(consumptionRequestRepository.find(GROUP_ID, INGREDIENT_ID, USER_ID, key))
                .thenReturn(Optional.empty());

        IngredientResponse first = ingredientService.consume(GROUP_ID, INGREDIENT_ID, USER_ID, 5L, key);
        when(consumptionRequestRepository.find(GROUP_ID, INGREDIENT_ID, USER_ID, key))
                .thenReturn(Optional.of(new CompletedRequest(5L, first)));

        IngredientResponse replay = ingredientService.consume(GROUP_ID, INGREDIENT_ID, USER_ID, 5L, key);

        assertEquals(first, replay);
        assertEquals("CONSUMED", replay.status());
        verifyConsumptionHistory(-5L);
        verify(consumptionRequestRepository, times(1)).save(GROUP_ID, INGREDIENT_ID, USER_ID, key, 5L, first);
    }

    @Test
    void 이미_사용한_키에_다른_수량을_보내면_재고를_변경하지_않는다() {
        String key = "egg-001";
        when(consumptionRequestRepository.find(GROUP_ID, INGREDIENT_ID, USER_ID, key))
                .thenReturn(Optional.of(new CompletedRequest(2L, IngredientResponse.from(ingredient))));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> ingredientService.consume(GROUP_ID, INGREDIENT_ID, USER_ID, 1L, key)
        );

        assertEquals(ErrorCode.INGREDIENT_IDEMPOTENCY_KEY_CONFLICT, exception.getErrorCode());
        assertEquals(5L, ingredient.getQuantity());
        verify(jdbcTemplate, never()).update(
                contains("INSERT INTO ingredient_histories"),
                any(Object[].class)
        );
        verify(consumptionRequestRepository, never()).save(
                eq(GROUP_ID), eq(INGREDIENT_ID), eq(USER_ID), eq(key), eq(1L), any(IngredientResponse.class)
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
