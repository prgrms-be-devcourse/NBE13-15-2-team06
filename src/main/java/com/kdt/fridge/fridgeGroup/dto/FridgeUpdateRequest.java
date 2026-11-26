package com.kdt.fridge.fridgeGroup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FridgeUpdateRequest(
        @NotBlank @Size
                (max = 50, message = "냉장고 이름은 앞뒤 공백 제거 후 1~50자 이어야 합니다.") String name
) {
}
