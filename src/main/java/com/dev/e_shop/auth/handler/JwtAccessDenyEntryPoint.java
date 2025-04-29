package com.dev.e_shop.auth.handler;

import com.dev.e_shop.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class JwtAccessDenyEntryPoint implements AccessDeniedHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        String errJson = objectMapper.writeValueAsString(new ErrorResponse(
                HttpServletResponse.SC_FORBIDDEN,
                "Forbidden",
                "Access Denied. You don't have permission to access this resource",
                request.getRequestURI()
        ));
        response.getWriter().write(errJson);
    }
}
