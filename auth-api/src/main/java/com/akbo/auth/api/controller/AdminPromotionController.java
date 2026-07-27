package com.akbo.auth.api.controller;

import static org.springframework.http.ResponseEntity.ok;
import com.akbo.auth.api.service.RoleUpdateRequestService;
import com.akbo.auth.dto.RoleUpdateRequestDto;
import com.akbo.auth.dto.RoleUpdateRequestResolutionDto;
import com.akbo.auth.dto.RoleUpdateRequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("admin/promotions")
@RequiredArgsConstructor
public class AdminPromotionController {

    private final RoleUpdateRequestService roleUpdateRequestService;

    @GetMapping
    public ResponseEntity<List<RoleUpdateRequestDto>> listPromotionRequests(
            @RequestParam(value = "status", required = false)
                    final RoleUpdateRequestStatus status) {
        return ok(roleUpdateRequestService.listRequests(status));
    }

    @PostMapping("{requestId}/approve")
    public ResponseEntity<RoleUpdateRequestDto> approvePromotionRequest(
            @PathVariable("requestId") final Long requestId,
            @AuthenticationPrincipal final Jwt jwt,
            @RequestBody(required = false) final RoleUpdateRequestResolutionDto resolution) {
        return ok(
                roleUpdateRequestService.approveRequest(
                        requestId, jwt.getSubject(), normalizeResolution(resolution)));
    }

    @PostMapping("{requestId}/reject")
    public ResponseEntity<RoleUpdateRequestDto> rejectPromotionRequest(
            @PathVariable("requestId") final Long requestId,
            @AuthenticationPrincipal final Jwt jwt,
            @RequestBody(required = false) final RoleUpdateRequestResolutionDto resolution) {
        return ok(
                roleUpdateRequestService.rejectRequest(
                        requestId, jwt.getSubject(), normalizeResolution(resolution)));
    }

    private RoleUpdateRequestResolutionDto normalizeResolution(
            final RoleUpdateRequestResolutionDto resolution) {
        return resolution == null ? new RoleUpdateRequestResolutionDto() : resolution;
    }
}
