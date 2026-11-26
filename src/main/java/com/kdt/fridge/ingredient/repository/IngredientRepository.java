package com.kdt.fridge.ingredient.repository;

import com.kdt.fridge.ingredient.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient,Long> {
    @Query("""
        SELECT i
        FROM Ingredient i
        WHERE i.groupId = :groupId
          AND (i.shared = true OR i.ownerId = :userId)
        """)
    List<Ingredient> findVisibleIngredients(
            @Param("groupId") long groupId,
            @Param("userId") long userId
    );

    Optional<Ingredient> findByIdAndGroupId(Long id, Long groupId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT i
           FROM Ingredient i
           WHERE i.id = :ingredientId AND i.groupId = :groupId
            """)
    Optional<Ingredient> findForUpdate(
            @Param("ingredientId")long ingredientId,
            @Param("groupId")long groupId
    );

    List<Ingredient> findAllByGroupId(Long groupId,Sort sort);

    List<Ingredient> findAllByGroupIdAndNameContaining(
            Long groupId,String keyword,Sort sort);

    List<Ingredient> findAllByGroupIdAndExpiresAtBetween(
            Long groupId,LocalDate from,LocalDate to,Sort sort
    );

    List<Ingredient> findAllByGroupIdAndNameContainingAndExpiresAtBetween(
            Long groupId,String keyword,LocalDate from,LocalDate to,Sort sort
    );
}
