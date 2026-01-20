package spboard.board.Controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import spboard.board.Domain.entity.User;
import spboard.board.Domain.enum_class.UserRole;
import spboard.board.Repository.UserRepository;
import spboard.board.config.jwt.JwtTokenProvider;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.loginId(), req.password())
        );

        User user = userRepository.findByLoginId(req.loginId()).orElseThrow();

        // BLACKLIST면 막기
        if (user.getUserRole() == UserRole.BLACKLIST) {
            return ResponseEntity.status(403).body(new LoginResponse(false, null, "BLACKLIST"));
        }

        String accessToken = jwtTokenProvider.generateAccessToken(auth);
        return ResponseEntity.ok(new LoginResponse(true, accessToken, null));
    }

    public record LoginRequest(String loginId, String password) {}
    public record LoginResponse(boolean ok, String accessToken, String reason) {}
}
