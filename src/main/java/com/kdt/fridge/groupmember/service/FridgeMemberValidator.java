package com.kdt.fridge.groupmember.service;

import com.kdt.fridge.fridgeGroup.entity.FridgeGroup;
import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.groupmember.entity.FridgeMember;
import com.kdt.fridge.groupmember.entity.MemberRole;
import com.kdt.fridge.groupmember.repository.FridgeMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 냉장고 구성원 관련 권한·상태 검증을 모아 둔 컴포넌트.
 * 구성원 여부, 운영자 여부, 마지막 운영자 보호, 냉장고 행 잠금 같은 공통 검사를
 * {@link FridgeMemberService}와 {@link JoinRequestService}가 함께 재사용하도록 분리했다.
 * 조건을 만족하지 못하면 {@code BusinessException}을 던진다.
 */
@Component
@RequiredArgsConstructor
public class FridgeMemberValidator {
    private final FridgeMemberRepository fridgeMemberRepository;

    /**
     * 냉장고 행에 비관적 잠금을 건다. 같은 냉장고에 대한 구성원 변경 작업이 한 번에 하나씩만 실행되게 한다.
     * <p>반드시 쓰기 메서드의 첫 줄에서 호출해야 한다. 잠금 전에 다른 조회를 하면
     * 잠금을 기다리는 사이 바뀐 데이터가 아니라 예전 데이터를 보고 검사하게 된다.
     */
    public void lockFridge(Long fridgeId) {
        fridgeMemberRepository.lockFridgeById(fridgeId);
    }

    /**
     * 초대 코드로 냉장고를 찾으며 동시에 행 잠금을 건다.
     * 해당 코드의 냉장고가 없으면 {@code INVALID_INVITE_CODE} 예외를 던진다.
     */
    public FridgeGroup lockFridgeByInviteCode(String inviteCode) {
        return fridgeMemberRepository.lockFridgeByInviteCode(inviteCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INVITE_CODE));
    }

    /**
     * 사용자가 해당 냉장고의 구성원인지 확인하고 구성원 엔티티를 반환한다.
     * 구성원이 아니면 {@code NOT_FRIDGE_MEMBER} 예외를 던진다.
     */
    public FridgeMember getMember(Long fridgeId, Long userId) {
        return fridgeMemberRepository.findByFridgeIdAndUserId(fridgeId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FRIDGE_MEMBER));
    }

    /**
     * 사용자가 해당 냉장고의 운영자인지 확인하고 구성원 엔티티를 반환한다.
     * 구성원이 아니면 {@code NOT_FRIDGE_MEMBER}, 구성원이지만 운영자가 아니면 {@code NOT_FRIDGE_ADMIN} 예외를 던진다.
     */
    public FridgeMember getAdmin(Long fridgeId, Long userId) {
        FridgeMember member = getMember(fridgeId, userId);

        if (!member.isAdmin()) {
            throw new BusinessException(ErrorCode.NOT_FRIDGE_ADMIN);
        }
        return member;
    }

    /**
     * 운영자가 한 명 이하인지 확인해, 마지막 운영자를 탈퇴시키거나 강등하는 것을 막는다.
     * 운영자 수가 1명 이하이면 {@code LAST_ADMIN_REQUIRED} 예외를 던진다.
     * 호출 전에 {@link #lockFridge}로 잠금을 잡아야 동시 변경에도 정확하다.
     */
    public void validateNotLastAdmin(Long fridgeId) {
        long adminCount = fridgeMemberRepository.countByFridgeIdAndRole(fridgeId, MemberRole.ADMIN);
        if (adminCount <= 1) {
            throw new BusinessException(ErrorCode.LAST_ADMIN_REQUIRED);
        }
    }
}