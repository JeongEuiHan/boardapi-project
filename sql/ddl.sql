-- 유저 테이블
CREATE TABLE todo.user (
	id BIGINT NOT NULL AUTO_INCREMENT,
    login_id VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    received_like INT NOT NULL DEFAULT 0,
    user_role VARCHAR(30) NOT NULL,
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_login_id (login_id),
    UNIQUE KEY uk_user_nickname (nickname),
    KEY idx_user_role (user_role),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 게시판 테이블
CREATE TABLE todo.board (
  id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  body VARCHAR(2000) NOT NULL,
  category VARCHAR(30) NOT NULL,
  user_id BIGINT NOT NULL,
  like_cnt INT NOT NULL DEFAULT 0,
  comment_cnt INT NOT NULL DEFAULT 0,
  upload_image_id BIGINT NULL,

  created_at DATETIME(6) NOT NULL,
  last_modified_at DATETIME(6) NOT NULL,

  PRIMARY KEY (id),
  KEY idx_board_user_id (user_id),
  KEY idx_board_category_id (category, id),
  CONSTRAINT fk_board_user
    FOREIGN KEY (user_id) REFERENCES todo.`user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 업로드 이미지 테이블
CREATE TABLE todo.upload_image (
	id BIGINT NOT NULL auto_increment,
    original_filename VARCHAR(255) NOT NULL,
    saved_filename VARCHAR(255) NOT NULL,
    
    primary key (id),
    unique key uk_upload_image_saved_filename (saved_filename)
) engine=InnoDB default charset=utf8mb4;

-- 댓글 테이블
create table todo.comment (
	id BIGINT NOT NULL AUTO_INCREMENT,
    body varchar(1000) NOT NULL,
    user_id bigint not null,
    board_id bigint not null,
    created_at datetime(6) not null,
    last_modified_at datetime(6) not null,
    
    primary key (id),
    KEY idx_comment_board_id (board_id),
    KEY idx_comment_user_id (user_id),
    
    constraint fk_comment_user
		foreign key (user_id) references todo.`user`(id),
	constraint fk_comment_board
		foreign key (board_id) references todo.board(id)
)engine=InnoDB default charset=utf8mb4;


create table todo.`like` (
	id bigint not null auto_increment,
    user_id bigint not null,
    board_id bigint not null,
    
-- 좋아요 테이블
    primary key(id),
    UNIQUE KEY uk_like_user_board (user_id, board_id),
    KEY idx_like_user_id (user_id),
    KEY idx_like_board_id (board_id),
    
    constraint fk_like_user
		foreign key (user_id) references todo.`user`(id),
	constraint fk_like_board
		foreign key (board_id) references todo.board(id)
) engine=InnoDB default charset=utf8mb4;