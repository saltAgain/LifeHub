CREATE TABLE pvp_stats (
                           match_id     CHAR(36) NOT NULL,
                           uuid         CHAR(36) NOT NULL,
                           kills        INT      NOT NULL DEFAULT 0,
                           assists      INT      NOT NULL DEFAULT 0,
                           damage_dealt BIGINT   NOT NULL DEFAULT 0,
                           damage_taken BIGINT   NOT NULL DEFAULT 0,
                           PRIMARY KEY (match_id, uuid),
                           INDEX idx_pvp_stats_uuid (uuid),
  CONSTRAINT fk_pvp_stats_match
    FOREIGN KEY (match_id) REFERENCES game_match(match_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;