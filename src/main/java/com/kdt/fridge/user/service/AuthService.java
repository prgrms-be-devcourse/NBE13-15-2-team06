package com.kdt.fridge.user.service;

import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.global.security.JwtProvider;
import com.kdt.fridge.user.client.KakaoOAuthClient;
import com.kdt.fridge.user.dto.*;
import com.kdt.fridge.user.entity.User;
import com.kdt.fridge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.createLocal(request.email(), passwordEncoder.encode(request.password()));
        userRepository.save(user);

        return SignupResponse.from(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        if (user.getPassword() == null
                || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        String accessToken = jwtProvider.createAccessToken(user.getId());
        return LoginResponse.of(user, accessToken, jwtProvider.getAccessTokenExpirationSeconds());
    }

    private final KakaoOAuthClient kakaoOAuthClient;   // 추가

    @Transactional
    public KakaoLoginResponse kakaoLogin(KakaoLoginRequest request) {
        Long kakaoId = kakaoOAuthClient.getKakaoId(request.authorizationCode());

        Optional<User> existingUser = userRepository.findByKakaoId(kakaoId);
        boolean isNewUser = existingUser.isEmpty();
        User user = existingUser.orElseGet(() -> userRepository.save(User.createKakao(kakaoId)));

        String accessToken = jwtProvider.createAccessToken(user.getId());
        return KakaoLoginResponse.of(user, accessToken, jwtProvider.getAccessTokenExpirationSeconds(), isNewUser);
    }
}