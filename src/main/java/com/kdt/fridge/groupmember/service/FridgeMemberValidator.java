package com.kdt.fridge.groupmember.service;

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
