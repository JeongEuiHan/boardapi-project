package spboard.board.Domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import spboard.board.Dto.BoardDto;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Board extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) // PK를 자동으로 생성해서 관리하라
    private Long id;

    private String title; // 제목
    private String body; // 본문

    @Enumerated(EnumType.STRING) // DB에 FREE, GREETING, GOLD 저장, 조회 -> BoardCategory.FREE
    private BoardCategory category; // 카테고리

    @ManyToOne(fetch = FetchType.LAZY) // 실제로 사용할 때만 DB 조회 (지연 로딩)
    private User user; // 작성자

    @OneToMany(mappedBy = "board", orphanRemoval = true)
    private List<Like> likes; // 좋아요
    private Integer likeCnt; // 좋아요 수

    @OneToMany(mappedBy = "board", orphanRemoval = true)
    private List<Comment> comments; // 댓글
    private Integer commentCnt; // 댓글 수

    @OneToOne(fetch = FetchType.LAZY)
    private UploadImage uploadImage;

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

}
