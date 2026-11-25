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


    //구성원 에러
    ALREADY_FRIDGE_MEMBER(HttpStatus.CONFLICT, "ALREADY_FRIDGE_MEMBER", "이미 냉장고 구성원입니다."),
    CANNOT_KICK_SELF(HttpStatus.BAD_REQUEST, "CANNOT_KICK_SELF", "자기 자신은 추방할 수 없습니다. 탈퇴를 이용해 주세요."),
    DUPLICATE_JOIN_REQUEST(HttpStatus.CONFLICT, "DUPLICATE_JOIN_REQUEST", "이미 처리 대기 중인 참여 신청이 있습니다."),
    INVALID_INVITE_CODE(HttpStatus.NOT_FOUND, "INVALID_INVITE_CODE", "유효하지 않은 초대 코드입니다."),
    JOIN_REQUEST_ALREADY_PROCESSED(HttpStatus.CONFLICT, "JOIN_REQUEST_ALREADY_PROCESSED", "이미 처리된 참여 신청입니다."),
    JOIN_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "JOIN_REQUEST_NOT_FOUND", "참여 신청을 찾을 수 없습니다."),
    LAST_ADMIN_REQUIRED(HttpStatus.CONFLICT, "LAST_ADMIN_REQUIRED", "냉장고에는 관리자가 최소 1명 있어야 합니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "해당 구성원을 찾을 수 없습니다."),
    NOT_FRIDGE_ADMIN(HttpStatus.FORBIDDEN, "NOT_FRIDGE_ADMIN", "관리자만 할 수 있는 기능입니다."),
    NOT_FRIDGE_MEMBER(HttpStatus.FORBIDDEN, "NOT_FRIDGE_MEMBER", "해당 냉장고의 구성원이 아닙니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}