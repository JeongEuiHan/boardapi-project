package spboard.board.Domain.entity;

import lombok.*;
import spboard.board.Domain.*;
import spboard.board.Domain.Dto.BoardDto;
import spboard.board.Domain.enum_class.BoardCategory;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Board extends BaseEntity {

    private Long id;
    private String title; // 제목
    private String body; // 본문
    private BoardCategory category; // 카테고리

    private User user; // 작성자

    private List<Like> likes; // 좋아요
    private Integer likeCnt; // 좋아요 수

    private List<Comment> comments; // 댓글
    private Integer commentCnt; // 댓글 수

    private UploadImage uploadImage;

    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;

    public void update(BoardDto dto) {
        this.title = dto.getTitle();
        this.body = dto.getBody();
    }

    public void likeChange(Integer likeCnt) {
        this.likeCnt = likeCnt;
    }

    public void commentChange(Integer commentCnt){
        this.commentCnt = commentCnt;
    }

    public void setUploadImage(UploadImage uploadImage) {
        this.uploadImage = uploadImage;
    }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void setLastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }

}
