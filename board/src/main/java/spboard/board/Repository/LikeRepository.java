package spboard.board.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spboard.board.Domain.entity.Like;

import java.util.List;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
        void deleteByUserLoginIdAndBoardId(String loginId, Long boardId);
        Boolean existsByUser_LoginIdAndBoardId(String loginId, Long boardId);
        List<Like> findAllByUser_LoginId(String loginId);
        Long countAllByUser_Id(Long userId);
}
