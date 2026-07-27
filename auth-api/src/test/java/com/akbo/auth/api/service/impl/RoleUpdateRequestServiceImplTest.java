package com.akbo.auth.api.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class RoleUpdateRequestServiceImplTest {

    @Mock private RoleUpdateRequestRepository roleUpdateRequestRepository;
    @Mock private UserRepository userRepository;

    private RoleUpdateRequestServiceImpl service;
    private User user;

    @BeforeEach
    void setUp() {
        service =
                new RoleUpdateRequestServiceImpl(
                        roleUpdateRequestRepository,
                        userRepository,
                        Clock.fixed(Instant.parse("2026-07-27T01:02:03Z"), ZoneOffset.UTC));

        user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setFirstName("Alice");
        user.setLastName("Reader");
        user.setAuthorities(new HashSet<>(Set.of(new UserRole(Role.ROLE_USER))));
    }

    @Test
    void createRequest_createsPendingEditorRequest() {
        RoleUpdateRequestCreateDto createDto = createDto(Role.ROLE_EDITOR, "I write novels.");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(roleUpdateRequestRepository.existsByUserUsernameAndRequestedRoleAndStatus(
                        "alice", Role.ROLE_EDITOR, RoleUpdateRequestStatus.PENDING))
                .thenReturn(false);
        when(roleUpdateRequestRepository.save(any(RoleUpdateRequest.class)))
                .thenAnswer(
                        invocation -> {
                            RoleUpdateRequest saved = invocation.getArgument(0);
                            saved.setId(44L);
                            return saved;
                        });

        RoleUpdateRequestDto result = service.createRequest("alice", createDto);

        assertEquals(44L, result.getId());
        assertEquals("alice", result.getUsername());
        assertEquals(Role.ROLE_EDITOR, result.getRequestedRole());
        assertEquals(RoleUpdateRequestStatus.PENDING, result.getStatus());
        assertEquals("I write novels.", result.getEvidence());
    }

    @Test
    void createRequest_rejectsDuplicatePendingRequest() {
        RoleUpdateRequestCreateDto createDto = createDto(Role.ROLE_EDITOR, "I write novels.");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(roleUpdateRequestRepository.existsByUserUsernameAndRequestedRoleAndStatus(
                        "alice", Role.ROLE_EDITOR, RoleUpdateRequestStatus.PENDING))
                .thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.createRequest("alice", createDto));
        verify(roleUpdateRequestRepository, never()).save(any());
    }

    @Test
    void createRequest_rejectsUnsupportedRole() {
        RoleUpdateRequestCreateDto createDto = createDto(Role.ROLE_ADMIN, "admin please");

        assertThrows(BadRequestException.class, () -> service.createRequest("alice", createDto));
        verifyNoInteractions(userRepository);
    }

    @Test
    void createRequest_rejectsRoleAlreadyHeldByUser() {
        user.getAuthorities().add(new UserRole(Role.ROLE_EDITOR));
        RoleUpdateRequestCreateDto createDto = createDto(Role.ROLE_EDITOR, "already have it");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> service.createRequest("alice", createDto));
        verify(roleUpdateRequestRepository, never()).save(any());
    }

    @Test
    void approveRequest_grantsRequestedRoleAndResolvesRequest() {
        RoleUpdateRequest request = pendingRequest();
        RoleUpdateRequestResolutionDto resolution = new RoleUpdateRequestResolutionDto();
        resolution.setReason("approved");
        when(roleUpdateRequestRepository.findById(44L)).thenReturn(Optional.of(request));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(roleUpdateRequestRepository.save(any(RoleUpdateRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RoleUpdateRequestDto result = service.approveRequest(44L, "admin", resolution);

        assertEquals(RoleUpdateRequestStatus.APPROVED, result.getStatus());
        assertEquals("admin", result.getResolvedBy());
        assertEquals(LocalDateTime.of(2026, 7, 27, 1, 2, 3), result.getResolvedAt());
        assertEquals("approved", result.getResolutionReason());
        assertTrue(user.getAuthorities().contains(new UserRole(Role.ROLE_EDITOR)));
    }

    @Test
    void rejectRequest_resolvesWithoutGrantingRole() {
        RoleUpdateRequest request = pendingRequest();
        RoleUpdateRequestResolutionDto resolution = new RoleUpdateRequestResolutionDto();
        resolution.setReason("not enough evidence");
        when(roleUpdateRequestRepository.findById(44L)).thenReturn(Optional.of(request));
        when(roleUpdateRequestRepository.save(any(RoleUpdateRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RoleUpdateRequestDto result = service.rejectRequest(44L, "admin", resolution);

        assertEquals(RoleUpdateRequestStatus.REJECTED, result.getStatus());
        assertEquals("not enough evidence", result.getResolutionReason());
        assertFalse(user.getAuthorities().contains(new UserRole(Role.ROLE_EDITOR)));
        verify(userRepository, never()).save(any());
    }

    @Test
    void approveRequest_rejectsAlreadyResolvedRequest() {
        RoleUpdateRequest request = pendingRequest();
        request.setStatus(RoleUpdateRequestStatus.APPROVED);
        when(roleUpdateRequestRepository.findById(44L)).thenReturn(Optional.of(request));

        assertThrows(
                BadRequestException.class,
                () -> service.approveRequest(44L, "admin", new RoleUpdateRequestResolutionDto()));
    }

    private RoleUpdateRequestCreateDto createDto(final Role requestedRole, final String evidence) {
        RoleUpdateRequestCreateDto dto = new RoleUpdateRequestCreateDto();
        dto.setRequestedRole(requestedRole);
        dto.setEvidence(evidence);
        return dto;
    }

    private RoleUpdateRequest pendingRequest() {
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setId(44L);
        request.setUser(user);
        request.setRequestedRole(Role.ROLE_EDITOR);
        request.setEvidence("I write novels.");
        request.setStatus(RoleUpdateRequestStatus.PENDING);
        return request;
    }
}
