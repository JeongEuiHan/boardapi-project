package spboard.board.Req;

import lombok.Data;
import spboard.board.Domain.Board;
import spboard.board.Domain.Comment;
import spboard.board.Domain.User;

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
