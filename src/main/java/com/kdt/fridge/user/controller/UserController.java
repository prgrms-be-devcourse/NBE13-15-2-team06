package com.kdt.fridge.user.controller;

import com.kdt.fridge.global.response.ApiResponse;
import com.kdt.fridge.user.dto.UserProfileResponse;
import com.kdt.fridge.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}