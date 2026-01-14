package spboard.board.config.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = Map.of(
                "code", "FORBIDDEN",
                "message", ex.getMessage() != null ? ex.getMessage() : "권한이 없습니다.",
                "path", request.getRequestURI()
        );

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}