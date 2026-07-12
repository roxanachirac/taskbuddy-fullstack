package com.roxana.taskbuddy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // Setează tipul de răspuns ca fiind JSON și statusul 401 (Neautorizat)
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Mesajul de eroare custom care va fi citit de frontend
        String jsonResponse = "{\"error\": \"Utilizatorul nu a fost găsit sau parola este incorectă.\"}";

        response.getWriter().write(jsonResponse);
    }
}