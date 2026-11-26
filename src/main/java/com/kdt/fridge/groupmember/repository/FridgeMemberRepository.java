package com.kdt.fridge.groupmember.repository;

import com.kdt.fridge.fridgeGroup.entity.FridgeGroup;
import com.kdt.fridge.groupmember.entity.FridgeMember;
import com.kdt.fridge.groupmember.entity.MemberRole;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FridgeMemberRepository extends JpaRepository<FridgeMember, Long> {
    List<FridgeMember> findAllByFridgeId(Long fridgeId);

    List<FridgeMember> findAllByFridgeIdOrderByJoinedAtAscIdAsc(Long fridgeId);

    boolean existsByFridgeIdAndUserId(Long fridgeId, Long userId);

    Optional<FridgeMember> findByFridgeIdAndUserId(Long fridgeId, Long userId);

    Optional<FridgeMember> findByIdAndFridgeId(Long memberId, Long fridgeId);

    long countByFridgeIdAndRole(Long fridgeId, MemberRole role);

    // 구성원 쓰기 작업을 냉장고 단위로 한 줄로 세우기 위한 냉장고 row 잠금 (SELECT ... FOR UPDATE)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FridgeGroup f where f.id = :fridgeId")
    Optional<FridgeGroup> lockFridgeById(@Param("fridgeId") Long fridgeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FridgeGroup f where f.inviteCode = :inviteCode")
    Optional<FridgeGroup> lockFridgeByInviteCode(@Param("inviteCode") String inviteCode);

    //내가 속한 냉장고 목록
    List<FridgeMember> findByUserId(Long userId);

    //냉장고의 구성원 수
    long countByFridgeId(Long fridgeId);
}