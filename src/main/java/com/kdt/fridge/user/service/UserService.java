package com.kdt.fridge.user.service;

import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.user.dto.NicknameUpdateRequest;
import com.kdt.fridge.user.dto.NicknameUpdateResponse;
import com.kdt.fridge.user.dto.UserPasswordUpdateRequest;
import com.kdt.fridge.user.dto.UserProfileResponse;
import com.kdt.fridge.user.entity.User;
import com.kdt.fridge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileResponse getMyProfile(Long userId) {
        return UserProfileResponse.from(findUser(userId));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public NicknameUpdateResponse updateNickname(Long userId, NicknameUpdateRequest request) {
        User user = findUser(userId);
        String newNickname = request.nickname();

        if (newNickname.equals(user.getNickname())) {
            return NicknameUpdateResponse.from(user);
        }

        if (userRepository.existsByNickname(newNickname)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        user.changeNickname(newNickname);
        return NicknameUpdateResponse.from(user);
    }

    @Transactional
    public void updatePassword(Long userId, UserPasswordUpdateRequest request) {
        User user = findUser(userId);

        if (user.getPassword() == null) {
            throw new BusinessException(ErrorCode.PASSWORD_NOT_SET);
        }

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.SAME_PASSWORD);
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }
}