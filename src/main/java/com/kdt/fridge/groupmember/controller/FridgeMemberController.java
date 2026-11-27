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

/**
 * 냉장고 구성원(FridgeMember) 관리 API 컨트롤러.
 * <p>기본 경로 {@code /api/v1/fridges/{fridgeId}/members} 아래에서 구성원 목록 조회, 본인 탈퇴,
 * 운영자의 구성원 강퇴, 구성원 권한 변경을 제공한다.
 * <p>로그인 사용자 ID는 {@code @AuthenticationPrincipal}로 받아 서비스에 넘기며,
 * 구성원 여부·운영자 여부 같은 실제 권한 검사와 비즈니스 규칙은 모두 {@link FridgeMemberService}가 담당한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fridges/{fridgeId}/members")
@Tag(name = "Member", description = "냉장고 구성원 관리")
public class FridgeMemberController {
    private final FridgeMemberService fridgeMemberService;

    /**
     * [GET] 냉장고 구성원 목록 조회.
     * 해당 냉장고의 구성원만 호출할 수 있으며, 가입 순(joinedAt, id 오름차순)으로 정렬해 200 OK로 반환한다.
     */
    @Operation(summary = "구성원 목록 조회", description = "냉장고 구성원을 가입 순으로 조회. 구성원만 가능")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FridgeMemberResponse>>> getMembers(
            @PathVariable Long fridgeId,
            @AuthenticationPrincipal Long loginUserId
    ) {
        List<FridgeMemberResponse> members = fridgeMemberService.getMembers(fridgeId, loginUserId);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "구성원 목록을 조회했습니다.", members));
    }

    /**
     * [DELETE] 로그인한 본인이 냉장고에서 탈퇴한다. 경로 {@code /me}로 '나 자신'을 가리킨다.
     * 마지막 운영자(ADMIN)는 탈퇴할 수 없고, 마지막 남은 구성원이 나가면 냉장고 자체가 삭제된다.
     * 성공 시 본문 없이 204 No Content를 반환한다.
     */
    @Operation(summary = "냉장고 탈퇴", description = "본인이 냉장고에서 나감. 마지막 운영자(ADMIN)는 탈퇴 불가")
    @DeleteMapping("/me")
    public ResponseEntity<Void> leave(
            @PathVariable Long fridgeId,
            @AuthenticationPrincipal Long loginUserId
    ) {
        fridgeMemberService.leave(fridgeId, loginUserId);
        return ResponseEntity.noContent().build();
    }

    /**
     * [DELETE] 운영자(ADMIN)가 특정 구성원을 냉장고에서 내보낸다.
     * {@code memberId}는 사용자 ID가 아니라 구성원(FridgeMember) 레코드의 ID이며, 자기 자신은 강퇴할 수 없다.
     * 성공 시 204 No Content를 반환한다.
     */
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

    /**
     * [PATCH] 운영자(ADMIN)가 특정 구성원의 역할(ADMIN/MEMBER)을 변경한다.
     * 마지막 운영자를 일반 구성원으로 강등하는 요청은 거부된다.
     * 변경된 구성원 정보를 200 OK로 반환한다.
     */
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