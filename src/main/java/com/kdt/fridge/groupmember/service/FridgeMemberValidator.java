package com.kdt.fridge.groupmember.service;

import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.groupmember.entity.FridgeMember;
import com.kdt.fridge.groupmember.role.MemberRole;
import com.kdt.fridge.groupmember.repository.FridgeMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "이 사람이 이 냉장고의 구성원인가? 관리자인가?" 같은 권한 검사를 모아 둔 클래스입니다.
 *
 * FridgeMemberService 와 JoinRequestService 가 같은 검사를 필요로 해서,
 * 코드를 복사해서 중복으로 쓰지 않도록 한 곳에 모았습니다.
 * (검사에 실패하면 BusinessException 을 던지고, 전역 예외 처리기가 이를 에러 응답으로 바꿔 줍니다.)
 */
// @Component: Spring 에게 "이 클래스의 객체를 네가 만들어서 관리해 줘"라고 요청하는 표시
//   이렇게 Spring 이 관리하는 객체를 "빈(Bean)"이라고 부름
//   다른 클래스는 이 객체를 직접 new 하지 않고, 생성자로 받아서(주입받아서) 사용함 → 이것이 의존성 주입(DI)
@Component
// @RequiredArgsConstructor: Lombok 이 final 필드를 파라미터로 받는 생성자를 자동으로 만들어 줌
//   Spring 은 이 생성자를 통해 fridgeMemberRepository 를 자동으로 넣어 줌
@RequiredArgsConstructor
public class FridgeMemberValidator {

    // final: 한 번 정해지면 바뀌지 않는 값. 생성자로만 값이 들어옴
    private final FridgeMemberRepository fridgeMemberRepository;

    /**
     * 요청한 사람이 이 냉장고의 구성원인지 확인하고, 맞으면 그 구성원 정보를 돌려준다.
     * 구성원이 아니면 403 에러(NOT_FRIDGE_MEMBER)가 발생한다.
     */
    public FridgeMember getMember(Long fridgeId, Long userId) {
        // findBy... 는 Optional 을 돌려줌. orElseThrow 는 "값이 없으면 예외를 던져라"라는 뜻
        //   () -> new BusinessException(...) 는 람다식: "예외가 필요할 때 이 예외를 만들어서 던져라"
        return fridgeMemberRepository.findByFridgeIdAndUserId(fridgeId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FRIDGE_MEMBER));
    }

    /**
     * 요청한 사람이 이 냉장고의 관리자인지 확인한다.
     * - 구성원이 아니면 403 에러(NOT_FRIDGE_MEMBER)
     * - 구성원이지만 관리자가 아니면 403 에러(NOT_FRIDGE_ADMIN)
     */
    public FridgeMember getAdmin(Long fridgeId, Long userId) {
        // 먼저 구성원인지 확인 (구성원이 아니면 여기서 예외가 터짐)
        FridgeMember member = getMember(fridgeId, userId);
        // 구성원이지만 관리자가 아니면 예외
        if (!member.isAdmin()) {
            throw new BusinessException(ErrorCode.NOT_FRIDGE_ADMIN);
        }
        return member;
    }

    /**
     * 관리자가 한 명 줄어도 괜찮은지 확인한다.
     * 지금 관리자가 1명 이하이면 409 에러(LAST_ADMIN_REQUIRED)가 발생한다.
     * 마지막 관리자가 탈퇴하거나 권한이 회수되어, 관리자가 없는 냉장고가 되는 것을 막기 위한 검사다.
     */
    public void validateNotLastAdmin(Long fridgeId) {
        // 이 냉장고의 관리자 수를 센다
        long adminCount = fridgeMemberRepository.countByFridgeIdAndRole(fridgeId, MemberRole.ADMIN);
        if (adminCount <= 1) {
            throw new BusinessException(ErrorCode.LAST_ADMIN_REQUIRED);
        }
    }
}
