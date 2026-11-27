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

/**
 * 냉장고 참여 신청(JoinRequest) API 컨트롤러.
 * <p>초대 코드로 참여를 신청하는 흐름과, 운영자가 신청 목록을 조회하고 수락/거절하는 흐름을 담당한다.
 * 신청 생성은 {@code /api/v1/fridge-join-requests}, 신청 조회·처리는 냉장고 하위 경로
 * {@code /api/v1/fridges/{fridgeId}/join-requests}를 사용하므로 클래스 레벨 {@code @RequestMapping} 없이
 * 메서드마다 전체 경로를 지정한다. 실제 검증과 처리는 {@link JoinRequestService}에 위임한다.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "JoinRequest", description = "냉장고 참여 신청")
public class JoinRequestController {
    private final JoinRequestService joinRequestService;

    /**
     * [POST] 초대 코드로 냉장고 참여를 신청한다.
     * 초대 코드가 올바르지 않거나, 이미 구성원이거나, 대기(PENDING) 중인 신청이 있으면 실패한다.
     * 성공 시 생성된 신청 정보를 201 Created로 반환한다.
     */
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

    /**
     * [GET] 해당 냉장고에 들어온 대기(PENDING) 상태의 참여 신청을 신청 시각 순으로 조회한다.
     * 운영자(ADMIN)만 호출할 수 있다.
     */
    @Operation(summary = "참여 신청 목록 조회", description = "대기 중인 신청을 신청 순으로 조회. 운영자(ADMIN)만 가능")
    @GetMapping("/api/v1/fridges/{fridgeId}/join-requests")
    public ResponseEntity<ApiResponse<List<JoinRequestResponse>>> getPendingRequests(
            @PathVariable Long fridgeId,
            @AuthenticationPrincipal Long loginUserId
    ) {
        List<JoinRequestResponse> requests = joinRequestService.getPendingRequests(fridgeId, loginUserId);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "참여 신청 목록을 조회했습니다.", requests));
    }

    /**
     * [POST] 참여 신청을 수락한다. 신청 상태를 APPROVED로 바꾸고 신청자를 일반 구성원(MEMBER)으로 추가한다.
     * 운영자(ADMIN)만 호출할 수 있으며, 신청 결과와 새 구성원 정보를 함께 반환한다.
     */
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

    /**
     * [POST] 참여 신청을 거절한다. 신청 상태만 REJECTED로 바꾸며 구성원은 추가되지 않는다.
     * 운영자(ADMIN)만 호출할 수 있다.
     */
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