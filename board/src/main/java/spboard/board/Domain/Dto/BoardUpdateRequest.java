package spboard.board.Domain.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardUpdateRequest {
    private String title;
    private String body;
}
