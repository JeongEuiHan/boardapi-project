package spboard.board.Domain.entity;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class UploadImage {

    private Long id;

    private String originalFilename; // 원본 파일명
    private String savedFilename; // 서버에 저장된 파일명

}
