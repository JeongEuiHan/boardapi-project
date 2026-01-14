package spboard.board.Domain.Dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import spboard.board.Domain.entity.Board;
import spboard.board.Domain.entity.UploadImage;

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
    private boolean notice;
    private MultipartFile newImage;
    private UploadImageDto uploadImage;


    public static BoardDto of(Board board) {
        return BoardDto.builder()
                .id(board.getId())
                .userLoginId(board.getUser().getLoginId())
                .userNickname(board.getUser().getNickname())
                .title(board.getTitle())
                .body(board.getBody())
                .createdAt(board.getCreatedAt())
                .lastModifiedAt(board.getLastModifiedAt())
                .likeCnt(board.getLikes().size())
                .notice(board.isNotice())
                .uploadImage(UploadImageDto.of(board.getUploadImage()))
                .build();
    }
}
