package spboard.board.Controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.expression.AccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spboard.board.Domain.Dto.*;
import spboard.board.Domain.entity.Board;
import spboard.board.Domain.enum_class.BoardCategory;
import spboard.board.Domain.enum_class.BoardSearchType;
import spboard.board.Service.BoardService;
import spboard.board.Service.CommentService;
import spboard.board.Service.LikeService;
import spboard.board.Service.UploadImageService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardApiController {

    private final BoardService boardService;
    private final LikeService likeService;
    private final CommentService commentService;
    private final UploadImageService uploadImageService;

    /**
     * 게시글 목록 조회 (REST)
     * GET /api/boards?category=free&page=0&size=10&sort=createdAt,desc
     */
    @GetMapping
    public ResponseEntity<?> getBoards(
            @RequestParam String category,
            @PageableDefault(size = 10)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC) // 기본 최신순
            }) Pageable pageable,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword
    ) {
        BoardCategory boardCategory = BoardCategory.of(category);
        if (boardCategory == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("code", "INVALID_CATEGORY", "message", "카테고리가 존재하지 않습니다.")
            );
        }

        BoardSearchType type = null;
        if (searchType != null) {
            type = switch (searchType.toLowerCase()) {
                case "title" -> BoardSearchType.TITLE;
                case "nickname" -> BoardSearchType.NICKNAME;
                default -> null; // 또는 여기서 400으로 막아도 됨
            };
        }

        Page<Board> boards = boardService.getBoardLists(boardCategory, pageable, type, keyword);
        Page<BoardDto> dtoPage = boards.map(BoardDto::of);

        return ResponseEntity.ok(dtoPage);
    }

    /**
     * 게시글 작성 (REST)
     * POST /api/boards?category=free
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createBoard(
            @RequestParam String category,
            @RequestPart("request") BoardCreateRequest request,
            @RequestPart(value = "uploadImage", required = false) MultipartFile uploadImage,
            Authentication auth
    ) throws IOException {

        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("code", "UNAUTHORIZED", "message", "로그인이 필요합니다."));
        }

        BoardCategory boardCategory = BoardCategory.of(category);
        if (boardCategory == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("code", "INVALID_CATEGORY", "message", "카테고리가 존재하지 않습니다.")
            );
        }

        try {
            Long id = boardService.writeBoard(request, boardCategory, auth.getName(), auth, uploadImage);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("code", "FORBIDDEN", "message", e.getMessage())
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("code", "BAD_REQUEST", "message", e.getMessage())
            );
        }
    }

    /**
     * 게시글 상세 조회 (REST)
     * GET /api/boards/{boardId}?category=free  (카테고리 검증이 필요하면 query로 받는 게 깔끔)
     */
    @GetMapping("/{boardId}")
    public ResponseEntity<?> getBoardDetail(
            @PathVariable Long boardId,
            @RequestParam String category,
            Authentication auth
    ) {
        BoardCategory c = BoardCategory.of(category);

        String loginId = (auth != null ? auth.getName() : null);

        if ( c == BoardCategory.GOLD && auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("code","UNAUTHORIZED", "message", "GOLD 게시판은 로그인 필요"));
        }

        BoardDto boardDto = boardService.getBoard(boardId, c, loginId);
        if (boardDto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("code", "NOT_FOUND", "message", "게시글을 찾을 수 없습니다.")
            );
        }

        boolean likeCheck = (auth != null) && likeService.existsLike(auth.getName(), boardId);

        return ResponseEntity.ok(
                Map.of(
                        "board", boardDto,
                        "comments", commentService.findAll(boardId),
                        "likeCheck", likeCheck
                )
        );
    }

    /**
     * 게시글 수정 (REST)
     * PUT /api/boards/{boardId}?category=free
     */
    @PutMapping(value = "/{boardId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateBoard(
            @PathVariable Long boardId,
            @RequestParam String category,
            @RequestPart("request") BoardUpdateRequest request,
            @RequestPart(value = "uploadImage", required = false) MultipartFile uploadImage,
            Authentication auth
    ) throws IOException {

        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("code", "UNAUTHORIZED", "message", "로그인이 필요합니다."));
        }

        Long editBoardId = boardService.editBoard(boardId, category, request, auth.getName(), uploadImage);

        if (editBoardId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("code", "NOT_FOUND", "message", "게시글을 찾을 수 없습니다.")
            );
        }
        return ResponseEntity.ok(Map.of("id", editBoardId));
    }

    /**
     * 게시글 삭제 (REST)
     * DELETE /api/boards/{boardId}?category=free
     */
    @DeleteMapping("/{boardId}")
    public ResponseEntity<?> deleteBoard(
            @PathVariable Long boardId,
            @RequestParam String category
    ) throws IOException {
        if ("greeting".equals(category)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("code", "FORBIDDEN_CATEGORY", "message", "가입인사는 삭제할 수 없습니다.")
            );
        }

        Long deletedBoardId = boardService.deleteBoard(boardId, category);

        if (deletedBoardId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("code", "NOT_FOUND", "message", "게시글을 찾을 수 없습니다.")
            );
        }
        // REST 관점에서 삭제 성공은 204 No Content도 많이 씀.
        return ResponseEntity.ok(Map.of("id", deletedBoardId));
    }

    // 이미지 API는 그대로 둬도 OK (다만 /api/boards/images 보다 /api/images가 더 리소스스럽긴 함)
    @GetMapping("/images/{filename}")
    public Resource showImage(@PathVariable String filename) throws MalformedURLException {
        return new UrlResource("file:" + uploadImageService.getFullPath(filename));
    }

    @GetMapping("/images/download/{boardId}")
    public ResponseEntity<UrlResource> downloadImage(@PathVariable Long boardId) throws MalformedURLException {
        return uploadImageService.downloadImage(boardId);
    }
}

