package com.kdt.fridge.fridgeGroup.service;

import com.kdt.fridge.fridgeGroup.dto.*;
import com.kdt.fridge.fridgeGroup.entity.FridgeGroup;
import com.kdt.fridge.fridgeGroup.repository.FridgeGroupRepository;
import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.groupmember.entity.FridgeMember;
import com.kdt.fridge.groupmember.entity.MemberRole;
import com.kdt.fridge.groupmember.repository.FridgeMemberRepository;
import com.kdt.fridge.user.entity.User;
import com.kdt.fridge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class FridgeGroupService {

    private final FridgeGroupRepository fridgeGroupRepository;
    private final FridgeMemberRepository fridgeMemberRepository;
    private final UserRepository userRepository;

    //냉장고 생성
    //냉장고를 만든 사람일 admin이 되며, 냉장고와 멤버 저장을 한 트랜잭션으로 묶음
    @Transactional
    public FridgeResponse create(Long userId, FridgeCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String inviteCode = UUID.randomUUID().toString().substring(0, 8);
        FridgeGroup fridge = fridgeGroupRepository.save(new FridgeGroup(request.name().trim(), inviteCode));

        fridgeMemberRepository.save(FridgeMember.createAdmin(fridge, user));

        return FridgeResponse.from(fridge, MemberRole.ADMIN.name(), 1L);
    }

    //내가 속한 냉장고 목록
    //group_members에서 출발해 냉장고와 내 역할을 함께 얻는다.
    @Transactional(readOnly = true)
    public List<FridgeSummaryResponse> getMyAllFridges(Long userId) {
        return fridgeMemberRepository.findByUserId(userId).stream()
                .map(member -> FridgeSummaryResponse.from(
                        member.getFridge(),
                        member.getRole().name(),
                        fridgeMemberRepository.countByFridgeId(member.getFridge().getId())))
                .toList();
    }

    //냉장도 상세정보
    @Transactional(readOnly = true)
    public FridgeResponse getFridge(Long userId, Long fridgeId) {
        FridgeGroup fridge = fridgeGroupRepository.findById(fridgeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FRIDGE_NOT_FOUND));

        FridgeMember member = fridgeMemberRepository.findByFridgeIdAndUserId(fridgeId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FRIDGE_MEMBER));

        return FridgeResponse.from(fridge, member.getRole().name(),
                fridgeMemberRepository.countByFridgeId(fridgeId));
    }

    // 냉장고 이름 수정. ADMIN만 가능하다.
    // save를 호출하지 않아도 트랜잭션이 끝날 때 변경이 반영된다.

    @Transactional
    public FridgeResponse updateName(Long userId, Long fridgeId, FridgeUpdateRequest request) {

        FridgeGroup fridge = fridgeGroupRepository.findById(fridgeId)
                .orElseThrow(()-> new BusinessException(ErrorCode.FRIDGE_NOT_FOUND));

        FridgeMember member = fridgeMemberRepository.findByFridgeIdAndUserId(fridgeId, userId)
                .orElseThrow(()-> new BusinessException(ErrorCode.NOT_FRIDGE_MEMBER));

        if(member.getRole() != MemberRole.ADMIN ){
            throw new BusinessException(ErrorCode.NOT_FRIDGE_ADMIN);
        }

        fridge.updateName(request.name().trim());
        return FridgeResponse.from(fridge,member.getRole().name()
                ,fridgeMemberRepository.countByFridgeId(fridgeId));
    }

    //냉장고 초대 코드 ADMIN만 가능하다
    @Transactional(readOnly = true)
    public InviteCodeResponse getInviteCode(Long userId, Long fridgeId) {
        FridgeGroup fridge = fridgeGroupRepository.findById(fridgeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FRIDGE_NOT_FOUND));

        FridgeMember member = fridgeMemberRepository.findByFridgeIdAndUserId(fridgeId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FRIDGE_MEMBER));

        if (member.getRole() != MemberRole.ADMIN) {
            throw new BusinessException(ErrorCode.NOT_FRIDGE_ADMIN);
        }

        return InviteCodeResponse.from(fridge);
    }
}
