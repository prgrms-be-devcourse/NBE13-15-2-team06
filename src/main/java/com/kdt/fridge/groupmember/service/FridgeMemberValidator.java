package com.kdt.fridge.groupmember.service;

import com.kdt.fridge.fridgeGroup.entity.FridgeGroup;
import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.groupmember.entity.FridgeMember;
import com.kdt.fridge.groupmember.entity.MemberRole;
import com.kdt.fridge.groupmember.repository.FridgeMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FridgeMemberValidator {
    private final FridgeMemberRepository fridgeMemberRepository;

    // 쓰기 메서드의 반드시 첫 줄에서 호출: 잠금 전에 다른 조회를 하면 예전 데이터를 보고 검사하게 됨
    public void lockFridge(Long fridgeId) {
        fridgeMemberRepository.lockFridgeById(fridgeId);
    }

    public FridgeGroup lockFridgeByInviteCode(String inviteCode) {
        return fridgeMemberRepository.lockFridgeByInviteCode(inviteCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INVITE_CODE));
    }

    public FridgeMember getMember(Long fridgeId, Long userId) {
        return fridgeMemberRepository.findByFridgeIdAndUserId(fridgeId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FRIDGE_MEMBER));
    }

    public FridgeMember getAdmin(Long fridgeId, Long userId) {
        FridgeMember member = getMember(fridgeId, userId);

        if (!member.isAdmin()) {
            throw new BusinessException(ErrorCode.NOT_FRIDGE_ADMIN);
        }
        return member;
    }

    public void validateNotLastAdmin(Long fridgeId) {
        long adminCount = fridgeMemberRepository.countByFridgeIdAndRole(fridgeId, MemberRole.ADMIN);
        if (adminCount <= 1) {
            throw new BusinessException(ErrorCode.LAST_ADMIN_REQUIRED);
        }
    }
}