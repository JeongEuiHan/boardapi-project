package spboard.board.config.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");

        String body = String.format(
                "{\"code\":\"UNAUTHORIZED\",\"message\":\"로그인이 필요합니다.\",\"path\":\"%s\"}",
                request.getRequestURI()
        );

        response.getWriter().write(body);
    }
}
