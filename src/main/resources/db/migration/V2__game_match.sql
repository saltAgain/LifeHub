CREATE TABLE game_match (
                            match_id   CHAR(36)    NOT NULL PRIMARY KEY,
                            game_type  VARCHAR(32) NOT NULL,
                            start_time TIME        NOT NULL,
                            INDEX idx_game_match_type (game_type)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;