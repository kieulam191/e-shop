package com.dev.e_shop.auth.handler;

import com.dev.e_shop.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        String message = getMessage(request);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        String errJson = objectMapper.writeValueAsString(new ErrorResponse(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized",
                message,
                request.getRequestURI()
        ));
        response.getWriter().write(errJson);
    }

    private String getMessage(HttpServletRequest request){
        Object jwtException = request.getAttribute("jwt_exception");

        if (jwtException instanceof ExpiredJwtException) {
            return "JWT token has expired";
        } else if (jwtException instanceof SignatureException) {
            return "Invalid JWT signature";
        } else if (jwtException instanceof MalformedJwtException) {
            return "Malformed JWT token";
        } else {
            return "Unauthorized access";
        }
    }
}
