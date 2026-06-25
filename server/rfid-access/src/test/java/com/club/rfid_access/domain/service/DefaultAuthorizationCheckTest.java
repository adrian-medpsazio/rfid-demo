package com.club.rfid_access.domain.service;

import com.club.rfid_access.domain.AuthorizationDecision;
import com.club.rfid_access.domain.AuthorizationRequest;
import com.club.rfid_access.infraestructure.persistence.entity.MemberEntity;
import com.club.rfid_access.infraestructure.persistence.entity.RfidTagEntity;
import com.club.rfid_access.infraestructure.persistence.repository.MemberRepository;
import com.club.rfid_access.infraestructure.persistence.repository.RfidTagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultAuthorizationCheckTest {

    @Mock
    private RfidTagRepository tagRepository;

    @Mock
    private MemberRepository memberRepository;

    private DefaultAuthorizationCheck authCheck;
    private AuthorizationRequest request;

    @BeforeEach
    void setUp() {
        authCheck = new DefaultAuthorizationCheck(tagRepository, memberRepository);
        request = new AuthorizationRequest("EPC-001", "GATE-01", Instant.now());
    }

    @Test
    void activeMember_shouldBeGranted() {
        var member = new MemberEntity();
        member.setActive(true);

        var tag = new RfidTagEntity();
        tag.setEpc("EPC-001");
        tag.setActive(true);
        tag.setMember(member);

        when(tagRepository.findByEpc("EPC-001")).thenReturn(Optional.of(tag));

        var result = authCheck.check(request);

        assertEquals(AuthorizationDecision.GRANTED, result.decision());
        assertEquals("MEMBER_ACTIVE", result.reason());
    }

    @Test
    void unknownTag_shouldBeDenied() {
        when(tagRepository.findByEpc("EPC-001")).thenReturn(Optional.empty());

        var result = authCheck.check(request);

        assertEquals(AuthorizationDecision.DENIED, result.decision());
        assertEquals("TAG_UNKNOWN", result.reason());
    }

    @Test
    void revokedTag_shouldBeDenied() {
        var tag = new RfidTagEntity();
        tag.setEpc("EPC-001");
        tag.setActive(false);

        when(tagRepository.findByEpc("EPC-001")).thenReturn(Optional.of(tag));

        var result = authCheck.check(request);

        assertEquals(AuthorizationDecision.DENIED, result.decision());
        assertEquals("TAG_REVOKED", result.reason());
    }

    @Test
    void inactiveMember_shouldBeDenied() {
        var member = new MemberEntity();
        member.setActive(false);

        var tag = new RfidTagEntity();
        tag.setEpc("EPC-001");
        tag.setActive(true);
        tag.setMember(member);

        when(tagRepository.findByEpc("EPC-001")).thenReturn(Optional.of(tag));

        var result = authCheck.check(request);

        assertEquals(AuthorizationDecision.DENIED, result.decision());
        assertEquals("MEMBER_INACTIVE", result.reason());
    }
}
