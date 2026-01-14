package spboard.board.Domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Table(
        name= "\"like\"",
        uniqueConstraints = { @UniqueConstraint(columnNames = {"user_id", "board_id"})
        }
) // JPA에서 엔티티가 매핑될 테이블 이름을 "like" 로 설정하는 것입니다.
public class Like {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user; // 좋아요를 누른 유저

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "board_id", nullable = false)
    private Board board; // 좋아요가 추가된 게시글

}
