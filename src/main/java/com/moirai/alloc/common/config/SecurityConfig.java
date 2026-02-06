package com.moirai.alloc.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moirai.alloc.auth.cookie.AuthCookieProperties;
import com.moirai.alloc.security.AllocUserDetailsService;
import com.moirai.alloc.security.GatewayHeaderAuthenticationFilter;
import com.moirai.alloc.common.security.csrf.DoubleSubmitCsrfFilter;
import com.moirai.alloc.internal.auth.security.InternalTokenAuthenticationFilter;
import com.moirai.alloc.common.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AllocUserDetailsService userDetailsService;
    private final AuthCookieProperties authCookieProperties;
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // ========================================================
                // 기본 설정
                // ========================================================
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"message\":\"Unauthorized\"}");
                }))

                // ========================================================
                // 인가 설정
                // ========================================================
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()


                        .requestMatchers("/", "/error", "/favicon.ico").permitAll()

                        // 인증(로그인) 관련: 필요한 것만 permitAll
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()

                        // 비밀번호 재설정 관련 API는 모두 허용
                        .requestMatchers(HttpMethod.POST, "/api/auth/password/reset/**").permitAll()

                        // 내부 연동 API
                        .requestMatchers("/api/internal/**").hasAuthority("INTERNAL")

                        // 관리자 전용 API
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                );

        // ========================================================
        // JWT 필터
        // ========================================================
        http.addFilterBefore(
                new DoubleSubmitCsrfFilter(authCookieProperties.getCsrfTokenName(), objectMapper),
                UsernamePasswordAuthenticationFilter.class
        );

        http.addFilterBefore(
                new InternalTokenAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
        );

        http.addFilterBefore(
                new GatewayHeaderAuthenticationFilter(userDetailsService),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    // ======================================================================
    // Password Encoder
    // ======================================================================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ======================================================================
    // CORS 설정
    // ======================================================================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:5713",
                "https://d1rpulb7zlphpl.cloudfront.net"
        ));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
