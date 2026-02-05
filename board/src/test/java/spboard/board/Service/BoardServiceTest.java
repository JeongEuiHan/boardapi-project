package spboard.board.Service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import spboard.board.Domain.Dto.BoardCreateRequest;
import spboard.board.Domain.Dto.BoardDto;
import spboard.board.Domain.Dto.BoardUpdateRequest;
import spboard.board.Domain.entity.Board;
import spboard.board.Domain.entity.UploadImage;
import spboard.board.Domain.entity.User;
import spboard.board.Domain.enum_class.BoardCategory;
import spboard.board.Domain.enum_class.BoardSearchType;
import spboard.board.Domain.enum_class.UserRole;
import spboard.board.Repository.BoardRepository;
import spboard.board.Repository.CommentRepository;
import spboard.board.Repository.LikeRepository;
import spboard.board.Repository.UserRepository;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock private BoardRepository boardRepository;
    @Mock private UserRepository userRepository;
    @Mock private LikeRepository likeRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private UploadImageService uploadImageService;

    @InjectMocks
    private BoardService boardService;

    // 자주 쓰는 값들
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10); // 입력 pageable
    }

    @Test
    @DisplayName("getBoardLists: 검색조건 없으면 category로만 조회 + notice desc, createdAt desc 정렬 적용")
    void getBoardLists_noSearch_returnsFindAllByCategory_withSortedPageable() {
        // given
        BoardCategory category = BoardCategory.FREE;
        Page<Board> expected = new PageImpl<>(List.of(mock(Board.class)));

        when(boardRepository.findAllByCategory(eq(category), any(Pageable.class)))
                .thenReturn(expected);

        // when
        Page<Board> result = boardService.getBoardLists(category, pageable, null, null);

        // then
        assertThat(result).isSameAs(expected);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(boardRepository).findAllByCategory(eq(category), pageableCaptor.capture());

        Pageable used = pageableCaptor.getValue();
        Sort sort = used.getSort();

        // 정렬 조건 확인: notice desc, createdAt desc
        assertThat(sort.getOrderFor("notice")).isNotNull();
        assertThat(sort.getOrderFor("notice").getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(sort.getOrderFor("createdAt")).isNotNull();
        assertThat(sort.getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("getBoardLists: TITLE 검색이면 title contains로 조회")
    void getBoardLists_titleSearch_callsTitleContainsRepo() {
        // given
        BoardCategory category = BoardCategory.FREE;
        String keyword = "hello";
        Page<Board> expected = new PageImpl<>(List.of(mock(Board.class)));

        when(boardRepository.findAllByCategoryAndTitleContains(eq(category), eq(keyword), any(Pageable.class)))
                .thenReturn(expected);

        // when
        Page<Board> result = boardService.getBoardLists(category, pageable, BoardSearchType.TITLE, keyword);

        // then
        assertThat(result).isSameAs(expected);
        verify(boardRepository).findAllByCategoryAndTitleContains(eq(category), eq(keyword), any(Pageable.class));
        verify(boardRepository, never()).findAllByCategoryAndUserNicknameContains(any(), any(), any());
    }

    @Test
    @DisplayName("getBoardLists: NICKNAME 검색이면 userNickname contains로 조회")
    void getBoardLists_nicknameSearch_callsNicknameContainsRepo() {
        // given
        BoardCategory category = BoardCategory.FREE;
        String keyword = "사과";
        Page<Board> expected = new PageImpl<>(List.of(mock(Board.class)));

        when(boardRepository.findAllByCategoryAndUserNicknameContains(eq(category), eq(keyword), any(Pageable.class)))
                .thenReturn(expected);

        // when
        Page<Board> result = boardService.getBoardLists(category, pageable, BoardSearchType.NICKNAME, keyword);

        // then
        assertThat(result).isSameAs(expected);
        verify(boardRepository).findAllByCategoryAndUserNicknameContains(eq(category), eq(keyword), any(Pageable.class));
        verify(boardRepository, never()).findAllByCategoryAndTitleContains(any(), any(), any());
    }

    @Test
    @DisplayName("getBoard: GOLD 게시판 조회는 GOLD/ADMIN만 가능 (SILVER면 AccessDenied)")
    void getBoard_goldPermissionDenied() {
        // given
        Long boardId = 1L;
        BoardCategory category = BoardCategory.GOLD;
        String loginId = "abc";

        User user = mock(User.class);
        when(userRepository.findByLoginId(loginId)).thenReturn(Optional.of(user));
        when(user.getUserRole()).thenReturn(UserRole.SILVER);

        // when & then
        assertThatThrownBy(() -> boardService.getBoard(boardId, category, loginId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("GOLD 게시판");
        verify(boardRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("getBoard: boardId가 없거나 category 불일치면 null 반환")
    void getBoard_notFoundOrCategoryMismatch_returnsNull() {
        // given
        Long boardId = 1L;
        BoardCategory category = BoardCategory.FREE;
        String loginId = "abc";

        // FREE니까 validateGoldReadPermission은 통과
        Board board = mock(Board.class);
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(board.getCategory()).thenReturn(BoardCategory.GOLD); // 카테고리 불일치

        // when
        BoardDto dto = boardService.getBoard(boardId, category, loginId);

        // then
        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("writeBoard: ADMIN이면 notice 표시, GREETING이면 rankUp 호출, 이미지 저장되면 setUploadImage 호출")
    void writeBoard_adminNotice_greetingRankUp_imageSaved() throws IOException {
        // given
        BoardCategory category = BoardCategory.GREETING;
        String loginId = "admin";
        Authentication auth = mock(Authentication.class);
        MultipartFile uploadImage = mock(MultipartFile.class);

        User loginUser = mock(User.class);
        when(userRepository.findByLoginId(loginId)).thenReturn(Optional.of(loginUser));
        when(loginUser.getUserRole()).thenReturn(UserRole.ADMIN);

        BoardCreateRequest request = mock(BoardCreateRequest.class);
        Board board = mock(Board.class);
        Board savedBoard = mock(Board.class);
        when(request.toEntity(eq(category), eq(loginUser))).thenReturn(board);
        when(boardRepository.save(board)).thenReturn(savedBoard);
        when(savedBoard.getId()).thenReturn(10L);

        UploadImage savedImage = mock(UploadImage.class);
        when(uploadImageService.saveImage(uploadImage, savedBoard)).thenReturn(savedImage);

        // when
        Long id = boardService.writeBoard(request, category, loginId, auth, uploadImage);

        // then
        assertThat(id).isEqualTo(10L);
        verify(board).markNotice(); // ADMIN이면 공지
        verify(savedBoard).setUploadImage(savedImage); // 이미지 연동
        verify(loginUser).rankUp(UserRole.SILVER, auth); // GREETING이면 승급 로직 호출
    }

    @Test
    @DisplayName("editBoard: 작성자 아니면 SecurityException 발생")
    void editBoard_notOwner_throwsSecurityException() throws IOException {
        // given
        Long boardId = 1L;
        String category = "free";
        String loginId = "otherUser";

        BoardUpdateRequest request = mock(BoardUpdateRequest.class);
        MultipartFile newImage = mock(MultipartFile.class);

        Board board = mock(Board.class);
        User owner = mock(User.class);

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(board.getCategory()).thenReturn(BoardCategory.FREE);
        when(board.getUser()).thenReturn(owner);
        when(owner.getLoginId()).thenReturn("realOwner");

        // 작성자와 로그인 사용자가 다를 때 예외 발생 검증
        assertThatThrownBy(() -> boardService.editBoard(boardId, category, request, loginId, newImage))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("수정 권한");

        // 보안 사고 방지: 수정 로직이 호출되지 않았음을 보장
        verify(board, never()).update(any());
        verify(uploadImageService, never()).saveImage(any(), any());
    }

    @Test
    @DisplayName("editBoard: 새 이미지가 들어오면 기존 이미지 삭제 후 새 이미지 저장")
    void editBoard_replaceImage_deletesOldAndSetsNew() throws IOException {
        // given
        Long boardId = 1L;
        String category = "free";
        String loginId = "owner";

        BoardUpdateRequest request = mock(BoardUpdateRequest.class);
        MultipartFile newImage = mock(MultipartFile.class);

        Board board = mock(Board.class);
        User owner = mock(User.class);
        UploadImage oldImage = mock(UploadImage.class);
        UploadImage savedImage = mock(UploadImage.class);

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(board.getCategory()).thenReturn(BoardCategory.FREE);

        when(board.getUser()).thenReturn(owner);
        when(owner.getLoginId()).thenReturn(loginId);

        when(board.getId()).thenReturn(boardId);

        when(newImage.isEmpty()).thenReturn(false);
        when(board.getUploadImage()).thenReturn(oldImage);
        when(uploadImageService.saveImage(newImage, board)).thenReturn(savedImage);

        // when
        Long id = boardService.editBoard(boardId, category, request, loginId, newImage);

        // then
        assertThat(id).isEqualTo(boardId);

        verify(uploadImageService).deleteImage(oldImage);
        verify(board).setUploadImage(null);
        verify(board).setUploadImage(savedImage);
        verify(board).update(request);
    }
<<<<<<< HEAD
    
=======

>>>>>>> 73f0b7e (커밋)
}
