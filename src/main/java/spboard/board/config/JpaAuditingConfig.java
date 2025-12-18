package spboard.board.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing // 엔티티 생성/수정 시간을 자동으로 기록하도록 JPA를 켜주는 스위치
public class JpaAuditingConfig {
}
