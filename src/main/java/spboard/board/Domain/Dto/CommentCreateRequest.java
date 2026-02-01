package spboard.board.Domain.Dto;

import lombok.Data;
import spboard.board.Domain.entity.Board;
import spboard.board.Domain.entity.Comment;
import spboard.board.Domain.entity.User;

import java.time.LocalDateTime;

    @Data
    public class CommentCreateRequest {

        private String body;

        public Comment toEntity(Board board, User user, LocalDateTime createdAt, LocalDateTime lastModifiedAt) {
            return Comment.builder()
                    .user(user)
                    .board(board)
                    .body(body)
                    .createdAt(createdAt)
                    .lastModifiedAt(lastModifiedAt)
                    .build();
        }
    }
