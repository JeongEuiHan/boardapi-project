package spboard.board.Domain.Dto;

import lombok.Builder;
import lombok.Getter;
import spboard.board.Domain.entity.Comment;

import java.time.LocalDateTime;

@Getter @Builder
public class CommentResponseDto {
    private Long id;
    private String body;
    private LocalDateTime createdAt;
    private String userLoginId;
    private String userNickname;

    public static CommentResponseDto from(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .body(comment.getBody())
                .createdAt(comment.getCreatedAt())
                .userLoginId(comment.getUser().getLoginId())
                .userNickname(comment.getUser() != null ? comment.getUser().getNickname() : null)
                .build();
    }
}
