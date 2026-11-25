package com.kdt.fridge.user.controller;

import com.kdt.fridge.global.response.ApiResponse;
import com.kdt.fridge.user.dto.NicknameUpdateRequest;
import com.kdt.fridge.user.dto.NicknameUpdateResponse;
import com.kdt.fridge.user.dto.UserProfileResponse;
import com.kdt.fridge.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal Long userId) {
        UserProfileResponse response = userService.getMyProfile(userId);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "회원정보를 조회했습니다.", response));
    }

    @PatchMapping("/me/nickname")
    public ResponseEntity<ApiResponse<NicknameUpdateResponse>> updateNickname(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody NicknameUpdateRequest request) {
        NicknameUpdateResponse response = userService.updateNickname(userId, request);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "닉네임이 설정되었습니다.", response));
    }
}