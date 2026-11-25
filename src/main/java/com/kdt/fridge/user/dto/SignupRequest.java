package com.kdt.fridge.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(

        @NotBlank(message = "이메일을 입력해 주세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 50, message = "이메일은 50자 이하여야 합니다.")
        String email,

        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,20}$",
                message = "비밀번호는 8~20자이며 영문, 숫자, 특수문자를 모두 포함해야 합니다."
        )
        String password
) {
}