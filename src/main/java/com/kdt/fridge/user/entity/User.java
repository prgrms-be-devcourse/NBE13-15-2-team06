package com.kdt.fridge.user.entity;

import com.kdt.fridge.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, columnDefinition = "VARCHAR(100) COLLATE utf8mb4_bin")
    private String email;

    @Column(length = 255)
    private String password;

    @Column(unique = true)
    private Long kakaoId;

    @Column(unique = true, columnDefinition = "VARCHAR(30) COLLATE utf8mb4_bin")
    private String nickname;

    private User(String email, String password, Long kakaoId) {
        this.email = email;
        this.password = password;
        this.kakaoId = kakaoId;
    }

    public static User createLocal(String email, String encodedPassword) {
        return new User(email, encodedPassword, null);
    }

    public LoginType getLoginType() {
        return kakaoId != null ? LoginType.KAKAO : LoginType.LOCAL;
    }

    public boolean isNicknameRequired() {
        return nickname == null;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}