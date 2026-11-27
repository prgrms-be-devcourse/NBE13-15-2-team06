package com.kdt.fridge.user.controller;

import com.kdt.fridge.global.response.ApiResponse;
import com.kdt.fridge.user.dto.NicknameUpdateRequest;
import com.kdt.fridge.user.dto.NicknameUpdateResponse;
import com.kdt.fridge.user.dto.UserPasswordUpdateRequest;
import com.kdt.fridge.user.dto.UserProfileResponse;
import com.kdt.fridge.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "내 회원정보")
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 회원정보 조회", description = "로그인한 사용자의 회원정보 조회")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal Long userId) {
        UserProfileResponse response = userService.getMyProfile(userId);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "회원정보를 조회했습니다.", response));
    }

    @Operation(summary = "닉네임 변경", description = "다른 사용자가 쓰는 닉네임은 불가")
    @PatchMapping("/me/nickname")
    public ResponseEntity<ApiResponse<NicknameUpdateResponse>> updateNickname(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody NicknameUpdateRequest request) {
        NicknameUpdateResponse response = userService.updateNickname(userId, request);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "닉네임이 설정되었습니다.", response));
    }

    @Operation(summary = "비밀번호 변경",
            description = "현재 비밀번호 확인 후 변경. 현재와 같은 비밀번호로는 불가. 비밀번호가 없는 카카오 가입 계정은 불가")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserPasswordUpdateRequest request) {
        userService.updatePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "비밀번호가 변경되었습니다.", null));
    }
}