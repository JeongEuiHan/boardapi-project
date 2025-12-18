package spboard.board.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import spboard.board.Domain.Comment;
import spboard.board.Domain.Like;
import spboard.board.Domain.User;
import spboard.board.Domain.UserRole;
import spboard.board.Dto.UserCntDto;
import spboard.board.Dto.UserDto;
import spboard.board.Repository.CommentRepository;
import spboard.board.Repository.LikeRepository;
import spboard.board.Repository.UserRepository;
import spboard.board.Req.UserJoinRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final BCryptPasswordEncoder encoder;

    public BindingResult joinValid(UserJoinRequest request, BindingResult bindingResult)
    {
        if (request.getLoginId().isEmpty()) {
            bindingResult.addError(new FieldError("request", "loginId", "아이디가 비어있습니다."));
        } else if (request.getLoginId().length() > 10) {
            bindingResult.rejectValue(
                    "loginId",
                    "length.exceeded",
                    "아이디가 10자가 넘습니다."
            );
        } else if (userRepository.existsByLoginId(request.getLoginId())) {
            bindingResult.rejectValue(
                    "loginId",
                    "loginId.duplicate",
                    "이미 사용 중인 아이디입니다."
            );
        }

        if (request.getPassword().isEmpty()) {
            bindingResult.rejectValue(
                    "password",
                    "password.empty",
                    "비밀번호가 비어있습니다."
            );
        }

        if (!request.getPassword().equals(request.getPasswordCheck())) {
            bindingResult.rejectValue(
                    "passwordCheck",
                    "password.mismatch",
                    "비밀번호가 일치하지 않습니다."
            );
        }

        if (request.getNickname().isEmpty()) {
            bindingResult.rejectValue(
                    "nickname",
                    "nickname.empty",
                    "닉네임이 비어있습니다."
            );
        } else if (request.getNickname().length() > 10) {
            bindingResult.rejectValue(
                    "nickname",
                    "length.exceeded",
                    "닉네임이 10자가 넘습니다."
            );
        } else if (userRepository.existsByNickname(request.getNickname())) {
            bindingResult.rejectValue(
                    "nickname",
                    "nickname.duplicate",
                    "닉네임이 중복됩니다."
            );
        }

        return bindingResult;
    }

    public void join(UserJoinRequest request) {
        userRepository.save(request.toEntity(encoder.encode(request.getPassword())));
    }

    public User myInfo(String loginId) {
        return userRepository.findByLoginId(loginId).get();
    }

    public BindingResult editValid(UserDto dto, BindingResult bindingResult, String loginId) {
        User longinUser = userRepository.findByLoginId(loginId).get();

        if (dto.getNowPassword().isEmpty()) {
            bindingResult.addError(new FieldError("dto","nowPassword","현재 비밀번호가 비어 있습니다"));
        } else if (!encoder.matches(dto.getNowPassword(), longinUser.getPassword())) {
            bindingResult.addError(new FieldError("dto", "nowPassword", "현재 비밀번호가 틀렸습니다."));
        }

        if (!dto.getNewPassword().equals(dto.getNewPasswordCheck())) {
            bindingResult.addError(new FieldError("dto", "newPasswordCheck", "비밀번호가 일치하지 않습니다."));
        }

        if (dto.getNickname().isEmpty()) {
            bindingResult.addError(new FieldError("dto", "nickname", "닉네임이 비어있습니다."));
        } else if (dto.getNickname().length() > 10) {
            bindingResult.addError(new FieldError("dto", "nickname", "닉네임이 10자가 넘습니다."));
        } else if (!dto.getNickname().equals(longinUser.getNickname()) && userRepository.existsByNickname(dto.getNickname())) {
            bindingResult.addError(new FieldError("dto", "nickname", "닉네임이 중복됩니다."));
        }

        return bindingResult;
    }

    @Transactional
    public void edit(UserDto dto, String loginId) {
        User loginUser = userRepository.findByLoginId(loginId).get();

        if (dto.getNewPassword().equals("")) {
            loginUser.edit(loginUser.getPassword(), dto.getNickname());
        } else {
            loginUser.edit(encoder.encode(dto.getNewPassword()), dto.getNickname());
        }
    }


    @Transactional
    public Boolean delete(String loginId, String nowPassword) {
        User loginuser = userRepository.findByLoginId(loginId).get();

        if(encoder.matches(nowPassword, loginuser.getPassword())) {
            List<Like> likes = likeRepository.findAllByUser_LoginId(loginId);
            for (Like like : likes) {
                like.getBoard().likeChange(like.getBoard().getLikeCnt() - 1);
            }

            List<Comment> comments = commentRepository.findAllByUser_LoginId(loginId);
            for (Comment comment: comments) {
                comment.getBoard().commentChange(comment.getBoard().getCommentCnt() - 1);
            }

            userRepository.delete(loginuser);
            return true;
        } else {
            return false;
        }
    }


    public Page<User> findAllByNickName(String keyword, PageRequest pageRequest) {
        return userRepository.findAllByNicknameContains(keyword, pageRequest);
    }

    @Transactional
    public void changeRole(Long userId) {
        User user = userRepository.findById(userId).get();
        user.changeRole();
    }

    public UserCntDto getUserCnt() {
        return UserCntDto.builder()
                .totalUserCnt(userRepository.count())
                .totalAdminCnt(userRepository.countAllByUserRole(UserRole.ADMIN))
                .totalBronzeCnt(userRepository.countAllByUserRole(UserRole.BRONZE))
                .totalSilverCnt(userRepository.countAllByUserRole(UserRole.SILVER))
                .totalGoldCnt(userRepository.countAllByUserRole(UserRole.GOLD))
                .totalBlacklistCnt(userRepository.countAllByUserRole(UserRole.BLACKLIST))
                .build();
    }

}
