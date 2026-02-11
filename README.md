# Spring Boot REST API 게시판 프로젝트

> **배포 주소:** [http://54.180.108.56:8084](http://54.180.108.56:8084)  
> **테스트 계정:** `aaa` / `123` (또는 회원가입 가능)

Spring Boot 기반 REST API 게시판 프로젝트입니다.
클라이언트 요청을 신뢰하지 않고,
서버가 요청을 끝까지 책임지는 구조를 목표로 설계·구현했습니다.

> **기술 스택별 브랜치 안내**
> - **[MyBatis & MySQL 버전 (Refactored)](https://github.com/JeongEuiHan/boardapi-project/tree/mybatis-version)**
> - SQL 중심 설계 및 MySQL 버전
> - XML 기반 동적 쿼리 및 ResultMap 활용
본 프로젝트는
JPA 기반으로 비즈니스 규칙과 도메인 구조를 먼저 안정화한 뒤,
동일한 기능을 MyBatis 기반으로 리팩토링하여
데이터 접근 기술 변화에도 Service 계층의 핵심 정책이 유지되도록 설계했습니다.

---

## 이 프로젝트를 만든 이유

웹 서비스를 이용하며 클라이언트 사이드(UI)의 제어만으로는 보안 취약점이 존재할 수 있음을 체감했습니다. 
특히 URL 직접 호출이나 API 조작을 통해 프론트엔드의 비즈니스 로직이 우회되는 사례를 보며, 
서버 측 검증의 중요성과 데이터 무결성을 깊이 있게 학습하고자 이 프로젝트를 시작하게 되었습니다.

---

## 프로젝트 목표

- REST API 기반 게시판 시스템 설계
- JWT 기반 인증/인가 구조 구현
- Service 계층 중심의 권한 검증
- 로직 + DB 제약을 통한 데이터 무결성 보장
- JPA → MyBatis 전환 경험

---

## 기술 스택

### Backend
- Java 17
- Spring Boot
- Spring Security
- MyBatis (XML 기반 SQL / ResultMap / 동적 쿼리)
- Spring Data JPA
- JWT (Authentication / Authorization)

### Frontend
- React
- Axios
- Vite

### Database
- MySQL

### Tool
- Postman
- Gradle
- Git / GitHub
---

## 기술적 의사결정

### Spring Boot
Spring Boot는 인증, 보안, 트랜잭션 같은 백엔드 필수 기능을
프레임워크 차원에서 일관되게 관리할 수 있어 선택했습니다.

Controller는 요청/응답 전달만 담당하고,
권한 검증과 비즈니스 규칙은 Service 계층에서 처리하도록 분리해
요청 검증 로직이 한 곳에 모이도록 설계했습니다.

---


### Spring Data JPA
초기 구조 설계 단계에서는 JPA를 활용해
게시글, 사용자, 좋아요 간의 연관관계를 도메인 중심으로 정리했습니다.

이를 통해
비즈니스 규칙을 명확히 정의하고
이후 MyBatis로 전환하더라도 동일한 정책을 유지할 수 있는 기반을 마련했습니다.

### MySQL
MySQL을 사용해 애플리케이션 로직뿐만 아니라
DB 제약 조건으로도 데이터 무결성을 보장하도록 설계했습니다.

- 좋아요 기능에 (user_id, board_id) 유니크 제약 적용
- 로직 검증 + DB 제약의 이중 안전장치 구조

### MyBatis (Refactored)
- COUNT + LIST 쿼리 분리로 페이징 처리
- 동적 검색 조건은 `<if> / <choose>`로 구현
- 정렬 컬럼은 서버 화이트리스트 방식으로 검증

[List 쿼리예시]
```xml
    <select id="findPageByCategoryExcludeRole" resultMap="BoardResultMap">
        SELECT
            b.id, b.title, b.body, b.category, b.like_cnt, b.comment_cnt, b.created_at, b.last_modified_at,
            u.id AS user_id,
            u.login_id AS login_id,
            u.nickname AS nickname
        FROM todo.`board` b
        JOIN todo.`user` u ON u.id = b.user_id
        WHERE b.category = #{category}
            AND u.user_role != #{excludeRole}
            AND u.status = 'ACTIVE'
        <if test="searchType != null and keyword != null and keyword != ''">
            <choose>
                <when test="searchType == 'title'">
                    AND b.title LIKE CONCAT('%', #{keyword}, '%')
                </when>
                <when test="searchType == 'nickname'">
                    AND u.nickname LIKE CONCAT('%', #{keyword}, '%')
                </when>
            </choose>
        </if>

        ORDER BY ${orderBy} DESC

        LIMIT #{limit} OFFSET #{offset}
    </select>
```
---

## Key Features

### 회원 / 인증
- 회원 가입 / 로그인
- JWT 기반 인증 처리
- Role 기반 접근 제어

### 게시판
- 게시글 CRUD
- 카테고리별 게시판 분리
- 작성자만 수정/삭제 가능

### 좋아요
- 사용자당 게시글 1회만 가능
- Service 검증 + DB 유니크 제약으로 중복 방지

---

## 프로젝트 구성

```text
boardapi/
├─ board/        # Spring Boot REST API 서버
└─ frontend/     # React 클라이언트

```

---

##  아키텍처

<p align="center">
  <img src="./docs/screenshots/architecture.svg" width="800" alt="System Architecture">
</p>

- Controller는 요청/응답만 담당
- Service: 권한 검증 및 비즈니스 규칙 집중
- Repository/Mapper: 데이터 접근 담당
- 정책이 한 곳에 모여 유지보수성과 안정성 확보

---

## 🗂 ERD

<p align="center">
  <img src="./docs/screenshots/erderd4.svg" width="800" alt="ERD Diagram">
</p>

- (user_id, board_id) 유니크 제약으로 좋아요 중복 방지

---

## Troubleshooting (핵심 문제 해결)

### JPA N+1 문제 해결

**문제 상황**
댓글 조회 후 DTO 변환 과정에서 연관 엔티티 접근으로 인해 N+1 문제가 발생했습니다.

**Entity 구조**
```java
@Entity
public class Comment {

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

}
```
**N+1 DTO 변환**
```java
public static CommentResponseDto from(Comment comment) {
    return CommentResponseDto.builder()
            .id(comment.getId())
            .body(comment.getBody())
            .createdAt(comment.getCreatedAt())
            .userLoginId(comment.getUser().getLoginId())
            .userNickname(comment.getUser() != null ? comment.getUser().getNickname() : null)
            .build();
    }
```

**기존 Repository**
```java
List<Comment> findAllByBoardId(Long boardId);
```

---

## 실행 로그 (Before)
```sql
select * from comment where board_id = 54;

select * from user where id = 3;
select * from user where id = 7;
select * from user where id = 2;
select * from user where id = 5;
```
댓글 수만큼 user 조회 발생 → 1 + N 쿼리

---

### 해결 방법 (Fetch Join 적용)
## Repository 수정
```java
@Query("""
    select c from Comment c
    join fetch c.user
    where c.board.id = :boardId
    order by c.createdAt desc
""")
List<Comment> findAllByBoardIdWithUser(@Param("boardId") Long boardId);
```

---

## 실행 로그 (After)
```
select c.*, u.*
from comment c
join user u on u.id = c.user_id
where c.board_id = 54
order by c.created_at desc;
```
- 댓글 + 작성자 단일 쿼리 조회
- 추가 SELECT 없음
- N+1 문제 해결 완료

---

## 개선 결과

| 항목 | 개선 전 | 개선 후 |
|------|---------|---------|
| 댓글 조회 쿼리 수 | 1 + N | 1 |
| 성능 특성 | 댓글 수에 비례 | 일정 유지 |
| DB 부하 | 증가 | 감소 |

---
## 한 줄 요약
LAZY 연관관계 접근으로 발생한 N+1 문제를 Fetch Join으로 해결하고, Hibernate SQL 로그로 검증했습니다.

### 2. 배포 환경(MariaDB)에서의 SQL 문법 호환성 문제

**문제 상황**
- 로컬(H2/MySQL)에서는 정상 작동하던 '좋아요' 기능이 배포 환경(AWS EC2 + MariaDB)에서 `500 Internal Server Error`를 발생시킴.
- 로그 확인 결과, JPA에서 생성한 `FOR UPDATE OF` 구문을 MariaDB가 인식하지 못해 `SQLSyntaxErrorException` 발생.

**해결**
- `application.yml`에 `database-platform`을 `org.hibernate.dialect.MariaDBDialect`로 설정하여 Dialect를 변경함.

---

##  테스트
- JUnit5 + Mockito 기반 Service 계층 단위 테스트
- DB/외부 의존성을 Mock 처리해 권한 검증과 비즈니스 로직 분기에 집중
**검증 내용**
- 게시판 목록 조회 시 카테고리·검색 조건별 Repository 호출 및 정렬 정책 검증
- GOLD 게시판 접근 권한 검증 (SILVER 접근 시 AccessDeniedException)
- 작성자 불일치 시 게시글 수정 제한
- 게시글 수정 시 이미지 교체 로직 검증 (기존 이미지 삭제 → 새 이미지 저장)

Service 정책 검증을 목적으로 한 테스트이며,
쿼리/매핑 검증은 @DataJpaTest로 확장 가능하도록 분리했습니다.

---

###  프로젝트를 통해 느낀 점
- 서버는 클라이언트 요청을 신뢰해서는 안 된다
- 권한 검증은 Controller가 아닌 **Service 책임**
- 데이터 무결성은 **로직 + DB 제약**으로 함께 보장해야 한다
- “기능이 된다”와 “서비스로 안전하다”는 전혀 다르다


---

## 실행 방법
1. Backend 실행
   - MySQL 설정
   - application.yml 환경변수 설정
   - `./gradlew bootRun`

2. Frontend 실행
   - `npm install`
   - `npm run dev`
  
---

### Links
- GitHub Repository: https://github.com/JeongEuiHan/boardapi-project
