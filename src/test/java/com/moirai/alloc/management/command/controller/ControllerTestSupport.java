package com.moirai.alloc.management.command.controller;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.management.command.service.SelectAssignmentCandidates;
import com.moirai.alloc.management.query.service.GetAssignedMembers;
import com.moirai.alloc.management.query.service.GetAssignedStatus;
import com.moirai.alloc.management.query.service.GetAssignmentCandidates;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
@Import(TestSecurityConfig.class)
public abstract class ControllerTestSupport {
    @MockBean protected GetAssignedMembers getAssignedMembers;
    @MockBean protected GetAssignmentCandidates getAssignmentCandidates;
    @MockBean protected GetAssignedStatus getAssignedStatus;
    @MockBean protected SelectAssignmentCandidates selectAssignmentCandidates;

    //  Stage 2 관점
//  - Controller + Security Context + Role 기반 접근 제어를
//   통합 관점에서 검증하기 위한 테스트 인프라
    protected RequestPostProcessor authenticatedUser(String role) {
        UserPrincipal principal = new UserPrincipal(
                1L,
                "user",
                "user@test.com",
                "user",
                role,
                "password"
        );

        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

        return authentication(auth);
    }
}
