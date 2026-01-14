package spboard.board.Domain.Dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import spboard.board.Domain.entity.Board;
import spboard.board.Domain.enum_class.BoardCategory;
import spboard.board.Domain.entity.User;

@Data
public class BoardCreateRequest {

    private String title;
    private String body;


    public Board toEntity(BoardCategory category, User user) {
        return Board.builder()
                .user(user)
                .category(category)
                .title(title)
                .body(body)
                .likeCnt(0)
                .commentCnt(0)
                .build();
    }
}
