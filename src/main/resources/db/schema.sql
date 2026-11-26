CREATE DATABASE IF NOT EXISTS fridge DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fridge;

CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       email VARCHAR(100) COLLATE utf8mb4_bin NULL,
                       password VARCHAR(255) NULL,
                       kakao_id BIGINT NULL,
                       nickname VARCHAR(30) COLLATE utf8mb4_bin NULL,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                       PRIMARY KEY (id),
                       UNIQUE KEY uk_users_email (email),
                       UNIQUE KEY uk_users_kakao_id (kakao_id),
                       UNIQUE KEY uk_users_nickname (nickname)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE fridge_groups
(
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50) NOT NULL,
    invite_code VARCHAR(20) NOT NULL,
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY `uk_fridge_groups_invite_code` (invite_code)
)ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE group_members(
                              id BIGINT NOT NULL AUTO_INCREMENT,
                              group_id BIGINT NOT NULL,
                              user_id BIGINT NOT NULL,
                              role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
                              joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                              PRIMARY KEY (id),
                              UNIQUE KEY uk_group_members_group_user (group_id, user_id),
                              CONSTRAINT fk_group_members_group FOREIGN KEY (group_id) REFERENCES fridge_groups (id),
                              CONSTRAINT fk_group_members_user FOREIGN KEY (user_id) REFERENCES users(id)
)ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE ingredients (
                             id BIGINT NOT NULL AUTO_INCREMENT,
                             group_id BIGINT,
                             owner_id BIGINT,
                             name VARCHAR(50),
                             quantity BIGINT,
                             unit VARCHAR(10),
                             storage_type VARCHAR(20),
                             is_shared BOOLEAN,
                             purchased_at DATE,
                             expires_at DATE,
                             notify_days_before INT,
                             status VARCHAR(20),
                             memo VARCHAR(255),
                             created_at DATETIME,
                             updated_at DATETIME,

                             PRIMARY KEY (id),
                             CONSTRAINT fk_ingredients_group
                                 FOREIGN KEY (group_id) REFERENCES fridge_groups(id),
                             CONSTRAINT fk_ingredients_owner
                                 FOREIGN KEY (owner_id) REFERENCES users(id)
)ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE ingredient_histories (
                                      id BIGINT NOT NULL AUTO_INCREMENT,
                                      ingredient_id BIGINT NOT NULL,
                                      user_id BIGINT, ##서비스 탈퇴 user가 삭제되므로 NULL
                                      action VARCHAR(30),
                                      quantity_change BIGINT,
                                      detail VARCHAR(255) COLLATE utf8mb4_bin NULL,
                                      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      PRIMARY KEY (id),
                                      CONSTRAINT fk_ingredient_histories_ingredient
                                          FOREIGN KEY (ingredient_id) REFERENCES ingredients (id),
                                      CONSTRAINT fk_ingredient_histories_user
                                          FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE notifications (
                               id BIGINT NOT NULL AUTO_INCREMENT,
                               user_id BIGINT NOT NULL,
                               ingredient_id BIGINT NOT NULL,
                               group_id BIGINT NOT NULL,
                               type VARCHAR(30) NOT NULL,
                               message VARCHAR(255) COLLATE utf8mb4_bin NULL,
                               is_read BOOLEAN NOT NULL DEFAULT FALSE,
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               PRIMARY KEY (id),
                               CONSTRAINT fk_notifications_user
                                   FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE, #부모 삭제시 자식도
                               CONSTRAINT fk_notifications_group
                                   FOREIGN KEY (group_id) REFERENCES fridge_groups (id) ON DELETE CASCADE,
                               CONSTRAINT fk_notifications_ingredient
                                   FOREIGN KEY (ingredient_id) REFERENCES ingredients (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE notification_settings (
                                       id BIGINT NOT NULL AUTO_INCREMENT,
                                       user_id BIGINT NOT NULL,
                                       group_id BIGINT NOT NULL,
                                       enabled BOOLEAN DEFAULT TRUE NOT NULL,
                                       receive_others BOOLEAN DEFAULT TRUE NOT NULL,
                                       notify_time TIME NOT NULL DEFAULT '09:00:00',


                                       PRIMARY KEY (id),
                                       UNIQUE KEY uk_notification_settings_user_group (user_id, group_id),
                                       CONSTRAINT fk_notification_settings_user
                                           FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE, #부모 삭제시 자식도
                                       CONSTRAINT fk_notification_settings_group
                                           FOREIGN KEY (group_id) REFERENCES fridge_groups (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
CREATE TABLE join_requests (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    group_id     BIGINT      NOT NULL,
    user_id      BIGINT      NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    decided_at   DATETIME    NULL,

    PRIMARY KEY (id),
    KEY idx_join_requests_group_status (group_id, status),
    CONSTRAINT fk_join_requests_group FOREIGN KEY (group_id) REFERENCES fridge_groups (id),
    CONSTRAINT fk_join_requests_user  FOREIGN KEY (user_id)  REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
