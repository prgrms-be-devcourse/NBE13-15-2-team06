package com.kdt.fridge.groupmember.controller;

import com.kdt.fridge.global.response.ApiResponse;
import com.kdt.fridge.groupmember.dto.FridgeMemberResponse;
import com.kdt.fridge.groupmember.dto.MemberRoleUpdateRequest;
import com.kdt.fridge.groupmember.service.FridgeMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fridges/{fridgeId}/members")
@Tag(name = "Member", description = "냉장고 구성원 관리")
public class FridgeMemberController {
    private final FridgeMemberService fridgeMemberService;

    @Operation(summary = "구성원 목록 조회", description = "냉장고 구성원을 가입 순으로 조회. 구성원만 가능")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FridgeMemberResponse>>> getMembers(
            @PathVariable Long fridgeId,
            @AuthenticationPrincipal Long loginUserId
    ) {
        List<FridgeMemberResponse> members = fridgeMemberService.getMembers(fridgeId, loginUserId);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "구성원 목록을 조회했습니다.", members));
    }

    @Operation(summary = "냉장고 탈퇴", description = "본인이 냉장고에서 나감. 마지막 운영자(ADMIN)는 탈퇴 불가")
    @DeleteMapping("/me")
    public ResponseEntity<Void> leave(
            @PathVariable Long fridgeId,
            @AuthenticationPrincipal Long loginUserId
    ) {
        fridgeMemberService.leave(fridgeId, loginUserId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "구성원 강퇴", description = "운영자(ADMIN)만 가능. 자기 자신은 강퇴 불가")
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> kick(
            @PathVariable Long fridgeId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal Long loginUserId
    ) {
        fridgeMemberService.kick(fridgeId, memberId, loginUserId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "구성원 권한 변경", description = "운영자(ADMIN)만 가능. 마지막 운영자는 일반 구성원으로 변경 불가")
    @PatchMapping("/{memberId}/role")
    public ResponseEntity<ApiResponse<FridgeMemberResponse>> changeRole(
            @PathVariable Long fridgeId,
            @PathVariable Long memberId,
            @RequestBody @Valid MemberRoleUpdateRequest request,
            @AuthenticationPrincipal Long loginUserId
    ) {
        FridgeMemberResponse response =
                fridgeMemberService.changeRole(fridgeId, memberId, request.role(), loginUserId);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "구성원 권한을 변경했습니다.", response));
    }
}