package spboard.board.Domain.Dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import spboard.board.Domain.entity.Board;
import spboard.board.Domain.enum_class.BoardCategory;
import spboard.board.Domain.entity.User;

import java.time.LocalDateTime;

@Data
public class BoardCreateRequest {

    private String title;
    private String body;
    private MultipartFile uploadImage;


    public Board toEntity(BoardCategory category, User user) {
        LocalDateTime now = LocalDateTime.now();

        return Board.builder()
                .user(user)
                .category(category)
                .title(title)
                .body(body)
                .likeCnt(0)
                .commentCnt(0)
                .createdAt(now)
                .lastModifiedAt(now)
                .build();
    }
}
