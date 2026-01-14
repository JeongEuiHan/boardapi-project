package spboard.board.Domain.Dto;

import lombok.*;
import spboard.board.Domain.entity.User;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private String loginId;
    private String nickname;
    private String nowPassword;
    private String newPassword;
    private String newPasswordCheck;

    public static UserDto of(User user) {
        return UserDto.builder()
                .loginId(user.getLoginId())
                .nickname(user.getNickname())
                .build();
    }
}