package spboard.board.Controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import spboard.board.Domain.Dto.CommentCreateRequest;
import spboard.board.Service.BoardService;
import spboard.board.Service.CommentService;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentApiController {

    private final CommentService commentService;
    private final BoardService boardService;

    /**
     * 댓글 작성
     */
    @PostMapping("/{boardId}")
    private ResponseEntity<?> addComments(
            @PathVariable Long boardId,
            @RequestBody CommentCreateRequest request,
            Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        commentService.writeComment(boardId, request, auth.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "댓글이 추가되었습니다.",
                        "boardId", boardId
                ));
    }

    /**
     * 댓글 수정
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<?> editComment(
            @PathVariable Long commentId,
            @RequestBody CommentCreateRequest request,
            Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long boardId = commentService.editComment(commentId, request.getBody(), auth.getName());

        if (boardId == null) {
            return ResponseEntity.badRequest().body("잘못된 요청입니다.");
        }

        return ResponseEntity.ok(
                Map.of(
                        "message", "댓글이 수정되었습니다.",
                        "boardId", boardId
                )
        );
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long boardId = commentService.deleteComment(commentId, auth.getName());

        if (boardId == null) {
            return ResponseEntity.badRequest().body("잘못된 요청입니다.");
        }
        return ResponseEntity.ok(
                Map.of(
                        "message", "댓글이 삭제되었습니다.",
                        "boardId", boardId
                )
        );
    }
}
