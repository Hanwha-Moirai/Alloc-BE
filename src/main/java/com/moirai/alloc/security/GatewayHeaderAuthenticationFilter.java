package com.moirai.alloc.security;

import com.moirai.alloc.common.security.auth.UserDetailsByIdService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class GatewayHeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final UserDetailsByIdService userDetailsByIdService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }
        String userIdHeader = request.getHeader(USER_ID_HEADER);

        if (StringUtils.hasText(userIdHeader)) {
            try {
                Long userId = Long.valueOf(userIdHeader);
                UserDetails userDetails = userDetailsByIdService.loadUserById(userId);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (NumberFormatException ex) {
                log.warn("[AUTH] invalid {} header value: {}", USER_ID_HEADER, userIdHeader);
            } catch (UsernameNotFoundException ex) {
                log.warn("[AUTH] user not found for {}: {}", USER_ID_HEADER, userIdHeader);
            } catch (RuntimeException ex) {
                log.warn("[AUTH] failed to resolve {}: {}", USER_ID_HEADER, ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
