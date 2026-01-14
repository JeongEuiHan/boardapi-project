package spboard.board.config.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import spboard.board.Domain.entity.User;
import spboard.board.Repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;
    @Override

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLoginId(username)
                .orElseThrow(() -> {
            return new UsernameNotFoundException("해당 유저를 찾을 수 없습니다.");
        });

        //Security의 세션에 유저 정보가 저장됨
        return new UserDetail(user);
    }
}
