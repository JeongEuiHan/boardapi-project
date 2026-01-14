package spboard.board.Domain.Dto;

import lombok.Builder;
import lombok.Getter;
import spboard.board.Domain.entity.User;

@Getter
@Builder
public class UserSummaryDto {
    private Long id;
    private String loginId;
    private String nickname;
    private String userRole;
    private Integer receivedLikeCnt;

    public static UserSummaryDto from(User u) {
        return UserSummaryDto.builder()
                .id(u.getId())
                .loginId(u.getLoginId())
                .nickname(u.getNickname())
                .userRole(u.getUserRole().name())   // enum이면
                .receivedLikeCnt(u.getReceivedLikeCnt())
                .build();
    }
}
