package spboard.board.Domain.Dto;

import lombok.Data;
import spboard.board.Domain.entity.User;
import spboard.board.Domain.enum_class.UserRole;

import java.time.LocalDateTime;

@Data
public class UserJoinRequest {

    private String loginId;
    private String password;
    private String passwordCheck;
    private String nickname;

    public User toEntity(String encodedPassword) {
        return User.builder()
                .loginId(loginId)
                .password(encodedPassword)
                .nickname(nickname)
                .userRole(UserRole.BRONZE)
                .createdAt(LocalDateTime.now())
                .receivedLikeCnt(0)
                .build();
    }
}
