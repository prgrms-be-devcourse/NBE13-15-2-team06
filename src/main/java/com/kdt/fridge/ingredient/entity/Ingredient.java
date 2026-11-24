package com.kdt.fridge.ingredient.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "ingredients")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(length = 50)
    private String name;

    private Long quantity;

    @Column(length = 10)
    private String unit;

    @Column(name = "storage_type", length = 20)
    private String storageType;

    @Column(name = "is_shared")
    private boolean shared;

    @Column(name = "purchased_at")
    private LocalDate purchasedAt;

    @Column(name  = "expires_at")
    private LocalDate expiresAt;

    @Column(name = "notify_days_before")
    private Integer notifyDaysBefore;

    @Column(length = 20)
    private String status;

    @Column(length = 255)
    private String memo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Ingredient(
            Long groupId,
            Long ownerId,
            String name,
            Long quantity,
            String unit,
            String storageType,
            Boolean shared,
            LocalDate purchasedAt,
            LocalDate expiresAt,
            Integer notifyDaysBefore,
            String status,
            String memo,
            LocalDateTime createdAt
    ){
        this.groupId = groupId;
        this.ownerId = ownerId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.storageType = storageType;
        this.shared = shared;
        this.purchasedAt = purchasedAt;
        this.expiresAt = expiresAt;
        this.notifyDaysBefore = notifyDaysBefore;
        this.status = status;
        this.memo = memo;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;

    }

}
