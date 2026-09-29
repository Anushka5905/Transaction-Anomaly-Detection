package com.anomaly.transactionanomalybackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // =====================================================
        // NO AUTHORIZATION HEADER
        // =====================================================

        if (authHeader == null || authHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // =====================================================
        // INVALID AUTHORIZATION FORMAT
        // =====================================================

        if (!authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // =====================================================
        // EXTRACT TOKEN
        // =====================================================

        String token = authHeader.substring(7).trim();

        if (token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            // =================================================
            // VALIDATE TOKEN
            // =================================================

            if (!jwtService.isTokenValid(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            // =================================================
            // EXTRACT USER INFORMATION
            // =================================================

            String email = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);

            // =================================================
            // VALIDATE CLAIMS
            // =================================================

            if (email == null
                    || email.isBlank()
                    || role == null
                    || role.isBlank()) {

                filterChain.doFilter(request, response);
                return;
            }

            role = role.trim().toUpperCase();

            // Only application roles are accepted.
            if (!role.equals("ADMIN")
                    && !role.equals("ANALYST")) {

                filterChain.doFilter(request, response);
                return;
            }

            // =================================================
            // CREATE SPRING SECURITY AUTHENTICATION
            // =================================================

            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority(
                            "ROLE_" + role
                    );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(authority)
                    );

            // =================================================
            // SET SECURITY CONTEXT
            // =================================================

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

        } catch (Exception exception) {

            /*
             * Never allow a malformed/expired JWT to crash
             * the request pipeline.
             *
             * The request continues without authentication,
             * allowing Spring Security to return 401/403
             * according to the endpoint's security rules.
             */
            SecurityContextHolder
                    .clearContext();
        }

        // =====================================================
        // CONTINUE REQUEST
        // =====================================================

        filterChain.doFilter(request, response);
    }
}