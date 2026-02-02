package com.moirai.alloc.common.security.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moirai.alloc.common.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class DoubleSubmitCsrfFilter extends OncePerRequestFilter {

    private static final String CSRF_HEADER_NAME = "X-CSRF-Token";

    private final String csrfCookieName;
    private final ObjectMapper objectMapper;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (!requiresCsrf(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String cookieToken = null;
        var cookie = WebUtils.getCookie(request, csrfCookieName);
        if (cookie != null) {
            cookieToken = cookie.getValue();
        }
        String headerToken = request.getHeader(CSRF_HEADER_NAME);

        if (!StringUtils.hasText(cookieToken) || !StringUtils.hasText(headerToken) || !cookieToken.equals(headerToken)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(
                    ApiResponse.failure("CSRF_FORBIDDEN", "Invalid CSRF token")));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean requiresCsrf(HttpServletRequest request) {
        String method = request.getMethod();
        if (HttpMethod.GET.matches(method)
                || HttpMethod.HEAD.matches(method)
                || HttpMethod.OPTIONS.matches(method)
                || HttpMethod.TRACE.matches(method)) {
            return false;
        }

        String path = request.getRequestURI();
        if (HttpMethod.POST.matches(method)
                && PATH_MATCHER.match("/api/auth/login", path)) {
            return false;
        }
        if (HttpMethod.POST.matches(method)
                && PATH_MATCHER.match("/api/auth/password/reset/**", path)) {
            return false;
        }
        if (PATH_MATCHER.match("/api/internal/**", path)) {
            return false;
        }

        return true;
    }
}
