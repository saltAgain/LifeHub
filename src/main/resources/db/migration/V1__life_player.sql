CREATE TABLE life_player (
                             uuid         CHAR(36)    NOT NULL PRIMARY KEY,
                             display_name VARCHAR(64),
                             first_join   DATETIME    NOT NULL,
                             last_joined  DATETIME    NOT NULL,
                             past_names   JSON        NOT NULL,
                             used_ips     JSON        NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;