package spboard.board.Service;

import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import spboard.board.Domain.Dto.BoardCntDto;
import spboard.board.Domain.Dto.BoardDto;
import spboard.board.Domain.Dto.BoardUpdateRequest;
import spboard.board.Domain.entity.*;
import spboard.board.Domain.enum_class.BoardCategory;
import spboard.board.Domain.enum_class.BoardSearchType;
import spboard.board.Domain.enum_class.UserRole;
import spboard.board.Repository.BoardRepository;
import spboard.board.Repository.CommentRepository;
import spboard.board.Repository.LikeRepository;
import spboard.board.Repository.UserRepository;
import spboard.board.Domain.Dto.BoardCreateRequest;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final UploadImageService uploadImageService;


    public Page<Board> getBoardLists(
            BoardCategory category,
            Pageable pageable,
            BoardSearchType searchType,
            String keyword
    ) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(
                        Sort.Order.desc("notice"),
                        Sort.Order.desc("createdAt")
                )
        );

        // 검색어 없음
        if (searchType == null || keyword == null || keyword.isBlank()) {
            return boardRepository.findByCategoryWithUser(category, sortedPageable);
        }

        // 제목 검색
        if (searchType == BoardSearchType.TITLE) {
            return boardRepository.findByCategoryAndTitleContainsWithUser(
                    category, keyword, sortedPageable
            );
        }

        // 작성자 검색
        return boardRepository.findByCategoryAndUserNickNameContainsWithUser(
                category, keyword, sortedPageable
        );
    }

    public BoardDto getBoard(Long boardId, BoardCategory category, String loginId) {

        validateGoldReadPermission(category, loginId);

        Optional<Board> optBoard = boardRepository.findByIdWithUser(boardId);

        // id에 해당하는 게시글이 없거나 카테고리가 일치하지 않으면 null return 대문자 소문자 무시
        if (optBoard.isEmpty() || optBoard.get().getCategory() != category){
            return null;
        }

        return BoardDto.of(optBoard.get());
    }

    @Transactional
    public Long writeBoard(BoardCreateRequest request, BoardCategory category, String loginId, Authentication auth, MultipartFile uploadImage) throws IOException {
        User loginUser = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        validateWritePermission(category, loginUser);

        Board board  = request.toEntity(category, loginUser);

        if (loginUser.getUserRole() == UserRole.ADMIN) {
            board.markNotice();
        }

        Board savedBoard = boardRepository.save(board);

        UploadImage savedImage = uploadImageService.saveImage(uploadImage, savedBoard);
        if (savedImage != null) {
            savedBoard.setUploadImage(savedImage);
        }

        if (BoardCategory.GREETING.equals(category)) {
            loginUser.rankUp(UserRole.SILVER, auth);
        }

        return savedBoard.getId();
    }

    @Transactional
    public Long editBoard(Long boardId, String category, BoardUpdateRequest request, String loginId, MultipartFile newImage) throws IOException {
        Optional<Board> optBoard = boardRepository.findById(boardId);

        // id에 해당하는 게시글이 없거나 카테고리가 일치하지 않으면 null return
        if (optBoard.isEmpty() || !optBoard.get().getCategory().toString().equalsIgnoreCase(category)) {
            return null;
        }

        Board board = optBoard.get();

        // 2) (권장) 작성자 본인만 수정 가능
        if (board.getUser() == null || !board.getUser().getLoginId().equals(loginId)) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        // 3) 이미지 교체 로직
        // 새 이미지가 "들어온 경우"에만 기존 이미지 삭제 + 새 이미지 저장
        if (newImage != null && !newImage.isEmpty()){
            if (board.getUploadImage() != null) {
                uploadImageService.deleteImage(board.getUploadImage());
                board.setUploadImage(null);
            }

            UploadImage uploadImage = uploadImageService.saveImage(newImage, board) ;
            if (uploadImage != null) {
                board.setUploadImage(uploadImage);
            }
        }

        board.update(request);

        return board.getId();
    }

    @Transactional
    public Long deleteBoard(Long boardId, String category)  {
        Optional<Board> optBoard = boardRepository.findById(boardId);

        // id에 해당하는 게시글이 없거나 카테고리가 일치하지 않으면 null retrun
        if (optBoard.isEmpty() || !optBoard.get().getCategory().toString().equalsIgnoreCase(category)) {
            return null;
        }

        Board board = optBoard.get();
        User boardUser = optBoard.get().getUser();
        boardUser.likeChange(boardUser.getReceivedLikeCnt() - optBoard.get().getLikeCnt());
        if (board.getUploadImage() != null) {
            board.setUploadImage(null);
        }
        boardRepository.deleteById(boardId);
        return boardId;
    }

    public String getCategory(Long boardId) {
        Board board = boardRepository.findById(boardId).get();
        return board.getCategory().toString().toLowerCase();
    }

    public List<Board> findMyBoard(String category, String loginId) {
        if (category.equals("board")) {
            return boardRepository.findAllByUser_LoginId(loginId);
        } else if (category.equals("like")) {
            List<Like> likes= likeRepository.findAllByUser_LoginId(loginId);
            List<Board> boards = new ArrayList<>();
            for (Like like : likes) {
                boards.add(like.getBoard());
            }
            return boards;
        } else if (category.equals("comment")) {
            List <Comment> comments = commentRepository.findAllByUser_LoginId(loginId);
            List<Board> boards = new ArrayList<>();
            HashSet<Long> commentIds = new HashSet<>();

            for (Comment comment : comments) {
                if(!commentIds.contains(comment.getBoard().getId())) {
                    boards.add(comment.getBoard());
                    commentIds.add(comment.getBoard().getId());
                }
            }

            return boards;
        }
        return Collections.emptyList();
    }

    public BoardCntDto getBoardCnt(){
        return BoardCntDto.builder()
                .totalBoardCnt(boardRepository.count())
                .totalNoticeCnt(boardRepository.countAllByUser_UserRole(UserRole.ADMIN))
                .totalGreetingCnt(boardRepository.countAllByCategoryAndUser_UserRoleNot(BoardCategory.GREETING, UserRole.ADMIN))
                .totalFreeCnt(boardRepository.countAllByCategoryAndUser_UserRoleNot(BoardCategory.FREE, UserRole.ADMIN))
                .totalGoldCnt(boardRepository.countAllByCategoryAndUser_UserRoleNot(BoardCategory.GOLD, UserRole.ADMIN))
                .build();
    }

    private void validateGoldReadPermission(BoardCategory category, String loginId) {
        if (category != BoardCategory.GOLD) return;

        // 여기까지 왔는데 loginId가 null이면 컨트롤러에서 이미 401 처리했거나,
        // 전역 EntryPoint에서 401로 처리되게 할 수 있음
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        if (user.getUserRole() != UserRole.GOLD && user.getUserRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("GOLD 게시판은 GOLD 이상만 조회 가능합니다.");
        }
    }

    private void validateWritePermission(BoardCategory category, User loginUser) {
        UserRole role = loginUser.getUserRole();

        switch (category) {
            case GREETING:
                if (role != UserRole.BRONZE && role != UserRole.ADMIN) {
                    throw new AccessDeniedException("가입인사 게시판은 BRONZE만 작성 가능합니다.");
                }
                break;

            case FREE:
                if (role != UserRole.SILVER && role != UserRole.GOLD && role != UserRole.ADMIN) {
                    throw new AccessDeniedException("자유게시판은 SILVER 이상만 작성 가능합니다.");
                }
                break;

            case GOLD:
                if (role != UserRole.GOLD && role != UserRole.ADMIN) {
                    throw new AccessDeniedException("골드게시판은 GOLD 이상만 작성 가능합니다.");
                }
                break;

            default:
                // 다른 카테고리 있으면 정책 정해서 추가
                break;
        }
    }
}
