package com.kdt.fridge.groupmember.service;

import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.groupmember.FridgeMemberRepository;
import com.kdt.fridge.groupmember.dto.FridgeMemberResponse;
import com.kdt.fridge.groupmember.entity.FridgeMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FridgeMemberService {

    private final FridgeMemberRepository fridgeMemberRepository;
    
    public List<FridgeMemberResponse> getMembers(Long fridgeId , Long loginUserId){
        if (!fridgeMemberRepository.existsByFridgeIdAndUserId(fridgeId, loginUserId)) {
            throw new BusinessException(ErrorCode."에러자리");
        }

        List<FridgeMember> members = fridgeMemberRepository.findAllByFridgeId(fridgeId);

        return members.stream()
                .map(FridgeMemberResponse::from)
                .toList();
    }
}