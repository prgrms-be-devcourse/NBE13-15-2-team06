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

/**
 * 냉장고 구성원(FridgeMember) 데이터 접근 리포지토리.
 * 이름 기반 조회 메서드와 함께, 구성원 변경 작업의 동시성을 제어하기 위한 냉장고 행 비관적 잠금 쿼리를 제공한다.
 */
public interface FridgeMemberRepository extends JpaRepository<FridgeMember, Long> {
    /**
     * 냉장고의 모든 구성원을 정렬 없이 조회한다.
     */
    List<FridgeMember> findAllByFridgeId(Long fridgeId);

    /**
     * 냉장고의 구성원을 가입 시각 오름차순으로 조회한다. 가입 시각이 같으면 id 순으로 정렬해 결과 순서를 고정한다.
     */
    List<FridgeMember> findAllByFridgeIdOrderByJoinedAtAscIdAsc(Long fridgeId);

    /**
     * 사용자가 해당 냉장고의 구성원인지 확인한다. 이미 구성원인 사람의 중복 신청/수락을 막는 데 쓴다.
     */
    boolean existsByFridgeIdAndUserId(Long fridgeId, Long userId);

    /**
     * 냉장고와 사용자로 구성원을 찾는다. 로그인 사용자의 구성원 여부·역할 확인에 사용한다.
     */
    Optional<FridgeMember> findByFridgeIdAndUserId(Long fridgeId, Long userId);

    /**
     * 구성원 ID로 조회하되 지정한 냉장고 소속인 경우에만 찾는다. 강퇴·권한 변경 대상 조회에 사용한다.
     */
    Optional<FridgeMember> findByIdAndFridgeId(Long memberId, Long fridgeId);

    /**
     * 냉장고에서 특정 역할을 가진 구성원 수를 센다. 운영자가 마지막 한 명인지 판단하는 데 쓴다.
     */
    long countByFridgeIdAndRole(Long fridgeId, MemberRole role);

    /**
     * 냉장고 ID로 냉장고 행에 비관적 쓰기 잠금을 건다 (SELECT ... FOR UPDATE).
     * 구성원 변경 작업(탈퇴·강퇴·권한 변경·신청 처리)을 냉장고 단위로 순서대로 처리해,
     * 두 운영자가 동시에 서로를 강등/탈퇴시켜 운영자가 0명이 되는 식의 경합을 막는다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FridgeGroup f where f.id = :fridgeId")
    Optional<FridgeGroup> lockFridgeById(@Param("fridgeId") Long fridgeId);

    /**
     * 초대 코드로 냉장고 행에 비관적 쓰기 잠금을 걸어 조회한다.
     * 참여 신청 시 같은 냉장고에 대한 동시 신청 중복 검사를 직렬화하기 위해 쓴다.
     * 초대 코드에 해당하는 냉장고가 없으면 빈 Optional을 반환한다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FridgeGroup f where f.inviteCode = :inviteCode")
    Optional<FridgeGroup> lockFridgeByInviteCode(@Param("inviteCode") String inviteCode);

    /**
     * 사용자가 속한 모든 냉장고의 구성원 레코드(내 냉장고 목록)를 조회한다.
     * 냉장고를 fetch join으로 함께 가져와 멤버마다 냉장고를 따로 조회하지 않는다.
     */
    @Query("select m from FridgeMember m join fetch m.fridge where m.user.id = :userId")
    List<FridgeMember> findWithFridgeByUserId(@Param("userId") Long userId);

    /**
     * 냉장고의 전체 구성원 수를 센다. 마지막 남은 구성원이 나갈 때 냉장고를 삭제할지 판단하는 데 쓴다.
     */
    long countByFridgeId(Long fridgeId);

    // 여러 냉장고의 구성원 수를 쿼리 한 번으로 센다. 목록 조회에서 냉장고마다 count를 보내지 않기 위함.
    @Query("select m.fridge.id as fridgeId, count(m) as memberCount "
            + "from FridgeMember m where m.fridge.id in :fridgeIds group by m.fridge.id")
    List<FridgeMemberCount> countByFridgeIds(@Param("fridgeIds") List<Long> fridgeIds);

    interface FridgeMemberCount {
        Long getFridgeId();

        Long getMemberCount();
    }
}