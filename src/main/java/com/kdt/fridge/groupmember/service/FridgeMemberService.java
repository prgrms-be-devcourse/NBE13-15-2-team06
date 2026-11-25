package com.kdt.fridge.groupmember.service;

import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.groupmember.dto.FridgeMemberResponse;
import com.kdt.fridge.groupmember.entity.FridgeMember;
import com.kdt.fridge.groupmember.role.MemberRole;
import com.kdt.fridge.groupmember.repository.FridgeMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 구성원 관련 기능(비즈니스 로직)을 처리하는 서비스입니다.
 * 기능: 구성원 조회, 그룹 탈퇴, 구성원 추방, 권한 부여/회수
 *
 * [서비스(Service)란?]
 * 컨트롤러가 받은 요청을 실제로 처리하는 계층입니다.
 * "누가 할 수 있는가", "어떤 순서로 처리하는가" 같은 규칙(비즈니스 로직)이 여기에 들어갑니다.
 * 컨트롤러 → 서비스 → 레포지토리 순서로 일이 넘어갑니다.
 */
// @Service: Spring 에게 "이 클래스는 서비스 계층이니 객체를 만들어서 관리해 줘"라고 알리는 표시 (빈 등록)
@Service
// @RequiredArgsConstructor: Lombok 이 final 필드를 파라미터로 받는 생성자를 자동으로 만들어 줌
//   Spring 은 이 생성자를 통해 필요한 객체(레포지토리 등)를 자동으로 넣어 줌 (의존성 주입)
@RequiredArgsConstructor
// @Transactional: 메서드 안의 DB 작업을 하나의 "트랜잭션"으로 묶음
//   트랜잭션 = 전부 성공하거나, 하나라도 실패하면 전부 취소(롤백)되는 작업 단위
//   readOnly = true: 읽기 전용. 데이터를 바꾸지 않는 조회에서 더 빠르고 안전함
//   클래스 위에 붙이면 모든 메서드의 기본값이 됨. 데이터를 바꾸는 메서드는 아래에서 다시 @Transactional 을 붙여 덮어씀
@Transactional(readOnly = true)
public class FridgeMemberService {

    private final FridgeMemberRepository fridgeMemberRepository;
    private final FridgeMemberValidator fridgeMemberValidator;

    /** 구성원 목록 조회. 이 냉장고의 구성원만 볼 수 있다. */
    public List<FridgeMemberResponse> getMembers(Long fridgeId, Long loginUserId) {
        // 요청자가 구성원인지 확인. 아니면 여기서 403 에러가 나서 아래 코드는 실행되지 않음
        fridgeMemberValidator.getMember(fridgeId, loginUserId);

        // stream().map(...).toList(): 목록의 각 항목을 하나씩 변환해서 새 목록을 만드는 방식
        //   FridgeMemberResponse::from 은 "각 구성원(엔티티)을 응답 객체(DTO)로 바꿔라"라는 뜻 (메서드 참조)
        return fridgeMemberRepository.findAllByFridgeId(fridgeId).stream()
                .map(FridgeMemberResponse::from)
                .toList();
    }

    /** 그룹 탈퇴. 내가 스스로 나가는 기능이다. */
    // @Transactional: 데이터를 삭제하므로 읽기 전용이 아닌 일반 트랜잭션으로 덮어씀
    @Transactional
    public void leave(Long fridgeId, Long loginUserId) {
        FridgeMember me = fridgeMemberValidator.getMember(fridgeId, loginUserId);

        // 내가 관리자라면, 내가 나가도 관리자가 남는지 확인 (마지막 관리자면 409 에러)
        if (me.isAdmin()) {
            fridgeMemberValidator.validateNotLastAdmin(fridgeId);
        }

        // 구성원 명부에서 나를 삭제 (DELETE 쿼리 실행)
        fridgeMemberRepository.delete(me);
    }

    /** 구성원 추방. 관리자가 다른 구성원을 내보내는 기능이다. */
    @Transactional
    public void kick(Long fridgeId, Long memberId, Long loginUserId) {
        // 요청자가 관리자인지 확인 (아니면 403 에러)
        FridgeMember admin = fridgeMemberValidator.getAdmin(fridgeId, loginUserId);
        // 추방할 대상 찾기 (없거나 다른 냉장고 소속이면 404 에러)
        FridgeMember target = findTarget(fridgeId, memberId);

        // 자기 자신은 추방할 수 없음. 나가고 싶으면 탈퇴 기능을 써야 함 (400 에러)
        // Long 타입끼리는 == 가 아니라 equals() 로 비교해야 함
        //   (== 는 "같은 객체인가"를 비교해서, 값이 같아도 false 가 나올 수 있음)
        if (target.getId().equals(admin.getId())) {
            throw new BusinessException(ErrorCode.CANNOT_KICK_SELF);
        }

        fridgeMemberRepository.delete(target);
    }

    /** 권한 변경. newRole 이 ADMIN 이면 관리자로 올리고, MEMBER 이면 일반 구성원으로 내린다. */
    @Transactional
    public FridgeMemberResponse changeRole(Long fridgeId, Long memberId, MemberRole newRole, Long loginUserId) {
        // 요청자가 관리자인지 확인 (아니면 403 에러)
        fridgeMemberValidator.getAdmin(fridgeId, loginUserId);
        // 권한을 바꿀 대상 찾기 (없거나 다른 냉장고 소속이면 404 에러)
        FridgeMember target = findTarget(fridgeId, memberId);

        // 관리자를 일반 구성원으로 내리는 경우, 내리고도 관리자가 남는지 확인 (마지막 관리자면 409 에러)
        if (target.isAdmin() && newRole == MemberRole.MEMBER) {
            fridgeMemberValidator.validateNotLastAdmin(fridgeId);
        }

        // save() 를 따로 호출하지 않아도 됨.
        // 트랜잭션 안에서 엔티티의 값이 바뀌면, 메서드가 끝날 때 JPA 가 바뀐 부분을 알아채서
        // 자동으로 UPDATE 쿼리를 실행해 줌 (이것을 "변경 감지(Dirty Checking)"라고 함)
        target.changeRole(newRole);

        return FridgeMemberResponse.from(target);
    }

    // 추방, 권한 변경의 대상 구성원을 찾는다. 이 냉장고 소속이 아니거나 없으면 404 에러
    // private: 이 클래스 안에서만 쓰는 도우미 메서드
    private FridgeMember findTarget(Long fridgeId, Long memberId) {
        return fridgeMemberRepository.findByIdAndFridgeId(memberId, fridgeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
