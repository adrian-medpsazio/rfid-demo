package com.club.rfid_access.domain.service;

import com.club.rfid_access.domain.AuthorizationCheck;
import com.club.rfid_access.domain.AuthorizationDecision;
import com.club.rfid_access.domain.AuthorizationRequest;
import com.club.rfid_access.domain.AuthorizationResult;
import com.club.rfid_access.infraestructure.persistence.repository.MemberRepository;
import com.club.rfid_access.infraestructure.persistence.repository.RfidTagRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DefaultAuthorizationCheck implements AuthorizationCheck {

    private final RfidTagRepository tagRepository;
    private final MemberRepository memberRepository;

    public DefaultAuthorizationCheck(RfidTagRepository tagRepository, MemberRepository memberRepository) {
        this.tagRepository = tagRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public AuthorizationResult check(AuthorizationRequest request) {
        var tagOpt = tagRepository.findByEpc(request.epc());

        if (tagOpt.isEmpty()) {
            return new AuthorizationResult(AuthorizationDecision.DENIED, "TAG_UNKNOWN", Instant.now());
        }

        var tag = tagOpt.get();

        if (!tag.isActive()) {
            return new AuthorizationResult(AuthorizationDecision.DENIED, "TAG_REVOKED", Instant.now());
        }

        if (tag.getMember() == null) {
            return new AuthorizationResult(AuthorizationDecision.DENIED, "TAG_UNASSIGNED", Instant.now());
        }

        var member = tag.getMember();

        if (!member.isActive()) {
            return new AuthorizationResult(AuthorizationDecision.DENIED, "MEMBER_INACTIVE", Instant.now());
        }

        return new AuthorizationResult(AuthorizationDecision.GRANTED, "MEMBER_ACTIVE", Instant.now());
    }
}
