CREATE TABLE users (
        id                BIGINT          NOT NULL AUTO_INCREMENT COMMENT '아이디 (PK)',
        email             VARCHAR(255)    NOT NULL COMMENT '이메일',
        name              VARCHAR(100)    NOT NULL COMMENT '이름',
        password          VARCHAR(255)    NULL     COMMENT '비밀번호 (소셜 가입 시 NULL)',
        provider          ENUM('LOCAL', 'GOOGLE', 'KAKAO', 'NAVER') NULL COMMENT '소셜 로그인 제공자',
        provider_id       VARCHAR(255)    NULL     COMMENT '소셜 고유 아이디 (sub 등)',
        profile_image_url VARCHAR(512)    NULL     COMMENT '프로필 이미지 URL',
        created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at          TIMESTAMP   NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        PRIMARY KEY (id),
        CONSTRAINT uk_provider_provider_id UNIQUE (provider, provider_id),
        CONSTRAINT uk_user_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;