package com.akbo.auth.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.akbo.auth.api.service.RoleUpdateRequestService;
import com.akbo.auth.dto.RoleUpdateRequestDto;
import com.akbo.auth.dto.RoleUpdateRequestResolutionDto;
import com.akbo.auth.dto.RoleUpdateRequestStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class AdminPromotionControllerTest {

    @Mock private RoleUpdateRequestService roleUpdateRequestService;

    @InjectMocks private AdminPromotionController controller;

    @Test
    void listPromotionRequests_delegatesWithStatusFilter() {
        RoleUpdateRequestDto dto = new RoleUpdateRequestDto();
        when(roleUpdateRequestService.listRequests(RoleUpdateRequestStatus.PENDING))
                .thenReturn(List.of(dto));

        ResponseEntity<List<RoleUpdateRequestDto>> response =
                controller.listPromotionRequests(RoleUpdateRequestStatus.PENDING);

        assertEquals(List.of(dto), response.getBody());
    }

    @Test
    void approvePromotionRequest_usesAuthenticatedAdminSubject() {
        Jwt jwt = jwt("admin");
        RoleUpdateRequestResolutionDto resolution = new RoleUpdateRequestResolutionDto();
        resolution.setReason("reviewed");
        RoleUpdateRequestDto dto = new RoleUpdateRequestDto();
        dto.setStatus(RoleUpdateRequestStatus.APPROVED);
        when(roleUpdateRequestService.approveRequest(eq(12L), eq("admin"), eq(resolution)))
                .thenReturn(dto);

        ResponseEntity<RoleUpdateRequestDto> response =
                controller.approvePromotionRequest(12L, jwt, resolution);

        assertEquals(RoleUpdateRequestStatus.APPROVED, response.getBody().getStatus());
    }

    @Test
    void rejectPromotionRequest_usesEmptyResolutionWhenBodyIsMissing() {
        Jwt jwt = jwt("admin");
        RoleUpdateRequestDto dto = new RoleUpdateRequestDto();
        dto.setStatus(RoleUpdateRequestStatus.REJECTED);
        when(roleUpdateRequestService.rejectRequest(eq(12L), eq("admin"), any())).thenReturn(dto);

        ResponseEntity<RoleUpdateRequestDto> response =
                controller.rejectPromotionRequest(12L, jwt, null);

        assertEquals(RoleUpdateRequestStatus.REJECTED, response.getBody().getStatus());
        verify(roleUpdateRequestService).rejectRequest(eq(12L), eq("admin"), any());
    }

    private Jwt jwt(final String subject) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .claim("roles", List.of("ROLE_ADMIN"))
                .build();
    }
}
