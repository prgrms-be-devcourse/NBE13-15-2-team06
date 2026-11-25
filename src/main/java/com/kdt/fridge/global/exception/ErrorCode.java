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
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLLICATE_EMAIL", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다."),

    INGREDIENT_GROUP_MEMBER_REQUIRED(
            HttpStatus.FORBIDDEN, "INGREDIENT_GROUP_MEMBER_REQUIRED",
            "냉장고 그룹 구성원이 아닙니다."),
    INGREDIENT_NOT_FOUND(
            HttpStatus.NOT_FOUND, "INGREDIENT_NOT_FOUND",
            "식재료를 찾을 수 없습니다."),
    INGREDIENT_CONSUMPTION_FORBIDDEN(
            HttpStatus.FORBIDDEN, "INGREDIENT_CONSUMPTION_FORBIDDEN",
            "이 식재료를 소비할 권한이 없습니다."),
    INGREDIENT_NOT_STORED(
            HttpStatus.CONFLICT, "INGREDIENT_NOT_STORED",
            "보관 중인 식재료만 소비할 수 있습니다."),
    INGREDIENT_INSUFFICIENT_QUANTITY(
            HttpStatus.CONFLICT, "INGREDIENT_INSUFFICIENT_QUANTITY",
            "남은 수량보다 많이 소비할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}