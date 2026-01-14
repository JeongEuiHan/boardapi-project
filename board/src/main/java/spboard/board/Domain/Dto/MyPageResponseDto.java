package spboard.board.Domain.Dto;

import lombok.Builder;
import lombok.Getter;
import spboard.board.Domain.entity.Board;
import spboard.board.Domain.entity.User;

import java.util.List;

@Getter
@Builder
public class MyPageResponseDto {
    private UserSummaryDto user;
    private List<BoardSummaryDto> boards;

    public static MyPageResponseDto of(User user, List<Board> boards) {
        return MyPageResponseDto.builder()
                .user(UserSummaryDto.from(user))
                .boards(boards.stream().map(BoardSummaryDto::from).toList())
                .build();
    }
}
