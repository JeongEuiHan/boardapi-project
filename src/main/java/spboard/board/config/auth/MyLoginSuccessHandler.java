package spboard.board.config.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import spboard.board.Domain.User;
import spboard.board.Domain.UserRole;
import spboard.board.Repository.UserRepository;

import java.io.IOException;
import java.io.PrintWriter;

@AllArgsConstructor
public class MyLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 세션 유지 시간 = 3600초
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(3600);

        User loginUser = userRepository.findByLoginId(authentication.getName()).orElseThrow();

        // 성공 시 메시지 출력 후 홈 화면으로 redirect
        String prevPage = (String) session.getAttribute("prevPage");

        String redirectUrl = (prevPage != null) ? prevPage : "/";
        if (loginUser.getUserRole() == UserRole.BLACKLIST) {
            response.sendRedirect(redirectUrl + "?blacklist=true");
        } else {
            response.sendRedirect(redirectUrl + "?loginSuccess=true");
        }

    }
}
