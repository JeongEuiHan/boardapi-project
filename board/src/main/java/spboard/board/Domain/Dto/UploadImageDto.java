package spboard.board.Domain.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import spboard.board.Domain.entity.UploadImage;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UploadImageDto {
    private Long id;
    private String originalFilename;
    private String savedFilename;

    public static UploadImageDto of(UploadImage img) {
        if (img == null) return null;
        return new UploadImageDto(
                img.getId(),
                img.getOriginalFilename(),
                img.getSavedFilename()
        );
    }
}