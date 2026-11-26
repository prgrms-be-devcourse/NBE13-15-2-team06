package com.kdt.fridge.groupmember.controller;

import com.kdt.fridge.global.response.ApiResponse;
import com.kdt.fridge.groupmember.dto.FridgeMemberResponse;
import com.kdt.fridge.groupmember.dto.MemberRoleUpdateRequest;
import com.kdt.fridge.groupmember.service.FridgeMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TODO: X-User-Id 헤더는 테스트용 임시 방법. JWT 연동 후 로그인 사용자로 교체
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fridges/{fridgeId}/members")
public class FridgeMemberController {

    private final FridgeMemberService fridgeMemberService;

    // 구성원 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<FridgeMemberResponse>>> getMembers(
            @PathVariable Long fridgeId,
            @RequestHeader("X-User-Id") Long loginUserId
    ) {
        List<FridgeMemberResponse> members = fridgeMemberService.getMembers(fridgeId, loginUserId);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "구성원 목록을 조회했습니다.", members));
    }

    // 그룹 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<Void> leave(
            @PathVariable Long fridgeId,
            @RequestHeader("X-User-Id") Long loginUserId
    ) {
        fridgeMemberService.leave(fridgeId, loginUserId);
        return ResponseEntity.noContent().build();
    }

    // 구성원 추방
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> kick(
            @PathVariable Long fridgeId,
            @PathVariable Long memberId,
            @RequestHeader("X-User-Id") Long loginUserId
    ) {
        fridgeMemberService.kick(fridgeId, memberId, loginUserId);
        return ResponseEntity.noContent().build();
    }

    // 권한 부여·회수
    @PatchMapping("/{memberId}/role")
    public ResponseEntity<ApiResponse<FridgeMemberResponse>> changeRole(
            @PathVariable Long fridgeId,
            @PathVariable Long memberId,
            @RequestBody @Valid MemberRoleUpdateRequest request,
            @RequestHeader("X-User-Id") Long loginUserId
    ) {
        FridgeMemberResponse response =
                fridgeMemberService.changeRole(fridgeId, memberId, request.role(), loginUserId);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "구성원 권한을 변경했습니다.", response));
    }
}