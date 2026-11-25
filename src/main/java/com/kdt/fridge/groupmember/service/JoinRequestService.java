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

/**
 * 참여 신청 관련 기능(비즈니스 로직)을 처리하는 서비스입니다.
 * 기능: 초대 코드로 신청, 신청자 목록 조회, 신청 수락, 신청 거절
 *
 * [서비스(Service)란?]
 * 컨트롤러가 받은 요청을 실제로 처리하는 계층입니다.
 * "누가 할 수 있는가", "어떤 순서로 처리하는가" 같은 규칙(비즈니스 로직)이 여기에 들어갑니다.
 * 컨트롤러 → 서비스 → 레포지토리 순서로 일이 넘어갑니다.
 */
// @Service: Spring 에게 "이 클래스는 서비스 계층이니 객체를 만들어서 관리해 줘"라고 알리는 표시 (빈 등록)
@Service
// @RequiredArgsConstructor: Lombok 이 final 필드를 파라미터로 받는 생성자를 자동으로 만들어 줌
//   Spring 은 이 생성자를 통해 필요한 객체들을 자동으로 넣어 줌 (의존성 주입)
@RequiredArgsConstructor
// @Transactional: 메서드 안의 DB 작업을 하나의 "트랜잭션"으로 묶음
//   트랜잭션 = 전부 성공하거나, 하나라도 실패하면 전부 취소(롤백)되는 작업 단위
//   readOnly = true: 읽기 전용. 조회만 하는 메서드에서 더 빠르고 안전함
//   클래스 위에 붙이면 모든 메서드의 기본값이 됨. 데이터를 바꾸는 메서드는 아래에서 @Transactional 을 다시 붙여 덮어씀
@Transactional(readOnly = true)
public class JoinRequestService {

    private final JoinRequestRepository joinRequestRepository;
    private final FridgeMemberRepository fridgeMemberRepository;
    // 냉장고(FridgeGroup)를 조회하는 레포지토리. 냉장고 담당 팀원이 만든 것
    private final FridgeGroupRepository fridgeGroupRepository;
    // 회원(User)을 조회하는 레포지토리. 회원 담당 팀원이 만든 것
    private final UserRepository userRepository;
    private final FridgeMemberValidator fridgeMemberValidator;

    /** 초대 코드로 참여를 신청한다. 신청은 "대기 중(PENDING)" 상태로 저장된다. */
    // @Transactional: 데이터를 저장하므로 읽기 전용이 아닌 일반 트랜잭션으로 덮어씀
    @Transactional
    public JoinRequestResponse requestJoin(String inviteCode, Long loginUserId) {

        // 1단계: 초대 코드로 냉장고 찾기 (코드가 틀려서 없으면 404 에러)
        //   orElseThrow: Optional 에 값이 없으면 예외를 던짐. () -> ... 는 "예외가 필요할 때 만들어라"라는 람다식
        FridgeGroup fridge = fridgeGroupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INVITE_CODE));

        // 2단계: 이미 이 냉장고의 구성원이면 신청할 필요가 없으므로 막음 (409 에러)
        if (fridgeMemberRepository.existsByFridgeIdAndUserId(fridge.getId(), loginUserId)) {
            throw new BusinessException(ErrorCode.ALREADY_FRIDGE_MEMBER);
        }

        // 3단계: 이미 대기 중인 신청이 있으면 중복 신청이므로 막음 (409 에러)
        if (joinRequestRepository.existsByFridgeIdAndUserIdAndStatus(
                fridge.getId(), loginUserId, JoinRequestStatus.PENDING)) {
            throw new BusinessException(ErrorCode.DUPLICATE_JOIN_REQUEST);
        }

        // 4단계: 신청하는 회원의 정보 가져오기 (회원이 없으면 404 에러)
        User user = userRepository.findById(loginUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 5단계: 모든 검사를 통과했으니 신청을 만들어서 저장 (INSERT)
        //   save() 는 저장된 엔티티(번호가 채워진 상태)를 돌려줌
        JoinRequest joinRequest = joinRequestRepository.save(JoinRequest.create(fridge, user));
        return JoinRequestResponse.from(joinRequest);
    }

    /** 대기 중인 신청자 목록을 오래된 순으로 조회한다. 관리자만 볼 수 있다. */
    // 조회만 하므로 클래스의 기본값(읽기 전용 트랜잭션)을 그대로 사용함
    public List<JoinRequestResponse> getPendingRequests(Long fridgeId, Long loginUserId) {
        // 요청자가 관리자인지 확인. 아니면 여기서 403 에러가 나서 아래 코드는 실행되지 않음
        fridgeMemberValidator.getAdmin(fridgeId, loginUserId);

        // 대기 중(PENDING)인 신청을 신청 시각이 오래된 순으로 가져와서, 응답 객체(DTO)로 변환
        return joinRequestRepository
                .findAllByFridgeIdAndStatusOrderByRequestedAtAsc(fridgeId, JoinRequestStatus.PENDING)
                .stream()
                .map(JoinRequestResponse::from)
                .toList();
    }

    /** 신청을 수락한다. 관리자만 가능하며, 신청 상태를 APPROVED 로 바꾸고 일반 구성원으로 추가한다. */
    @Transactional
    public JoinRequestApproveResponse approve(Long fridgeId, Long requestId, Long loginUserId) {
        // 요청자가 관리자인지 확인 (아니면 403 에러)
        fridgeMemberValidator.getAdmin(fridgeId, loginUserId);
        // 수락할 신청 찾기 (없거나 다른 냉장고의 신청이면 404 에러)
        JoinRequest joinRequest = findRequest(fridgeId, requestId);

        // 먼저 신청 상태를 수락으로 바꿈. 이미 처리된 신청이면 여기서 409 에러가 남
        // 아래 단계에서 예외가 나면 @Transactional 이 모든 변경을 되돌리므로(롤백) APPROVED 는 저장되지 않음
        // 여기서 save() 를 따로 호출하지 않아도, 트랜잭션이 끝날 때 JPA 가 바뀐 값을 알아채서 UPDATE 를 실행함 (변경 감지)
        joinRequest.approve();

        // 그 사이에 다른 경로로 이미 구성원이 된 경우, 중복 등록을 막음 (409 에러)
        if (fridgeMemberRepository.existsByFridgeIdAndUserId(fridgeId, joinRequest.getUser().getId())) {
            throw new BusinessException(ErrorCode.ALREADY_FRIDGE_MEMBER);
        }

        // 신청한 회원을 일반 구성원(MEMBER)으로 만들어서 저장 (INSERT)
        FridgeMember member = fridgeMemberRepository.save(
                FridgeMember.createMember(joinRequest.getFridge(), joinRequest.getUser())
        );

        return JoinRequestApproveResponse.of(joinRequest, member);
    }

    /** 신청을 거절한다. 관리자만 가능하며, 신청 상태를 REJECTED 로 바꾼다. (기록은 삭제하지 않고 남김) */
    @Transactional
    public JoinRequestResponse reject(Long fridgeId, Long requestId, Long loginUserId) {
        // 요청자가 관리자인지 확인 (아니면 403 에러)
        fridgeMemberValidator.getAdmin(fridgeId, loginUserId);
        // 거절할 신청 찾기 (없거나 다른 냉장고의 신청이면 404 에러)
        JoinRequest joinRequest = findRequest(fridgeId, requestId);

        // 상태를 거절로 바꿈. 이미 처리된 신청이면 여기서 409 에러가 남
        // save() 를 따로 호출하지 않아도, 값이 바뀌면 JPA 가 알아서 UPDATE 를 실행함 (변경 감지)
        joinRequest.reject();

        return JoinRequestResponse.from(joinRequest);
    }

    // 수락, 거절할 신청을 찾는다. 없으면 404 에러
    // 신청 번호만 쓰지 않고 냉장고 번호까지 함께 확인하는 이유:
    //   주소의 requestId 만 바꿔서 다른 냉장고의 신청을 처리하는 것을 막기 위해서다.
    // private: 이 클래스 안에서만 쓰는 도우미 메서드
    private JoinRequest findRequest(Long fridgeId, Long requestId) {
        return joinRequestRepository.findByIdAndFridgeId(requestId, fridgeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOIN_REQUEST_NOT_FOUND));
    }
}
