package spboard.board.Domain.Dto;

import lombok.Builder;
import lombok.Getter;
import spboard.board.Domain.entity.Board;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardSummaryDto {
    private Long id;
    private String category;
    private String title;
    private Integer likeCnt;
    private Integer commentCnt;
    private LocalDateTime createdAt;
    private UserSummaryDto user;

    public static BoardSummaryDto from(Board b) {
        return BoardSummaryDto.builder()
                .id(b.getId())
                .category(b.getCategory().name()) // enum이면
                .title(b.getTitle())
                .likeCnt(b.getLikeCnt())
                .commentCnt(b.getCommentCnt())
                .createdAt(b.getCreatedAt())
                .user(b.getUser() != null ? UserSummaryDto.from(b.getUser()) : null)
                .build();
    }
}
