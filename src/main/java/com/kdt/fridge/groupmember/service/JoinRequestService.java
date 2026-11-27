package com.kdt.fridge.groupmember.service;

import com.kdt.fridge.fridgeGroup.entity.FridgeGroup;
import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.groupmember.dto.JoinRequestApproveResponse;
import com.kdt.fridge.groupmember.dto.JoinRequestResponse;
import com.kdt.fridge.groupmember.entity.FridgeMember;
import com.kdt.fridge.groupmember.entity.JoinRequest;
import com.kdt.fridge.groupmember.entity.JoinRequestStatus;
import com.kdt.fridge.groupmember.repository.FridgeMemberRepository;
import com.kdt.fridge.groupmember.repository.JoinRequestRepository;
import com.kdt.fridge.user.entity.User;
import com.kdt.fridge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 냉장고 참여 신청의 접수·조회·수락·거절을 처리하는 서비스.
 * 클래스는 기본이 읽기 전용 트랜잭션이고, 데이터를 바꾸는 메서드에만 {@code @Transactional}을 붙였다.
 * 냉장고 행 잠금으로 같은 냉장고에 대한 신청/처리가 동시에 일어나도 중복이나 불일치가 없도록 한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JoinRequestService {
    private final JoinRequestRepository joinRequestRepository;
    private final FridgeMemberRepository fridgeMemberRepository;
    private final UserRepository userRepository;
    private final FridgeMemberValidator fridgeMemberValidator;

    /**
     * 초대 코드로 참여를 신청한다.
     * <ol>
     * <li>초대 코드로 냉장고를 찾으며 잠근다(없으면 예외).</li>
     * <li>이미 구성원이면 {@code ALREADY_FRIDGE_MEMBER}.</li>
     * <li>이미 대기 중인 신청이 있으면 {@code DUPLICATE_JOIN_REQUEST}.</li>
     * <li>신청자 사용자를 조회한 뒤 PENDING 상태의 신청을 저장한다.</li>
     * </ol>
     */
    @Transactional
    public JoinRequestResponse requestJoin(String inviteCode, Long loginUserId) {
        FridgeGroup fridge = fridgeMemberValidator.lockFridgeByInviteCode(inviteCode);

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

    /**
     * 냉장고의 대기(PENDING) 신청 목록을 신청 순으로 조회한다. 요청자는 운영자여야 한다.
     */
    public List<JoinRequestResponse> getPendingRequests(Long fridgeId, Long loginUserId) {
        fridgeMemberValidator.getAdmin(fridgeId, loginUserId);

        return joinRequestRepository
                .findAllByFridgeIdAndStatusOrderByRequestedAtAsc(fridgeId, JoinRequestStatus.PENDING)
                .stream()
                .map(JoinRequestResponse::from)
                .toList();
    }

    /**
     * 참여 신청을 수락하고 신청자를 일반 구성원(MEMBER)으로 추가한다.
     * 운영자 확인 후 신청 상태를 APPROVED로 바꾸고 구성원을 저장한다.
     * 그 사이 이미 구성원이 된 경우를 사전 확인과 DB 유니크 제약 위반 처리 두 단계로 막아
     * {@code ALREADY_FRIDGE_MEMBER}로 일관되게 응답한다.
     * {@code saveAndFlush}를 쓰는 이유는 제약 위반을 이 메서드 안에서 즉시 잡기 위해서다.
     */
    @Transactional
    public JoinRequestApproveResponse approve(Long fridgeId, Long requestId, Long loginUserId) {
        fridgeMemberValidator.lockFridge(fridgeId);

        fridgeMemberValidator.getAdmin(fridgeId, loginUserId);

        JoinRequest joinRequest = findRequest(fridgeId, requestId);

        joinRequest.approve();

        if (fridgeMemberRepository.existsByFridgeIdAndUserId(fridgeId, joinRequest.getUser().getId())) {
            throw new BusinessException(ErrorCode.ALREADY_FRIDGE_MEMBER);
        }

        FridgeMember member;
        try {
            member = fridgeMemberRepository.saveAndFlush(
                    FridgeMember.createMember(joinRequest.getFridge(), joinRequest.getUser())
            );
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.ALREADY_FRIDGE_MEMBER);
        }

        return JoinRequestApproveResponse.of(joinRequest, member);
    }

    /**
     * 참여 신청을 거절한다. 운영자 확인 후 신청 상태만 REJECTED로 바꾸며 구성원은 추가하지 않는다.
     */
    @Transactional
    public JoinRequestResponse reject(Long fridgeId, Long requestId, Long loginUserId) {
        fridgeMemberValidator.lockFridge(fridgeId);

        fridgeMemberValidator.getAdmin(fridgeId, loginUserId);

        JoinRequest joinRequest = findRequest(fridgeId, requestId);

        joinRequest.reject();

        return JoinRequestResponse.from(joinRequest);
    }

    /**
     * 신청 ID로 신청을 찾되, 해당 냉장고에 속한 신청인지까지 확인한다.
     * 없으면 {@code JOIN_REQUEST_NOT_FOUND} 예외를 던진다.
     */
    private JoinRequest findRequest(Long fridgeId, Long requestId) {
        return joinRequestRepository.findByIdAndFridgeId(requestId, fridgeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOIN_REQUEST_NOT_FOUND));
    }
}