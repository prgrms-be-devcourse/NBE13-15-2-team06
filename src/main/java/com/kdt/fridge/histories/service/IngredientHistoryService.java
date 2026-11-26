package com.kdt.fridge.histories.service;

import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.groupmember.repository.FridgeMemberRepository;
import com.kdt.fridge.histories.dto.IngredientHistoryResponse;
import com.kdt.fridge.histories.repository.IngredientHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientHistoryService {

    private final IngredientHistoryRepository ingredientHistoryRepository;
    private final FridgeMemberRepository fridgeMemberRepository;

    // 냉장고의 변경 이력 조회. 구성원만 가능하다.
    @Transactional(readOnly = true)
    public List<IngredientHistoryResponse> getHistories(Long userId, Long groupId) {
        if (!fridgeMemberRepository.existsByFridgeIdAndUserId(groupId, userId)) {
            throw new BusinessException(ErrorCode.NOT_FRIDGE_MEMBER);
        }

        return ingredientHistoryRepository.findByIngredientGroupIdOrderByCreatedAtDesc(groupId).stream()
                .map(IngredientHistoryResponse::from)
                .toList();
    }
}
