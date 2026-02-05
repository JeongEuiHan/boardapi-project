package spboard.board.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spboard.board.Domain.entity.Board;
import spboard.board.Domain.entity.Like;
import spboard.board.Domain.entity.User;
import spboard.board.Repository.BoardRepository;
import spboard.board.Repository.LikeRepository;
import spboard.board.Repository.UserRepository;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    @Transactional
    public void addLike(String loginId, Long boardId) {
        Board board = boardRepository.findByIdWithLock(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        User loginUser = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        User boardUser = board.getUser();

        if (likeRepository.existsByUser_LoginIdAndBoardId(loginId, boardId)) {
            throw new IllegalStateException("이미 처리된 좋아요입니다.");
        }

        // 자신이 누른 좋아요가 아니라면
        if (!boardUser.equals(loginUser)) {
            boardUser.likeChange(boardUser.getReceivedLikeCnt() + 1);
        }
        board.likeChange(board.getLikeCnt() + 1);

        likeRepository.save(Like.builder()
                .user(loginUser)
                .board(board)
                .build());
    }

    @Transactional
    public void deleteLike(String loginId, Long boardId) {
        Board board = boardRepository.findByIdWithLock(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        User loginUser = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        User boardUser = board.getUser();

        if (!likeRepository.existsByUser_LoginIdAndBoardId(loginId, boardId)) {
            throw new IllegalStateException("이미 없어진 좋아요입니다.");
        }

        // 자신이 누른 좋아요가 아니라면
        if(!boardUser.equals(loginUser)) {
            boardUser.likeChange(boardUser.getReceivedLikeCnt() - 1);
        }
        board.likeChange(board.getLikeCnt() - 1);

        likeRepository.deleteByUserLoginIdAndBoardId(loginId, boardId);
    }

    @Transactional(readOnly = true)
    public Boolean existsLike(String loginId, Long boardId) {
        return likeRepository.existsByUser_LoginIdAndBoardId(loginId, boardId);
    }
}
