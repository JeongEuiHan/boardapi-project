package spboard.board.Domain.entity;

import lombok.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import spboard.board.Domain.enum_class.UserRole;
import spboard.board.Domain.enum_class.UserStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class User {

    private Long id;
    private String loginId; // 로그인할 때 사용하는 아이디
    private String password; // 비밀번호
    private String nickname; // 닉네임
    private LocalDateTime createdAt; // 가입 시간
    private Integer receivedLikeCnt; // 유저가 받은 좋아요 개수

    private UserRole userRole; // 권한
    private UserStatus status; // 상태

    private int boardCnt;
    private int commentCnt;
    private int likeCnt;

    private List<Board> boards;
    private List<Like> likes;
    private List<Comment> comments;

    public void rankUp(UserRole userRole, Authentication auth) {
        this.userRole = userRole;

        // 현재 저장되어 있는 Authentication 수정 => 재로그인 하지 않아도 권한 수정 되기 위함
        List<GrantedAuthority> updatedAuthorities = new ArrayList<>();
        updatedAuthorities.add(new SimpleGrantedAuthority(userRole.name()));
        Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), updatedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    public void likeChange(Integer receivedLikeCnt) {
        this.receivedLikeCnt = receivedLikeCnt;
        if (this.receivedLikeCnt >= 10 && this.userRole.equals(UserRole.SILVER)) {
            this.userRole = UserRole.GOLD;
        }
    }

    public void edit(String newPassword, String newNickname) {
        this.password = newPassword;
        this.nickname = newNickname;
    }

    public UserRole changeRole() {
        if (userRole.equals(UserRole.BRONZE)) return userRole = UserRole.SILVER;
        else if (userRole.equals(UserRole.SILVER)) return userRole = UserRole.GOLD;
        else if (userRole.equals(UserRole.GOLD)) return userRole = UserRole.BLACKLIST;
        return userRole = UserRole.BRONZE;
    }


}
