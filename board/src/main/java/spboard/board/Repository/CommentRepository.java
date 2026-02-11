package spboard.board.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spboard.board.Domain.entity.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByBoardId(Long boardId);
    List<Comment> findAllByUser_LoginId(String loginId);
    Long countAllByUser_Id(Long userId);

    @Query("""
    select c from Comment c
    join fetch c.user
    where c.board.id = :boardId
    order by c.createdAt desc
    """)
    List<Comment> findByBoardIdWithUser(@Param("boardId") Long boardId);
}
