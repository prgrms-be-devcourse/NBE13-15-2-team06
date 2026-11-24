package com.kdt.fridge.ingredient.repository;

import com.kdt.fridge.ingredient.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient,Long> {
}
