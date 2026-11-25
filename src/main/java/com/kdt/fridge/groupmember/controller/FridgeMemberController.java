package com.kdt.fridge.groupmember.controller;

import com.kdt.fridge.global.response.ApiResponse;
import com.kdt.fridge.groupmember.dto.FridgeMemberResponse;
import com.kdt.fridge.groupmember.entity.FridgeMember;
import com.kdt.fridge.groupmember.service.FridgeMemberService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestHeader("X-User-Id") Long loginUserId
    ) {
        List<FridgeMemberResponse> members = fridgeMemberService.getMembers(fridgeId, loginUserId);
        return ResponseEntity.ok(
                ApiResponse.of(HttpStatus.OK, "구성원 목록을 조회했습니다", members)
        );
    }
}
