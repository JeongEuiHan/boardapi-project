package spboard.board.Domain.Dto;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import spboard.board.Domain.entity.User;
import spboard.board.Domain.enum_class.UserRole;
import spboard.board.Repository.UserRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AdminInitializer {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    @PostConstruct
    public void initAdmin() {
        if (userRepository.existsByLoginId("admin")) return;

        User admin = User.builder()
                .loginId("admin")
                .password(encoder.encode("admin1234"))
                .nickname("관리자")
                .userRole(UserRole.ADMIN)
                .receivedLikeCnt(0)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(admin);
    }
}
