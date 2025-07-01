package org.mi.plannitybe.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.mi.plannitybe.exception.ExpiredTokenException;
import org.mi.plannitybe.exception.InvalidTokenException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String message = (authException instanceof InvalidTokenException || authException instanceof ExpiredTokenException) ?
                authException.getMessage() : "로그인이 필요한 요청입니다.";

        response.getWriter().write(
                new ObjectMapper().writeValueAsString(Map.of(
                        "code", "UNAUTHORIZED",
                        "message", message
                ))
        );
    }
}
