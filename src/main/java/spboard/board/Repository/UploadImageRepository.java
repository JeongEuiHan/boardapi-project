package spboard.board.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spboard.board.Domain.UploadImage;

@Repository
public interface UploadImageRepository extends JpaRepository<UploadImage, Long> {
}
