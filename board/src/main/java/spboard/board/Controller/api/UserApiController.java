package spboard.board.Controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import spboard.board.Domain.Dto.MeResponse;
import spboard.board.Domain.Dto.MyPageResponseDto;
import spboard.board.Domain.Dto.UserDto;
import spboard.board.Domain.Dto.UserJoinRequest;
import spboard.board.Domain.entity.Board;
import spboard.board.Domain.entity.User;
import spboard.board.Service.BoardService;
import spboard.board.Service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserApiController {

    private final UserService userService;
    private final BoardService boardService;

    /**
     * 회원가입
     */
    @PostMapping("/join")
    public ResponseEntity<?> join(
            @Valid @RequestBody UserJoinRequest request,
            BindingResult bindingResult) {
        // DTO 검증
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(toFieldErrorMap(bindingResult));
        }

        // 비즈니스 검증
        userService.joinValid(request, bindingResult);
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(toFieldErrorMap(bindingResult));
        }

        // 회원가입
        userService.join(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("회원가입에 성공했습니다.");
    }

    /**
     * 로그인 여부 확인 (React용)
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.myInfo(auth.getName());

        return ResponseEntity.ok(
                MeResponse.from(user)
        );
    }

    /**
     * 마이페이지 - 내기 쓴 글
     */
    @GetMapping("/mypage/{category}")
    public ResponseEntity<?> myPage(
            @PathVariable String category,
            Authentication auth ) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.myInfo(auth.getName());
        List<Board> boards = boardService.findMyBoard(category, auth.getName());

        return ResponseEntity.ok(MyPageResponseDto.of(user, boards));
    }

    /**
     * 내 정보 수정
     */
    @PostMapping("/edit")
    public ResponseEntity<?> userEdit(
            @Valid @RequestBody UserDto dto,
            BindingResult bindingResult,
            Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Validation
        userService.editValid(dto, bindingResult, auth.getName());
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(toFieldErrorMap(bindingResult));
        }

        userService.edit(dto, auth.getName());
        return ResponseEntity.ok("회원 정보가 수정되었습니다.");
    }

    /**
     * 회원 탈퇴
     */
    @PostMapping("/delete")
    public ResponseEntity<?> userDelete(
            @RequestBody UserDto dto,
            Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Boolean deleteSuccess = userService.delete(auth.getName(), dto.getNowPassword());

        if(!deleteSuccess) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("현재 비밀번호가 틀립니다.");
        }

        return ResponseEntity.ok("탈퇴가 완료되었습니다.");
    }

    /**
     * 관리자 - 회원 목록
     */
    @GetMapping("/admin")
    public ResponseEntity<?> adminPage(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "") String keyword
            ) {
        PageRequest pageRequest = PageRequest.of(page - 1, 10, Sort.by("id").descending());
        return ResponseEntity.ok(
                userService.findAllByNickName(keyword, pageRequest)
        );
    }

    /**
     * 관리자 - 권한 변경
     */
    @PatchMapping("/admin/{userId}/role")
    public ResponseEntity<?> adminChangeRole(@PathVariable Long userId) {
        userService.changeRole(userId);
        return ResponseEntity.ok("권한이 변경되었습니다.");
    }

    private Map<String, String> toFieldErrorMap(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        bindingResult.getFieldErrors().forEach(e -> {
            // 같은 필드 에러가 여러 개면 첫 번째만 유지
            errors.putIfAbsent(e.getField(), e.getDefaultMessage());
        });
        return errors;
    }
}
