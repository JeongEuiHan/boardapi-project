package spboard.board.Controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import spboard.board.Service.BoardService;
import spboard.board.Service.LikeService;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeApiController {

    private final LikeService likeService;

    @PostMapping("/{boardId}")
    public ResponseEntity<?> addLike(
            @PathVariable Long boardId,
            Authentication auth
    ) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        likeService.addLike(auth.getName(), boardId);
        return ResponseEntity.ok(
                Map.of(
                        "message", "좋아요가 추가되었습니다",
                        "boardId", boardId
                )
        );
    }

    /**
     * 좋아요 취소
     */
    @DeleteMapping("/{boardId}")
    public ResponseEntity<?> deleteLike(
            @PathVariable Long boardId,
            Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        likeService.deleteLike(auth.getName(), boardId);

        return ResponseEntity.ok(
                Map.of(
                        "message", "좋아요가 취소되었습니다.",
                        "boardId", boardId
                )
        );
    }
}
