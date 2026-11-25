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
            "남은 수량보다 많이 소비할 수 없습니다."),
    INGREDIENT_STATUS_UPDATE_FORBIDDEN(
            HttpStatus.FORBIDDEN, "INGREDIENT_STATUS_UPDATE_FORBIDDEN",
            "이 식재료의 상태를 변경할 권한이 없습니다."),
    INGREDIENT_INVALID_STATUS_TRANSITION(
            HttpStatus.CONFLICT, "INGREDIENT_INVALID_STATUS_TRANSITION",
            "현재 상태에서 요청한 상태로 변경할 수 없습니다."),
    INGREDIENT_INVALID_STORED_QUANTITY(
            HttpStatus.CONFLICT, "INGREDIENT_INVALID_STORED_QUANTITY",
            "저장된 식재료 수량이 올바르지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH", "현재 비밀번호가 일치하지 않습니다."),
    SAME_PASSWORD(HttpStatus.BAD_REQUEST, "SAME_PASSWORD", "새 비밀번호가 현재 비밀번호와 같습니다."),
    PASSWORD_NOT_SET(HttpStatus.BAD_REQUEST, "PASSWORD_NOT_SET", "비밀번호가 없는 계정입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}