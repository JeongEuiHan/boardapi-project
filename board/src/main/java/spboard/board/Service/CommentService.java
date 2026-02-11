package spboard.board.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spboard.board.Domain.Dto.CommentResponseDto;
import spboard.board.Domain.entity.Board;
import spboard.board.Domain.entity.Comment;
import spboard.board.Domain.entity.User;
import spboard.board.Domain.enum_class.UserRole;
import spboard.board.Repository.BoardRepository;
import spboard.board.Repository.CommentRepository;
import spboard.board.Repository.UserRepository;
import spboard.board.Domain.Dto.CommentCreateRequest;

import javax.security.auth.login.AccountException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public void writeComment(Long boardId, CommentCreateRequest request, String loginId) {
        Board board = boardRepository.findById(boardId).get();
        User user = userRepository.findByLoginId(loginId).get();
        board.commentChange(board.getCommentCnt() + 1);
        commentRepository.save(request.toEntity(board, user));
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> findAll(Long boardId) {
        return commentRepository.findByBoardIdWithUser(boardId).stream()
                .map(CommentResponseDto::from)
                .toList();
    }

    @Transactional
    public Long editComment(Long commentId, String newBody, String loginId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글 없음"));

        if (!comment.getUser().getLoginId().equals(loginId)) {
            throw new AccessDeniedException("수정 권한 없음");
        }

        comment.update(newBody);

        return comment.getBoard().getId();
    }

    @Transactional
    public Long deleteComment(Long commentId, String loginId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글 없음"));

        if (!comment.getUser().getLoginId().equals(loginId)) {
            throw new AccessDeniedException("삭제 권한 없음");
        }

        Board board = comment.getBoard();

        // 댓글 수 감소 (0 이하 방어)
        if (board.getCommentCnt() != null && board.getCommentCnt() > 0) {
            board.commentChange(board.getCommentCnt() - 1);
        }

        commentRepository.delete(comment);
        return board.getId();
    }
}
