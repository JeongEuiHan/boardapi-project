package spboard.board.Domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // 공통 속성을 상속받게 하고, 테이블은 만들지 않음
@EntityListeners(AuditingEntityListener.class) // 생성/수정 날짜 및 사용자 정보를 자동 관리
public class BaseEntity {

    @CreatedDate // 엔티티가 처음 저장될 때 생성 시각 자동 기록
    @Column(updatable = false) // UPDATE SQL에서 제외, INSERT 시만 값 저장
    private LocalDateTime createdAt;

    @LastModifiedDate // 엔티티 수정 시 자동 갱신
    private LocalDateTime lastModifiedAt;
}
