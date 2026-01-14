package spboard.board.Domain.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import spboard.board.Domain.entity.User;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MeResponse {
    private Long id;
    private String loginId;
    private String nickname;
    private String userRole;
    private LocalDateTime createdAt;
    private int receivedLikeCnt;

    public static MeResponse from(User u) {
        return new MeResponse(
                u.getId(),
                u.getLoginId(),
                u.getNickname(),
                u.getUserRole().name(),
                u.getCreatedAt(),
                u.getReceivedLikeCnt()
        );
    }
}