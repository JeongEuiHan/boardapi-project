package spboard.board.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spboard.board.Domain.Board;
import spboard.board.Domain.BoardCategory;
import spboard.board.Domain.UserRole;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    Page<Board> findAllByCategoryAndUser_UserRoleNot(BoardCategory category, UserRole userRole, PageRequest pageRequest); // 해당 카테고리에 있는 게시글을 페이지에 맞게 조회, 이 때 ADMIN이 작성한 글(공지) 포함 X
    Page<Board> findAllByCategoryAndTitleContainsAndUser_UserRoleNot(BoardCategory category, String title, UserRole userRole, PageRequest pageRequest); // 해당 카테고리에 있는 공지 글 조회
    Page<Board> findAllByCategoryAndUserNicknameContainsAndUser_UserRoleNot(BoardCategory category, String nickname, UserRole userRole, PageRequest pageRequest); // 검색 기능에 사용
    List<Board> findAllByUser_LoginId(String loginId);
    List<Board> findAllByCategoryAndUser_UserRole(BoardCategory category, UserRole userRole);
    Long countAllByUser_UserRole(UserRole userRole); // 전체 공지글이 몇개 있는지 조회시 사용
    Long countAllByCategoryAndUser_UserRoleNot(BoardCategory category, UserRole userRole); // 해당 카테고리에 공지글을 제외한 글이 몇개 있는지 조회 시 사용
}
