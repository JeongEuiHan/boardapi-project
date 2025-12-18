package spboard.board.Req;

import lombok.Data;

@Data
public class UserLoginRequest {

    private String loginId;
    private String password;
}
