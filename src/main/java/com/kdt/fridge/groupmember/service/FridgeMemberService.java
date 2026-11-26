package com.kdt.fridge.groupmember.service;

import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.groupmember.dto.FridgeMemberResponse;
import com.kdt.fridge.groupmember.entity.FridgeMember;
import com.kdt.fridge.groupmember.entity.MemberRole;
import com.kdt.fridge.groupmember.repository.FridgeMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service

@RequiredArgsConstructor

@Transactional(readOnly = true)
public class FridgeMemberService {
    private final FridgeMemberRepository fridgeMemberRepository;
    private final FridgeMemberValidator fridgeMemberValidator;

    public List<FridgeMemberResponse> getMembers(Long fridgeId, Long loginUserId) {
        fridgeMemberValidator.getMember(fridgeId, loginUserId);

        return fridgeMemberRepository.findAllByFridgeId(fridgeId).stream()
                .map(FridgeMemberResponse::from)
                .toList();
    }

    @Transactional
    public void leave(Long fridgeId, Long loginUserId) {
        FridgeMember me = fridgeMemberValidator.getMember(fridgeId, loginUserId);

        if (me.isAdmin()) {
            fridgeMemberValidator.validateNotLastAdmin(fridgeId);
        }

        fridgeMemberRepository.delete(me);
    }

    @Transactional
    public void kick(Long fridgeId, Long memberId, Long loginUserId) {
        FridgeMember admin = fridgeMemberValidator.getAdmin(fridgeId, loginUserId);

        FridgeMember target = findTarget(fridgeId, memberId);

        if (target.getId().equals(admin.getId())) {
            throw new BusinessException(ErrorCode.CANNOT_KICK_SELF);
        }

        fridgeMemberRepository.delete(target);
    }

    @Transactional
    public FridgeMemberResponse changeRole(Long fridgeId, Long memberId, MemberRole newRole, Long loginUserId) {
        fridgeMemberValidator.getAdmin(fridgeId, loginUserId);

        FridgeMember target = findTarget(fridgeId, memberId);

        if (target.isAdmin() && newRole == MemberRole.MEMBER) {
            fridgeMemberValidator.validateNotLastAdmin(fridgeId);
        }

        target.changeRole(newRole);

        return FridgeMemberResponse.from(target);
    }

    private FridgeMember findTarget(Long fridgeId, Long memberId) {
        return fridgeMemberRepository.findByIdAndFridgeId(memberId, fridgeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
