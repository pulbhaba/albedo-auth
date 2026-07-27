package com.akbo.auth.api.service;

import com.akbo.auth.dto.RoleUpdateRequestCreateDto;
import com.akbo.auth.dto.RoleUpdateRequestDto;
import com.akbo.auth.dto.RoleUpdateRequestResolutionDto;
import com.akbo.auth.dto.RoleUpdateRequestStatus;

import java.util.List;

public interface RoleUpdateRequestService {

    RoleUpdateRequestDto createRequest(String username, RoleUpdateRequestCreateDto request);

    List<RoleUpdateRequestDto> listRequestsForUser(String username);

    List<RoleUpdateRequestDto> listRequests(RoleUpdateRequestStatus status);

    RoleUpdateRequestDto approveRequest(
            Long requestId, String adminUsername, RoleUpdateRequestResolutionDto resolution);

    RoleUpdateRequestDto rejectRequest(
            Long requestId, String adminUsername, RoleUpdateRequestResolutionDto resolution);
}
