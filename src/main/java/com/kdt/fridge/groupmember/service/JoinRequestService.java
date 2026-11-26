package com.kdt.fridge.groupmember.service;

import com.kdt.fridge.fridgeGroup.entity.FridgeGroup;
import com.kdt.fridge.fridgeGroup.repository.FridgeGroupRepository;
import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.groupmember.dto.JoinRequestApproveResponse;
import com.kdt.fridge.groupmember.dto.JoinRequestResponse;
import com.kdt.fridge.groupmember.entity.FridgeMember;
import com.kdt.fridge.groupmember.entity.JoinRequest;
import com.kdt.fridge.groupmember.role.JoinRequestStatus;
import com.kdt.fridge.groupmember.repository.FridgeMemberRepository;
import com.kdt.fridge.groupmember.repository.JoinRequestRepository;
import com.kdt.fridge.user.entity.User;
import com.kdt.fridge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service

@RequiredArgsConstructor

@Transactional(readOnly = true)
public class JoinRequestService {
    private final JoinRequestRepository joinRequestRepository;
    private final FridgeMemberRepository fridgeMemberRepository;

    private final FridgeGroupRepository fridgeGroupRepository;

    private final UserRepository userRepository;
    private final FridgeMemberValidator fridgeMemberValidator;

    @Transactional
    public JoinRequestResponse requestJoin(String inviteCode, Long loginUserId) {
        FridgeGroup fridge = fridgeGroupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INVITE_CODE));

        if (fridgeMemberRepository.existsByFridgeIdAndUserId(fridge.getId(), loginUserId)) {
            throw new BusinessException(ErrorCode.ALREADY_FRIDGE_MEMBER);
        }

        if (joinRequestRepository.existsByFridgeIdAndUserIdAndStatus(
                fridge.getId(), loginUserId, JoinRequestStatus.PENDING)) {
            throw new BusinessException(ErrorCode.DUPLICATE_JOIN_REQUEST);
        }

        User user = userRepository.findById(loginUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        JoinRequest joinRequest = joinRequestRepository.save(JoinRequest.create(fridge, user));
        return JoinRequestResponse.from(joinRequest);
    }

    public List<JoinRequestResponse> getPendingRequests(Long fridgeId, Long loginUserId) {
        fridgeMemberValidator.getAdmin(fridgeId, loginUserId);

        return joinRequestRepository
                .findAllByFridgeIdAndStatusOrderByRequestedAtAsc(fridgeId, JoinRequestStatus.PENDING)
                .stream()
                .map(JoinRequestResponse::from)
                .toList();
    }

    @Transactional
    public JoinRequestApproveResponse approve(Long fridgeId, Long requestId, Long loginUserId) {
        fridgeMemberValidator.getAdmin(fridgeId, loginUserId);

        JoinRequest joinRequest = findRequest(fridgeId, requestId);

        joinRequest.approve();

        if (fridgeMemberRepository.existsByFridgeIdAndUserId(fridgeId, joinRequest.getUser().getId())) {
            throw new BusinessException(ErrorCode.ALREADY_FRIDGE_MEMBER);
        }

        FridgeMember member = fridgeMemberRepository.save(
                FridgeMember.createMember(joinRequest.getFridge(), joinRequest.getUser())
        );

        return JoinRequestApproveResponse.of(joinRequest, member);
    }

    @Transactional
    public JoinRequestResponse reject(Long fridgeId, Long requestId, Long loginUserId) {
        fridgeMemberValidator.getAdmin(fridgeId, loginUserId);

        JoinRequest joinRequest = findRequest(fridgeId, requestId);

        joinRequest.reject();

        return JoinRequestResponse.from(joinRequest);
    }

    private JoinRequest findRequest(Long fridgeId, Long requestId) {
        return joinRequestRepository.findByIdAndFridgeId(requestId, fridgeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOIN_REQUEST_NOT_FOUND));
    }
}
