package spboard.board.Domain.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import spboard.board.Domain.entity.User;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserAdminDto {
    private Long id;
    private String loginId;
    private String nickname;
    private String userRole;
    private LocalDateTime createdAt;
    private int receivedLikeCnt;

    private long boardCount;
    private long commentCount;
    private long likeCount;

    public static UserAdminDto of(User u, long boardCount, long commentCount, long likeCount) {
        return new UserAdminDto(
                u.getId(),
                u.getLoginId(),
                u.getNickname(),
                u.getUserRole().name(),   // userRole이 enum이면
                u.getCreatedAt(),
                u.getReceivedLikeCnt(),
                boardCount,
                commentCount,
                likeCount
        );
    }
}