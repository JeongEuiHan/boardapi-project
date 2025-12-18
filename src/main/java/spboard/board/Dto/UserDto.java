package spboard.board.Dto;

import lombok.Builder;
import lombok.Data;
import spboard.board.Domain.User;

@Data
@Builder
// 정보 수정, 탈퇴 등에 사용되는 DTO
public class UserDto {

    private String loginId;
    private String nickname;
    private String nowPassword;
    private String newPassword;
    private String newPasswordCheck;

    // User 엔티티를 UserDto로 변환
    public static UserDto of(User user) {
        return UserDto.builder()
                .loginId(user.getLoginId())
                .nickname(user.getNickname())
                .build();
    }
}
