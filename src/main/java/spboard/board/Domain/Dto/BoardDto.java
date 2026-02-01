package spboard.board.Domain.Dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import spboard.board.Domain.entity.Board;
import spboard.board.Domain.entity.UploadImage;
import spboard.board.Domain.entity.User;

import java.time.LocalDateTime;

@Data
@Builder
public class BoardDto {

    private Long id;
    private String userLoginId;
    private String userNickname;
    private String title;
    private String body;
    private Integer likeCnt;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private MultipartFile newImage;
    private UploadImage uploadImage;

    public static BoardDto of(Board board) {
        User user = board.getUser();

        return BoardDto.builder()
                .id(board.getId())
                .userLoginId(user != null ? user.getLoginId() : "N/A")
                .userNickname(user != null ? user.getNickname() : "익명")
                .title(board.getTitle())
                .body(board.getBody())
                .createdAt(board.getCreatedAt())
                .lastModifiedAt(board.getLastModifiedAt())
                .likeCnt(board.getLikeCnt() != null ? board.getLikeCnt() : 0)
                .uploadImage(board.getUploadImage())
                .build();
    }
}
