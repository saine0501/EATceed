package com.gaebaljip.exceed.member.application.port.in;

import com.gaebaljip.exceed.common.annotation.UseCase;

@UseCase
public interface DeleteMemberUseCase {
    void execute(Long memberId);
}
