package spboard.board.Domain.Dto;

import lombok.Data;

@Data
public class UserLoginRequest {

    private String loginId;
    private String password;
}
