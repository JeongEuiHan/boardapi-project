package spboard.board.Domain.entity;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Like {

    private Long id;


    private User user; // 좋아요를 누른 유저
    private Board board; // 좋아요가 추가된 게시글

}
