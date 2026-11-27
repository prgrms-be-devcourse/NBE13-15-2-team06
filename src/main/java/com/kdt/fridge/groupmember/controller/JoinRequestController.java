package com.kdt.fridge.groupmember.controller;

import com.kdt.fridge.global.response.ApiResponse;
import com.kdt.fridge.groupmember.dto.FridgeJoinRequest;
import com.kdt.fridge.groupmember.dto.JoinRequestApproveResponse;
import com.kdt.fridge.groupmember.dto.JoinRequestResponse;
import com.kdt.fridge.groupmember.service.JoinRequestService;
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
@Tag(name = "JoinRequest", description = "냉장고 참여 신청")
public class JoinRequestController {
    private final JoinRequestService joinRequestService;

    @Operation(summary = "참여 신청", description = "초대 코드로 냉장고 참여 신청. 이미 구성원이거나 대기 중인 신청이 있으면 불가")
    @PostMapping("/api/v1/fridge-join-requests")
    public ResponseEntity<ApiResponse<JoinRequestResponse>> requestJoin(
            @RequestBody @Valid FridgeJoinRequest request,
            @AuthenticationPrincipal Long loginUserId
    ) {
        JoinRequestResponse response = joinRequestService.requestJoin(request.inviteCode(), loginUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "참여 신청이 접수되었습니다.", response));
    }

    @Operation(summary = "참여 신청 목록 조회", description = "대기 중인 신청을 신청 순으로 조회. 운영자(ADMIN)만 가능")
    @GetMapping("/api/v1/fridges/{fridgeId}/join-requests")
    public ResponseEntity<ApiResponse<List<JoinRequestResponse>>> getPendingRequests(
            @PathVariable Long fridgeId,
            @AuthenticationPrincipal Long loginUserId
    ) {
        List<JoinRequestResponse> requests = joinRequestService.getPendingRequests(fridgeId, loginUserId);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "참여 신청 목록을 조회했습니다.", requests));
    }

    @Operation(summary = "참여 신청 수락", description = "신청자를 일반 구성원으로 추가. 운영자(ADMIN)만 가능")
    @PostMapping("/api/v1/fridges/{fridgeId}/join-requests/{requestId}/approve")
    public ResponseEntity<ApiResponse<JoinRequestApproveResponse>> approve(
            @PathVariable Long fridgeId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal Long loginUserId
    ) {
        JoinRequestApproveResponse response = joinRequestService.approve(fridgeId, requestId, loginUserId);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "참여 신청을 수락했습니다.", response));
    }

    @Operation(summary = "참여 신청 거절", description = "운영자(ADMIN)만 가능")
    @PostMapping("/api/v1/fridges/{fridgeId}/join-requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<JoinRequestResponse>> reject(
            @PathVariable Long fridgeId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal Long loginUserId
    ) {
        JoinRequestResponse response = joinRequestService.reject(fridgeId, requestId, loginUserId);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "참여 신청을 거절했습니다.", response));
    }
}