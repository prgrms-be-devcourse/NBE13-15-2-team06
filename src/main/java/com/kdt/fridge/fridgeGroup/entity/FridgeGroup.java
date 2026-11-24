package com.kdt.fridge.fridgeGroup.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "fridge_groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FridgeGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "invite_code", nullable = false, length = 20, unique = true)
    private String inviteCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public FridgeGroup(String name, String inviteCode) {
        this.name = name;
        this.inviteCode = inviteCode;
    }

    public void updateName(String name) {
        this.name = name;
    }
}
