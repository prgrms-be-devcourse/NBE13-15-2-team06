package com.kdt.fridge.histories.entity;

import com.kdt.fridge.ingredient.entity.Ingredient;
import com.kdt.fridge.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ingredient_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IngredientHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    // 변경한 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 30)
    private String action;

    @Column(name = "quantity_change")
    private Long quantityChange;

    @Column(name = "detail", length = 255)
    private String detail;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public IngredientHistory(Ingredient ingredient, User user, String action,
                             Long quantityChange, String detail) {
        this.ingredient = ingredient;
        this.user = user;
        this.action = action;
        this.quantityChange = quantityChange;
        this.detail = detail;
    }
}
