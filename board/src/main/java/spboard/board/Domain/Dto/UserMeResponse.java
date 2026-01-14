package spboard.board.Domain.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserMeResponse {
    private String loginId;
    private String nickname;
    private String userRole;        // "BRONZE" / "SILVER" / "GOLD" / "BLACKLIST" / "ADMIN"
    private boolean authenticated;  // 프론트에서 쓰기 편함
}