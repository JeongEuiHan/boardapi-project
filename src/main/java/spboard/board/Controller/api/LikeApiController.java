package spboard.board.Controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spboard.board.Service.BoardService;
import spboard.board.Service.LikeService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/likes")
public class LikeApiController {
    private final LikeService likeService;
    private final BoardService boardService;

    @PostMapping("/{boardId}")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long boardId, Authentication auth) {
        Map<String, Object> result = new HashMap<>();

        if (auth == null) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(result);
        }

        // 1. 좋아요 상태 변경 (있으면 삭제, 없으면 추가)
        // likeService.toggleLike 로직은 본인의 기존 로직을 활용하세요.
        String loginId = auth.getName();
        boolean isLiked = likeService.toggleLike(loginId, boardId);

        // 2. 해당 게시글의 최신 좋아요 개수 가져오기
        int likeCnt = boardService.getLikeCount(boardId);

        result.put("success", true);
        result.put("isLiked", isLiked); // 현재 좋아요 상태 (채워진 하트용)
        result.put("likeCnt", likeCnt); // 바뀐 좋아요 숫자

        return ResponseEntity.ok(result);
    }

}
