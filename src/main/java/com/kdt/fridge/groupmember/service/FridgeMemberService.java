package com.kdt.fridge.groupmember.service;

import com.kdt.fridge.fridgeGroup.repository.FridgeGroupRepository;
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

/**
 * 냉장고 구성원 관리 비즈니스 로직을 담당하는 서비스.
 * 구성원 목록 조회, 탈퇴, 강퇴, 권한 변경을 처리한다. 클래스는 기본이 읽기 전용 트랜잭션이고,
 * 데이터를 바꾸는 메서드에만 {@code @Transactional}을 붙였다.
 * 변경 메서드는 모두 첫 줄에서 냉장고 행을 잠가 동시 변경으로 인한 불일치(운영자 0명 등)를 막는다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FridgeMemberService {
    private final FridgeMemberRepository fridgeMemberRepository;
    private final FridgeMemberValidator fridgeMemberValidator;
    private final FridgeGroupRepository fridgeGroupRepository;

    /**
     * 냉장고 구성원 목록을 가입 순으로 조회한다.
     * 조회자가 해당 냉장고의 구성원이 아니면 예외가 발생한다.
     */
    public List<FridgeMemberResponse> getMembers(Long fridgeId, Long loginUserId) {
        fridgeMemberValidator.getMember(fridgeId, loginUserId);

        return fridgeMemberRepository.findAllByFridgeIdOrderByJoinedAtAscIdAsc(fridgeId).stream()
                .map(FridgeMemberResponse::from)
                .toList();
    }

    /**
     * 로그인한 사용자가 냉장고에서 탈퇴한다.
     * <ul>
     * <li>마지막 한 명의 구성원이면 냉장고 자체를 삭제한다.</li>
     * <li>운영자라면 마지막 운영자가 아닐 때만 탈퇴할 수 있다.</li>
     * <li>그 외에는 본인의 구성원 레코드만 삭제한다.</li>
     * </ul>
     */
    @Transactional
    public void leave(Long fridgeId, Long loginUserId) {
        fridgeMemberValidator.lockFridge(fridgeId);

        FridgeMember me = fridgeMemberValidator.getMember(fridgeId, loginUserId);

        if (fridgeMemberRepository.countByFridgeId(fridgeId) == 1) {
            // 구성원을 먼저 삭제해야 삭제된 냉장고를 참조한다는 Hibernate 예외가 나지 않는다
            fridgeMemberRepository.delete(me);
            fridgeGroupRepository.deleteById(fridgeId);
            return;
        }

        if (me.isAdmin()) {
            fridgeMemberValidator.validateNotLastAdmin(fridgeId);
        }

        fridgeMemberRepository.delete(me);
    }

    /**
     * 운영자가 다른 구성원을 강퇴한다.
     * 요청자가 운영자인지 확인하고, 대상이 해당 냉장고 구성원인지 확인한 뒤,
     * 대상이 자기 자신이면 {@code CANNOT_KICK_SELF} 예외를 던진다. 통과하면 대상 구성원을 삭제한다.
     */
    @Transactional
    public void kick(Long fridgeId, Long memberId, Long loginUserId) {
        fridgeMemberValidator.lockFridge(fridgeId);

        FridgeMember admin = fridgeMemberValidator.getAdmin(fridgeId, loginUserId);

        FridgeMember target = findTarget(fridgeId, memberId);

        if (target.getId().equals(admin.getId())) {
            throw new BusinessException(ErrorCode.CANNOT_KICK_SELF);
        }

        fridgeMemberRepository.delete(target);
    }

    /**
     * 운영자가 구성원의 역할을 변경한다.
     * 요청자가 운영자여야 하며, 대상이 마지막 운영자인데 MEMBER로 강등하려는 경우는 거부한다.
     * 변경은 영속 엔티티의 값을 바꾸는 것으로 끝나며(더티 체킹), 변경된 구성원 정보를 반환한다.
     */
    @Transactional
    public FridgeMemberResponse changeRole(Long fridgeId, Long memberId, MemberRole newRole, Long loginUserId) {
        fridgeMemberValidator.lockFridge(fridgeId);

        fridgeMemberValidator.getAdmin(fridgeId, loginUserId);

        FridgeMember target = findTarget(fridgeId, memberId);

        if (target.isAdmin() && newRole == MemberRole.MEMBER) {
            fridgeMemberValidator.validateNotLastAdmin(fridgeId);
        }

        target.changeRole(newRole);

        return FridgeMemberResponse.from(target);
    }

    /**
     * 구성원 ID로 대상 구성원을 찾되, 해당 냉장고 소속인지까지 확인한다.
     * 없으면 {@code MEMBER_NOT_FOUND} 예외를 던진다.
     */
    private FridgeMember findTarget(Long fridgeId, Long memberId) {
        return fridgeMemberRepository.findByIdAndFridgeId(memberId, fridgeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}