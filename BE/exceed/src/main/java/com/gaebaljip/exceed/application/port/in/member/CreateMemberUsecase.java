package com.gaebaljip.exceed.application.port.in.member;

import org.springframework.stereotype.Component;

import com.gaebaljip.exceed.dto.request.SignUpMemberRequest;

@Component
public interface CreateMemberUsecase {
    void execute(SignUpMemberRequest signUpMemberRequest);
}
