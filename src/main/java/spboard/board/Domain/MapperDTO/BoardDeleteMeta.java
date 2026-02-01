package spboard.board.Domain.MapperDTO;

import spboard.board.Domain.enum_class.BoardCategory;

public record BoardDeleteMeta(
        Long boardId,
        BoardCategory category,
        Long userId,
        Integer likeCnt,
        Long uploadImageId
) {
}
