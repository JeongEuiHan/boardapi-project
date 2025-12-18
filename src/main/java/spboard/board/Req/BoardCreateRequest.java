package spboard.board.Req;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import spboard.board.Domain.Board;
import spboard.board.Domain.BoardCategory;
import spboard.board.Domain.User;

@Data
public class BoardCreateRequest {

    private String title;
    private String body;
    private MultipartFile uploadImage;


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
