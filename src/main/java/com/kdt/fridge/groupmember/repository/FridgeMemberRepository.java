package com.kdt.fridge.groupmember.repository;

import com.kdt.fridge.groupmember.entity.FridgeMember;
import com.kdt.fridge.groupmember.role.MemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 구성원(group_members 테이블)을 DB 에서 읽고 쓰는 인터페이스입니다.
 *
 * [레포지토리(Repository)란?]
 * DB 에 접근하는 코드를 모아 둔 계층입니다. 서비스는 SQL 을 직접 쓰지 않고 레포지토리를 통해 데이터를 다룹니다.
 *
 * [JpaRepository<FridgeMember, Long>]
 * Spring Data JPA 가 제공하는 인터페이스를 상속(extends)한 것입니다.
 *   - 첫 번째 타입(FridgeMember): 어떤 엔티티를 다룰지
 *   - 두 번째 타입(Long): 그 엔티티의 기본키(id) 타입
 * 상속만 해도 save(저장), findById(번호로 조회), findAll(전체 조회), delete(삭제) 같은
 * 기본 기능이 자동으로 생깁니다. 구현 클래스는 직접 만들 필요가 없습니다. (Spring 이 대신 만들어 줌)
 *
 * [메서드 이름으로 쿼리 만들기]
 * 아래처럼 정해진 규칙대로 메서드 이름을 지으면 Spring 이 SQL 을 자동으로 만들어 줍니다.
 *   findBy / existsBy / countBy / findAllBy : 무엇을 할지 (조회 / 존재 여부 / 개수 / 여러 건 조회)
 *   FridgeId : 엔티티의 fridge 필드 안의 id 로 조건을 걸겠다는 뜻
 *   And      : 조건을 "그리고"로 연결
 */
public interface FridgeMemberRepository extends JpaRepository<FridgeMember, Long> {

    // 이 냉장고의 구성원 전체 목록 (구성원 조회에서 사용)
    // 만들어지는 SQL: SELECT * FROM group_members WHERE group_id = ?
    List<FridgeMember> findAllByFridgeId(Long fridgeId);

    // 이 회원이 이미 이 냉장고의 구성원인지 true / false 로 확인 (참여 신청, 수락에서 사용)
    // 만들어지는 SQL: group_id = ? AND user_id = ? 인 행이 있는지 확인
    boolean existsByFridgeIdAndUserId(Long fridgeId, Long userId);

    // 이 냉장고에서 이 회원의 구성원 정보를 찾는다. (권한 확인에서 사용)
    // Optional: "값이 있을 수도, 없을 수도 있다"를 표현하는 상자. 없으면 비어 있는 Optional 이 돌아옴
    //           (null 을 직접 다루다가 생기는 NullPointerException 을 막아 줌)
    Optional<FridgeMember> findByFridgeIdAndUserId(Long fridgeId, Long userId);

    // 구성원 번호(id)로 찾되, 이 냉장고 소속인지도 같이 확인한다. (추방, 권한 변경에서 사용)
    // 다른 냉장고의 구성원 번호를 넣어도 찾아지지 않게 막아 주는 보안 장치 역할
    Optional<FridgeMember> findByIdAndFridgeId(Long memberId, Long fridgeId);

    // 이 냉장고에서 해당 역할을 가진 사람이 몇 명인지 센다. (마지막 관리자인지 확인할 때 사용)
    // countBy... = 조건에 맞는 행의 개수를 돌려줌
    long countByFridgeIdAndRole(Long fridgeId, MemberRole role);
}
