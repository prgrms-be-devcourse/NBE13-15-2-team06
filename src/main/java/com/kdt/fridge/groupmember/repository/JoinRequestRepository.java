package com.kdt.fridge.groupmember.repository;

import com.kdt.fridge.groupmember.entity.JoinRequest;
import com.kdt.fridge.groupmember.entity.JoinRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 참여 신청(JoinRequest) 데이터 접근 리포지토리. 메서드는 Spring Data JPA가 이름으로 쿼리를 생성한다.
 */
public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    /**
     * 특정 냉장고에 대해 해당 사용자의 특정 상태 신청이 있는지 확인한다.
     * 참여 신청 시 대기(PENDING) 중복 신청을 막는 데 쓴다.
     */
    boolean existsByFridgeIdAndUserIdAndStatus(Long fridgeId, Long userId, JoinRequestStatus status);

    /**
     * 특정 냉장고의 특정 상태 신청들을 신청 시각 오름차순(오래된 순)으로 조회한다.
     * 운영자의 대기 신청 목록 화면에 사용한다.
     */
    List<JoinRequest> findAllByFridgeIdAndStatusOrderByRequestedAtAsc(Long fridgeId, JoinRequestStatus status);

    /**
     * 신청 ID로 조회하되 지정한 냉장고에 속한 신청만 찾는다.
     * 다른 냉장고의 신청 ID를 넣어 처리하는 것을 막는다.
     */
    Optional<JoinRequest> findByIdAndFridgeId(Long requestId, Long fridgeId);
}
