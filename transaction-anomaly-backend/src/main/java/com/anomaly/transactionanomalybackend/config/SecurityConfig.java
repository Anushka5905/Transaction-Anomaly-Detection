package com.anomaly.transactionanomalybackend.config;

import com.anomaly.transactionanomalybackend.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

                // -------------------------------------------------
                // CSRF
                // -------------------------------------------------
                // Disabled because this application uses
                // stateless JWT authentication instead of
                // browser sessions/cookies.
                .csrf(csrf -> csrf.disable())

                // -------------------------------------------------
                // CORS
                // -------------------------------------------------
                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                // -------------------------------------------------
                // STATELESS SESSION
                // -------------------------------------------------
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // -------------------------------------------------
                // AUTHORIZATION RULES
                // -------------------------------------------------
                .authorizeHttpRequests(auth -> auth

                        // =========================================
                        // PUBLIC ENDPOINTS
                        // =========================================

                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        // =========================================
                        // DASHBOARD
                        // =========================================

                        .requestMatchers(
                                "/api/dashboard/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "ANALYST"
                        )

                        // =========================================
                        // TRANSACTIONS - READ
                        // =========================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/transactions/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "ANALYST"
                        )

                        // =========================================
                        // TRANSACTIONS - CREATE
                        // =========================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/transactions/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "ANALYST"
                        )

                        // =========================================
                        // TRANSACTIONS - DELETE
                        // =========================================

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/transactions/**"
                        ).hasRole("ADMIN")

                        // =========================================
                        // ANOMALIES
                        // =========================================

                        .requestMatchers(
                                "/api/anomalies/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "ANALYST"
                        )

                        // =========================================
                        // ANALYTICS
                        // =========================================

                        .requestMatchers(
                                "/api/analytics/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "ANALYST"
                        )

                        // =========================================
                        // ALL OTHER ENDPOINTS
                        // =========================================

                        .anyRequest().authenticated()
                )

                // -------------------------------------------------
                // ERROR HANDLING
                // -------------------------------------------------

                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        authenticationEntryPoint()
                                )
                                .accessDeniedHandler(
                                        accessDeniedHandler()
                                )
                )

                // -------------------------------------------------
                // JWT FILTER
                // -------------------------------------------------

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // =========================================================
    // CORS CONFIGURATION
    // =========================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        /*
         * Only the React frontend running on port 5173
         * is allowed to make browser requests.
         */
        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:5173"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type"
                )
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    // =========================================================
    // 401 UNAUTHORIZED
    // =========================================================

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {

        return (request, response, authException) -> {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.setContentType(
                    "application/json"
            );

            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(
                    """
                    {
                        "status": 401,
                        "error": "Unauthorized",
                        "message": "Valid JWT token required."
                    }
                    """
            );
        };
    }

    // =========================================================
    // 403 FORBIDDEN
    // =========================================================

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {

        return (request, response, accessDeniedException) -> {

            response.setStatus(
                    HttpServletResponse.SC_FORBIDDEN
            );

            response.setContentType(
                    "application/json"
            );

            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(
                    """
                    {
                        "status": 403,
                        "error": "Forbidden",
                        "message": "You do not have permission to access this resource."
                    }
                    """
            );
        };
    }
}