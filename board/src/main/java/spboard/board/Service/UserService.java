package spboard.board.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import spboard.board.Domain.Dto.UserAdminDto;
import spboard.board.Domain.entity.Comment;
import spboard.board.Domain.entity.Like;
import spboard.board.Domain.entity.User;
import spboard.board.Domain.enum_class.UserRole;
import spboard.board.Domain.Dto.UserCntDto;
import spboard.board.Domain.Dto.UserDto;
import spboard.board.Repository.BoardRepository;
import spboard.board.Repository.CommentRepository;
import spboard.board.Repository.LikeRepository;
import spboard.board.Repository.UserRepository;
import spboard.board.Domain.Dto.UserJoinRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final BCryptPasswordEncoder encoder;

    public void joinValid(UserJoinRequest request, BindingResult bindingResult)
    {
        if (request.getLoginId().isEmpty()) {
            bindingResult.rejectValue("loginId", "empty", "아이디가 비어있습니다.");
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

    }

    public void join(UserJoinRequest request) {
        userRepository.save(request.toEntity(encoder.encode(request.getPassword())));
    }

    public User myInfo(String loginId) {
        return userRepository.findByLoginId(loginId).get();
    }

    public BindingResult editValid(UserDto dto, BindingResult bindingResult, String loginId) {
        User longinUser = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        String nowPw = dto.getNowPassword();
        String newPw = dto.getNewPassword();
        String newPwChk = dto.getNewPasswordCheck();
        String nickname = dto.getNickname();

        if (nowPw == null || nowPw.isBlank()) {
            bindingResult.rejectValue("nowPassword", "empty", "현재 비밀번호가 비어 있습니다.");
        } else if (!encoder.matches(nowPw, longinUser.getPassword())) {
            bindingResult.rejectValue("nowPassword", "mismatch", "현재 비밀번호가 틀렸습니다.");
        }

        if ((newPw != null && !newPw.isBlank()) || (newPwChk != null && !newPwChk.isBlank())) {
            if (newPw == null) newPw = "";
            if (newPwChk == null) newPwChk = "";
            if (!newPw.equals(newPwChk)) {
                bindingResult.rejectValue("newPasswordCheck", "mismatch", "비밀번호가 일치하지 않습니다.");
            }
        }

        if (nickname == null || nickname.isBlank()) {
            bindingResult.rejectValue("nickname", "empty", "닉네임이 비어있습니다.");
        } else if (nickname.length() > 10) {
            bindingResult.rejectValue("nickname", "length.exceeded", "닉네임이 10자가 넘습니다.");
        } else if (!nickname.equals(longinUser.getNickname()) && userRepository.existsByNickname(nickname)) {
            bindingResult.rejectValue("nickname", "duplicate", "닉네임이 중복됩니다.");
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


    public Page<UserAdminDto> findAllByNickName(String keyword, Pageable pageable) {
        Page<User> page = userRepository.findAllByNicknameContains(keyword, pageable);
        return page.map(u ->{
            long boardCount = boardRepository.countAllByUser_Id(u.getId());
            long commentCount = commentRepository.countAllByUser_Id(u.getId());
            long likeCount = likeRepository.countAllByUser_Id(u.getId());

            return UserAdminDto.of(u, boardCount, commentCount, likeCount);
        });
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
