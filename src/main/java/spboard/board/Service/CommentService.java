package spboard.board.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spboard.board.Domain.entity.Board;
import spboard.board.Domain.entity.Comment;
import spboard.board.Domain.entity.User;
import spboard.board.Domain.enum_class.UserRole;
import spboard.board.Domain.mybati.BoardMapper;
import spboard.board.Domain.mybati.CommentMapper;
import spboard.board.Domain.mybati.UserMapper;
import spboard.board.Domain.Dto.CommentCreateRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final BoardMapper boardMapper;
    private final UserMapper userMapper;

    public void writeComment(Long boardId, CommentCreateRequest request, String loginId) {
        Board board = boardMapper.findById(boardId).orElseThrow(() -> new IllegalArgumentException("게시판 없음"));
        User user = userMapper.findByLoginId(loginId).orElseThrow(() -> new IllegalArgumentException(" 유저 없음"));

        boardMapper.incrementCommentCount(boardId);

        commentMapper.insert(request.toEntity(board, user, LocalDateTime.now(), LocalDateTime.now()));
    }

    public List<Comment> findAll(Long boardId) { return commentMapper.findAllByBoardId(boardId);
    }

    @Transactional
    public Long editComment(Long commentId, String newBody, String loginId) {
        Comment comment = commentMapper.findById(commentId).orElseThrow(() -> new IllegalArgumentException("댓글 없음"));
        User user = userMapper.findByLoginId(loginId).orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        if (!comment.getUser().getId().equals(user.getId())) {
            return  null;
        }

        commentMapper.updateBody(comment.getId(), newBody, LocalDateTime.now());

        return comment.getBoard().getId();
    }

    public Long deleteComment(Long commentId, String loginId) {
        Comment comment = commentMapper.findById(commentId).orElseThrow(() -> new IllegalArgumentException("댓글 없음"));
        User user = userMapper.findByLoginId(loginId).orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        if ((!comment.getUser().getId().equals(user.getId()) && !user.getUserRole().equals(UserRole.ADMIN))) {
            return null;
        }

        Long boardId = comment.getBoard().getId();

        boardMapper.decrementCommentCount(boardId);

        commentMapper.deleteById(commentId);
        return boardId;
    }
}
