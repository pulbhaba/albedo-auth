package com.akbo.auth.api.service.impl;

import com.akbo.auth.api.service.RoleUpdateRequestService;
import com.akbo.auth.dao.entity.RoleUpdateRequest;
import com.akbo.auth.dao.entity.User;
import com.akbo.auth.dao.entity.UserRole;
import com.akbo.auth.dao.repository.RoleUpdateRequestRepository;
import com.akbo.auth.dao.repository.UserRepository;
import com.akbo.auth.dto.Role;
import com.akbo.auth.dto.RoleUpdateRequestCreateDto;
import com.akbo.auth.dto.RoleUpdateRequestDto;
import com.akbo.auth.dto.RoleUpdateRequestResolutionDto;
import com.akbo.auth.dto.RoleUpdateRequestStatus;
import com.akbo.auth.exception.BadRequestException;
import com.akbo.auth.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleUpdateRequestServiceImpl implements RoleUpdateRequestService {

    private static final String MISSING_REQUEST = "Role update request was not found.";
    private static final String REQUEST_NOT_PENDING = "Role update request is already resolved.";

    private final RoleUpdateRequestRepository roleUpdateRequestRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Override
    @Transactional
    public RoleUpdateRequestDto createRequest(
            final String username, final RoleUpdateRequestCreateDto request) {
        validateRequest(request);
        final User user = loadUser(username);

        if (hasRole(user, request.getRequestedRole())) {
            throw new BadRequestException("User already has the requested role.");
        }

        if (roleUpdateRequestRepository.existsByUserUsernameAndRequestedRoleAndStatus(
                username, request.getRequestedRole(), RoleUpdateRequestStatus.PENDING)) {
            throw new BadRequestException("A pending role update request already exists.");
        }

        final RoleUpdateRequest roleUpdateRequest = new RoleUpdateRequest();
        roleUpdateRequest.setUser(user);
        roleUpdateRequest.setRequestedRole(request.getRequestedRole());
        roleUpdateRequest.setEvidence(request.getEvidence());
        roleUpdateRequest.setStatus(RoleUpdateRequestStatus.PENDING);
        return mapToDto(roleUpdateRequestRepository.save(roleUpdateRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleUpdateRequestDto> listRequestsForUser(final String username) {
        return roleUpdateRequestRepository
                .findByUserUsernameOrderByCreatedTimeDesc(username)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleUpdateRequestDto> listRequests(final RoleUpdateRequestStatus status) {
        final List<RoleUpdateRequest> requests =
                status == null
                        ? roleUpdateRequestRepository.findAllByOrderByCreatedTimeDesc()
                        : roleUpdateRequestRepository.findByStatusOrderByCreatedTimeDesc(status);
        return requests.stream().map(this::mapToDto).toList();
    }

    @Override
    @Transactional
    public RoleUpdateRequestDto approveRequest(
            final Long requestId,
            final String adminUsername,
            final RoleUpdateRequestResolutionDto resolution) {
        final RoleUpdateRequest request = loadPendingRequest(requestId);
        final User user = request.getUser();

        if (!hasRole(user, request.getRequestedRole())) {
            if (user.getAuthorities() == null) {
                user.setAuthorities(new HashSet<>());
            }
            user.getAuthorities().add(new UserRole(request.getRequestedRole()));
            userRepository.save(user);
        }

        resolve(request, RoleUpdateRequestStatus.APPROVED, adminUsername, resolution);
        return mapToDto(roleUpdateRequestRepository.save(request));
    }

    @Override
    @Transactional
    public RoleUpdateRequestDto rejectRequest(
            final Long requestId,
            final String adminUsername,
            final RoleUpdateRequestResolutionDto resolution) {
        final RoleUpdateRequest request = loadPendingRequest(requestId);
        resolve(request, RoleUpdateRequestStatus.REJECTED, adminUsername, resolution);
        return mapToDto(roleUpdateRequestRepository.save(request));
    }

    private void validateRequest(final RoleUpdateRequestCreateDto request) {
        if (request == null || request.getRequestedRole() == null) {
            throw new BadRequestException("requestedRole is required.");
        }

        if (request.getRequestedRole() != Role.ROLE_EDITOR) {
            throw new BadRequestException("Only ROLE_EDITOR requests are supported.");
        }
    }

    private RoleUpdateRequest loadPendingRequest(final Long requestId) {
        final RoleUpdateRequest request =
                roleUpdateRequestRepository
                        .findById(requestId)
                        .orElseThrow(() -> new NotFoundException(MISSING_REQUEST));
        if (request.getStatus() != RoleUpdateRequestStatus.PENDING) {
            throw new BadRequestException(REQUEST_NOT_PENDING);
        }
        return request;
    }

    private User loadUser(final String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User was not found."));
    }

    private boolean hasRole(final User user, final Role role) {
        return user.getAuthorities() != null
                && user.getAuthorities().stream()
                        .anyMatch(authority -> authority.getRole() == role);
    }

    private void resolve(
            final RoleUpdateRequest request,
            final RoleUpdateRequestStatus status,
            final String adminUsername,
            final RoleUpdateRequestResolutionDto resolution) {
        request.setStatus(status);
        request.setResolvedBy(adminUsername);
        request.setResolvedAt(LocalDateTime.now(clock));
        request.setResolutionReason(resolution == null ? null : resolution.getReason());
    }

    private RoleUpdateRequestDto mapToDto(final RoleUpdateRequest request) {
        final User user = request.getUser();
        final RoleUpdateRequestDto dto = new RoleUpdateRequestDto();
        dto.setId(request.getId());
        dto.setCreatedTime(request.getCreatedTime());
        dto.setLastUpdatedTime(request.getLastUpdatedTime());
        dto.setCreatedBy(request.getCreatedBy());
        dto.setLastUpdatedBy(request.getLastUpdatedBy());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRequestedRole(request.getRequestedRole());
        dto.setEvidence(request.getEvidence());
        dto.setStatus(request.getStatus());
        dto.setResolvedBy(request.getResolvedBy());
        dto.setResolvedAt(request.getResolvedAt());
        dto.setResolutionReason(request.getResolutionReason());
        return dto;
    }
}
