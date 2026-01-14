package spboard.board.Domain.Dto;

import lombok.Data;
import spboard.board.Domain.entity.Board;
import spboard.board.Domain.entity.Comment;
import spboard.board.Domain.entity.User;

@Data
public class CommentCreateRequest {

    private String body;

    public Comment toEntity(Board board, User user) {
        return Comment.builder()
                .user(user)
                .board(board)
                .body(body)
                .build();
    }
}
