CREATE TABLE life_player (
                             uuid         CHAR(36)    NOT NULL PRIMARY KEY,
                             display_name VARCHAR(64),
                             first_join   DATETIME    NOT NULL,
                             last_joined  DATETIME    NOT NULL
);

CREATE TABLE life_player_name (
                                  player_uuid  CHAR(36)    NOT NULL,
                                  username     VARCHAR(64) NOT NULL,
                                  first_seen   DATETIME    NOT NULL,
                                  last_seen    DATETIME    NOT NULL,

                                  PRIMARY KEY (player_uuid, username),
                                  FOREIGN KEY (player_uuid)
                                      REFERENCES life_player(uuid)
                                      ON DELETE CASCADE,

                                  INDEX idx_username (username)
);

CREATE TABLE life_player_ip (
                                player_uuid  CHAR(36)     NOT NULL,
                                ip_address   VARCHAR(45)  NOT NULL,
                                first_seen   DATETIME     NOT NULL,
                                last_seen    DATETIME     NOT NULL,

                                PRIMARY KEY (player_uuid, ip_address),
                                FOREIGN KEY (player_uuid)
                                    REFERENCES life_player(uuid)
                                    ON DELETE CASCADE,

                                INDEX idx_ip (ip_address)
);