package com.kdt.fridge.fridgeGroup.repository;

import com.kdt.fridge.fridgeGroup.entity.FridgeGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FridgeGroupRepository extends JpaRepository<FridgeGroup,Long> {

    Optional<FridgeGroup> findByInviteCode(String inviteCode);
}
