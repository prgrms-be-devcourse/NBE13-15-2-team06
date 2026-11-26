package com.kdt.fridge.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "입력값이 올바르지 않습니다."),

    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "LOGIN_FAILED", "이메일 또는 비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "존재하지 않는 회원입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다."),

    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_NOT_FOUND", "존재하지 않는 알림입니다."),
    NOTIFICATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "NOTIFICATION_ACCESS_DENIED", "본인 알림이 아닙니다."),
    NOTIFICATION_SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_SETTING_NOT_FOUND", "알림 설정이 존재하지 않습니다."),

    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH", "현재 비밀번호가 일치하지 않습니다."),
    SAME_PASSWORD(HttpStatus.BAD_REQUEST, "SAME_PASSWORD", "새 비밀번호가 현재 비밀번호와 같습니다."),
    PASSWORD_NOT_SET(HttpStatus.BAD_REQUEST, "PASSWORD_NOT_SET", "비밀번호가 없는 계정입니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}