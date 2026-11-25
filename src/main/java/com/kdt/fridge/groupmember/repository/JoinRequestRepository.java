package com.kdt.fridge.groupmember.repository;

import com.kdt.fridge.groupmember.entity.JoinRequest;
import com.kdt.fridge.groupmember.role.JoinRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 참여 신청(join_requests 테이블)을 DB 에서 읽고 쓰는 인터페이스입니다.
 *
 * [레포지토리(Repository)란?]
 * DB 에 접근하는 코드를 모아 둔 계층입니다. 서비스는 SQL 을 직접 쓰지 않고 레포지토리를 통해 데이터를 다룹니다.
 *
 * [JpaRepository<JoinRequest, Long>]
 * Spring Data JPA 가 제공하는 인터페이스를 상속(extends)한 것입니다.
 *   - JoinRequest: 다룰 엔티티
 *   - Long: 그 엔티티의 기본키(id) 타입
 * 상속만 해도 save(저장), findById(번호로 조회), delete(삭제) 같은 기본 기능이 자동으로 생깁니다.
 * 구현 클래스는 직접 만들 필요가 없습니다. (Spring 이 실행 시점에 대신 만들어 줌)
 *
 * [메서드 이름으로 쿼리 만들기]
 * 정해진 규칙대로 메서드 이름을 지으면 Spring 이 SQL 을 자동으로 만들어 줍니다.
 *   findBy / existsBy / findAllBy : 조회 / 존재 여부 / 여러 건 조회
 *   And     : 조건을 "그리고"로 연결
 *   OrderBy : 정렬 기준 지정 (뒤에 Asc = 오름차순, Desc = 내림차순)
 */
public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {

    // 이 회원이 이 냉장고에 해당 상태의 신청을 이미 갖고 있는지 true / false 로 확인 (중복 신청 막기용)
    // 만들어지는 SQL: group_id = ? AND user_id = ? AND status = ? 인 행이 있는지 확인
    boolean existsByFridgeIdAndUserIdAndStatus(Long fridgeId, Long userId, JoinRequestStatus status);

    // 이 냉장고의 특정 상태 신청 목록을, 신청 시각(requestedAt)이 오래된 순서로 조회
    // 만들어지는 SQL: WHERE group_id = ? AND status = ? ORDER BY requested_at ASC
    // Asc(Ascending) = 오름차순, 즉 작은 값(오래된 시각)이 먼저 나옴
    List<JoinRequest> findAllByFridgeIdAndStatusOrderByRequestedAtAsc(Long fridgeId, JoinRequestStatus status);

    // 신청 번호(id)로 찾되, 이 냉장고의 신청인지도 같이 확인한다. (수락, 거절에서 사용)
    // 다른 냉장고의 신청 번호를 넣어도 찾아지지 않게 막아 주는 보안 장치 역할
    // Optional: "값이 있을 수도, 없을 수도 있다"를 표현하는 상자 (없으면 비어 있는 Optional)
    Optional<JoinRequest> findByIdAndFridgeId(Long requestId, Long fridgeId);
}
