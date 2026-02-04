package com.moirai.alloc.common.security.jwt;

import com.moirai.alloc.common.security.auth.UserDetailsByIdService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsByIdService userDetailsByIdService;
    private final String accessTokenCookieName;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        String token = resolveToken(req);

        if (token != null && jwtTokenProvider.validate(token)) {

            Claims claims = jwtTokenProvider.getClaims(token);

            // Refresh 토큰이면 인증용으로 사용 금지
            String typ = claims.get("typ", String.class);
            if ("refresh".equals(typ)) {
                chain.doFilter(req, res);
                return;
            }

            if ("internal".equals(typ)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                "internal",
                                null,
                                List.of(new SimpleGrantedAuthority("INTERNAL"))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                chain.doFilter(req, res);
                return;
            }

            Long userId = Long.parseLong(claims.getSubject());
            UserDetails userDetails = userDetailsByIdService.loadUserById(userId);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(req, res);
    }

    private boolean hasInternalScope(Object scope) {
        if (scope instanceof List<?> scopes) {
            return scopes.contains("INTERNAL");
        }
        if (scope instanceof String scopeString) {
            return scopeString.contains("INTERNAL");
        }
        return false;
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        if (StringUtils.hasText(accessTokenCookieName)) {
            var cookie = WebUtils.getCookie(request, accessTokenCookieName);
            if (cookie != null && StringUtils.hasText(cookie.getValue())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
