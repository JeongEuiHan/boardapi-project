package spboard.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import spboard.board.Domain.entity.Board;

@Getter
@AllArgsConstructor
public class BoardResponseDto {

    private Long id;
    private String title;
    private String writer;
    private int likeCount;

    public static BoardResponseDto from(Board board) {
        return new BoardResponseDto(
                board.getId(),
                board.getTitle(),
                board.getUser().getNickname(),
                board.getLikes().size()
        );
    }
}
