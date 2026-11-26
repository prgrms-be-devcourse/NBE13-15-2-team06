package com.kdt.fridge.groupmember.controller;

import com.kdt.fridge.global.response.ApiResponse;
import com.kdt.fridge.groupmember.dto.FridgeMemberResponse;
import com.kdt.fridge.groupmember.dto.MemberRoleUpdateRequest;
import com.kdt.fridge.groupmember.service.FridgeMemberService;
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
public class FridgeMemberController {
    private final FridgeMemberService fridgeMemberService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FridgeMemberResponse>>> getMembers(
            @PathVariable Long fridgeId,
            @AuthenticationPrincipal Long loginUserId
    ) {
        List<FridgeMemberResponse> members = fridgeMemberService.getMembers(fridgeId, loginUserId);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "구성원 목록을 조회했습니다.", members));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> leave(
            @PathVariable Long fridgeId,
            @AuthenticationPrincipal Long loginUserId
    ) {
        fridgeMemberService.leave(fridgeId, loginUserId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> kick(
            @PathVariable Long fridgeId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal Long loginUserId
    ) {
        fridgeMemberService.kick(fridgeId, memberId, loginUserId);
        return ResponseEntity.noContent().build();
    }

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