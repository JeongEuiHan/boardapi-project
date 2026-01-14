package spboard.board.Controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spboard.board.Service.BoardService;
import spboard.board.Service.UserService;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HomeApiController {

    private final UserService userService;
    private final BoardService boardService;

    /**
     * 홈 데이터 조회 (회원 수, 게시글 수)
     */
    @GetMapping("/home")
    public ResponseEntity<?> home() {

        return ResponseEntity.ok(
                Map.of(
                        "userCnt", userService.getUserCnt(),
                        "boardCnt", boardService.getBoardCnt()
                )
        );
    }
}
