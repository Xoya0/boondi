package com.boondi.infrastructure.config;

import com.boondi.infrastructure.exception.ApiResponse;
import com.boondi.infrastructure.exception.ErrorCode;
import com.boondi.infrastructure.security.CustomUserDetailsService;
import com.boondi.infrastructure.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, AuthenticationEntryPoint authenticationEntryPoint) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Without this, Spring Security's fallback entry point for a stateless config
                // with no httpBasic()/formLogin() is Http403ForbiddenEntryPoint — meaning every
                // missing/expired/invalid JWT gets a 403, not 401. That silently breaks the
                // client-side refresh flow, since OkHttp's Authenticator (and equivalents) only
                // ever fires on 401. Anonymous/unauthenticated access must be 401; 403 is
                // reserved for a valid, authenticated request that lacks the required role
                // (e.g. @PreAuthorize("hasRole('ADMIN')") — handled separately, see
                // GlobalExceptionHandler#handleAccessDeniedException).
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/reset-password").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/auth/verify-email").permitAll()
                        .requestMatchers(HttpMethod.GET, "/posts/{postId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/posts/{postId}/replies").permitAll()
                        .requestMatchers(HttpMethod.GET, "/timelines/latest").permitAll()
                        .requestMatchers(HttpMethod.GET, "/timelines/trending").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/{username}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/{username}/posts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/{username}/followers").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/{username}/following").permitAll()
                        .requestMatchers(HttpMethod.GET, "/search/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/search/posts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/search/hashtags").permitAll()
                        .requestMatchers(HttpMethod.GET, "/hashtags/trending").permitAll()
                        .requestMatchers(HttpMethod.GET, "/health").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api-docs/**", "/api-docs").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** Writes a proper 401 + ApiResponse JSON body for any request that reaches an
     *  authenticated endpoint without valid credentials (missing, malformed, or expired JWT). */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ApiResponse<Void> body = ApiResponse.failure(
                    ErrorCode.TOKEN_INVALID.name(),
                    "Authentication is required to access this resource",
                    request.getRequestURI()
            );
            response.getWriter().write(objectMapper.writeValueAsString(body));
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
