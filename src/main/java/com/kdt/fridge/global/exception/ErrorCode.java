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

    //냉장고 관리 에러코드
    FRIDGE_NOT_FOUND(HttpStatus.NOT_FOUND, "FRIDGE_NOT_FOUND", "냉장고를 찾을 수 없습니다."),
    NOT_FRIDGE_MEMBER(HttpStatus.FORBIDDEN, "NOT_FRIDGE_MEMBER", "해당 냉장고의 구성원이 아닙니다."),
    NOT_FRIDGE_ADMIN(HttpStatus.FORBIDDEN,"NOT_FRIDGE_ADMIN" ,"해당 냉장고의 관리자가 아닙니다." );

    private final HttpStatus status;
    private final String code;
    private final String message;
}