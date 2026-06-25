package com.club.rfid_access.domain;

public interface AuthorizationCheck {
    AuthorizationResult check(AuthorizationRequest request);
}
