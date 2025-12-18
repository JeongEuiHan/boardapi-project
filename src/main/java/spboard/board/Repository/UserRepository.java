package spboard.board.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spboard.board.Domain.User;
import spboard.board.Domain.UserRole;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String longId);
    Page<User> findAllByNicknameContains(String nickname, PageRequest pageRequest); // 닉네임에 String이 포함되어 있는지 => ADMIN이 User검색 시 사용
    Boolean existsByLoginId(String loginId); // 로그인 아이디를 가진 유저가 존재하는지
    Boolean existsByNickname(String nickname); // 닉네임을 가진 유저가 존재하는지 => 회원 가입 시 중복 체크용으로 사용
    Long countAllByUserRole(UserRole userRole); // 해당 등급을 가진 유저가 몇명 있는지 => 홈화면에서 출력하기 위해 사용
}
