package com.polyglot.sms.sender.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polyglot.sms.sender.dto.AuthResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/v1/sms/send");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "FAIL", "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String role = claims.get("role", String.class);

            if (!"ADMIN".equals(role)) {
                log.warn("Access Denied: Role is " + role);
                writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "FAIL", "Access Denied: Admin only");
                return;
            }

            chain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT Verification Failed: {}", e.getMessage());
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "FAIL", "Invalid or expired token");
        }
    }

    private void writeJsonError(HttpServletResponse response, int status, String statusText, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        AuthResponse authResponse = new AuthResponse(statusText, message, null);
        
        String json = objectMapper.writeValueAsString(authResponse);
        response.getWriter().write(json);
    }
}