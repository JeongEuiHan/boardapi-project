package spboard.board.Controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spboard.board.Service.UserService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserApiController {
    private final UserService userService;

    @GetMapping("/check-id")
    public ResponseEntity<Map<String, Boolean>> checkId(@RequestParam String loginId) {
        boolean isDuplicate = userService.checkLoginIdDuplicate(loginId);

        return ResponseEntity.ok(Map.of("isAvailable", !isDuplicate));
    }
}
