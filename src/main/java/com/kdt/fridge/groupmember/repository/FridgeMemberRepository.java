package com.kdt.fridge.groupmember.repository;

import com.kdt.fridge.groupmember.entity.FridgeMember;
import com.kdt.fridge.groupmember.role.MemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FridgeMemberRepository extends JpaRepository<FridgeMember, Long> {
    List<FridgeMember> findAllByFridgeId(Long fridgeId);

    boolean existsByFridgeIdAndUserId(Long fridgeId, Long userId);

    Optional<FridgeMember> findByFridgeIdAndUserId(Long fridgeId, Long userId);

    Optional<FridgeMember> findByIdAndFridgeId(Long memberId, Long fridgeId);

    long countByFridgeIdAndRole(Long fridgeId, MemberRole role);
}
