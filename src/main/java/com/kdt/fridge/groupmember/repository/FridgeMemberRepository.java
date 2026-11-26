package com.kdt.fridge.groupmember.repository;

import com.kdt.fridge.groupmember.entity.FridgeMember;
import com.kdt.fridge.groupmember.entity.MemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FridgeMemberRepository extends JpaRepository<FridgeMember, Long> {

    // 구성원 조회
    List<FridgeMember> findAllByFridgeId(Long fridgeId);

    // 참여 신청 : 이미 있는지 없는지 확인
    boolean existsByFridgeIdAndUserId(Long fridgeId, Long userId);

    // 관리자 기능
    Optional<FridgeMember> findByFridgeIdAndUserId(Long fridgeId, Long userId);

    // 관리자 몇 명인지 확인
    long countByFridgeIdAndRole(Long fridgeId, MemberRole role);

    //내가 속한 냉장고 목록
    List<FridgeMember> findByUserId(Long userId);

    //냉장고의 구성원 수
    long countByFridgeId(Long fridgeId);
}