package spboard.board.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;
import spboard.board.Domain.entity.Board;
import spboard.board.Domain.entity.UploadImage;
import spboard.board.Repository.BoardRepository;
import spboard.board.Repository.UploadImageRepository;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadImageService {

    private final UploadImageRepository uploadImageRepository;
    private final BoardRepository boardRepository;

    private final String rootPath = System.getProperty("user.dir");
    private final String fileDir = rootPath + "/board/src/main/resources/static/upload-images/";

    public String getFullPath(String filename) {
        return fileDir + filename;
    }

    public UploadImage saveImage(MultipartFile multipartFile, Board board) throws IOException {
        // ✅ null/empty 방어 (500 방지)
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        // ✅ 디렉토리 생성 (500 방지)
        File dir = new File(fileDir);
        if (!dir.exists()) dir.mkdirs();

        String originalFilename = multipartFile.getOriginalFilename();
        String ext = extractExt(originalFilename);

        if (originalFilename == null || ext.isBlank()) {
            // 확장자 없는 파일은 거부(선택)
            return null;
        }

        String savedFilename = UUID.randomUUID() + "." + ext;

        multipartFile.transferTo(new File(getFullPath(savedFilename)));

        return uploadImageRepository.save(UploadImage.builder()
                .originalFilename(originalFilename)
                .savedFilename(savedFilename)
                .board(board)
                .build());
    }

    @Transactional
    public void deleteImage(UploadImage uploadImage) throws IOException {
        if (uploadImage == null) return;
        uploadImageRepository.delete(uploadImage);
        Files.deleteIfExists(Paths.get(getFullPath(uploadImage.getSavedFilename())));
    }

    private String extractExt(String originalFilename) {
        if (originalFilename == null) return "";
        int pos = originalFilename.lastIndexOf(".");
        if (pos < 0) return "";
        return originalFilename.substring(pos + 1);
    }

    public ResponseEntity<UrlResource> downloadImage(Long boardId) throws MalformedURLException {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글이 없습니다."));

        if (board.getUploadImage() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "이미지가 없습니다.");
        }

        UrlResource urlResource = new UrlResource(
                "file:" + getFullPath(board.getUploadImage().getSavedFilename())
        );

        String encodedName = UriUtils.encode(board.getUploadImage().getOriginalFilename(), StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedName + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(urlResource);
    }
}

