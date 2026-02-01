package spboard.board.Domain.entity;
import lombok.*;
import spboard.board.Domain.BaseEntity;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Comment extends BaseEntity {

    private Long id;

    private String body;

    private User user; // 작성자

    private Board board; // 댓글이 달린 게시판

    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;

    public void update(String newBody) {
        this.body = newBody;
        this.lastModifiedAt = LocalDateTime.now();
    }
}
