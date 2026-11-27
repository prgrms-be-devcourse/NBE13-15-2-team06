package com.kdt.fridge.user.controller;

import com.kdt.fridge.global.response.ApiResponse;
import com.kdt.fridge.user.dto.*;
import com.kdt.fridge.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name ="Auth", description = "회원가입 · 로그인")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일·비밀번호로 가입. 이미 가입된 이메일은 불가")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "회원가입이 완료되었습니다.", response));
    }

    @Operation(summary = "로그인", description = "이메일·비밀번호로 로그인하고 액세스 토큰(JWT) 발급")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "로그인에 성공했습니다.", response));
    }

    @Operation(summary = "로그아웃", description = "서버에서 토큰을 무효화하지 않음. 클라이언트에서 토큰을 삭제해야 함")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "카카오 로그인", description = "카카오 인가 코드로 로그인하고 액세스 토큰(JWT) 발급. 처음이면 자동 가입")
    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<KakaoLoginResponse>> kakaoLogin(
            @Valid @RequestBody KakaoLoginRequest request) {
        KakaoLoginResponse response = authService.kakaoLogin(request);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "카카오 로그인에 성공했습니다.", response));
    }
}