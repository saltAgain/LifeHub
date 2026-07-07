package dev.saltt.hub;

import com.hypixel.hytale.codec.Codec;          // NOTE: this Codec, not com.mojang or others
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

// Saved as JSON under: mods/<Group>_<Name>/LifeHub.json  (keys MUST be capitalized)
public class HubConfig {

    public static final BuilderCodec<HubConfig> CODEC = BuilderCodec.builder(HubConfig.class, HubConfig::new)
            .append(new KeyedCodec<String>("JdbcUrl", Codec.STRING),
                    (c, v, info) -> c.jdbcUrl = v,    (c, info) -> c.jdbcUrl).add()
            .append(new KeyedCodec<String>("DbUser", Codec.STRING),
                    (c, v, info) -> c.dbUser = v,     (c, info) -> c.dbUser).add()
            .append(new KeyedCodec<String>("DbPassword", Codec.STRING),
                    (c, v, info) -> c.dbPassword = v, (c, info) -> c.dbPassword).add()
            .append(new KeyedCodec<Integer>("DbPoolSize", Codec.INTEGER),
                    (c, v, info) -> c.dbPoolSize = v, (c, info) -> c.dbPoolSize).add()
            .append(new KeyedCodec<String>("ApiBind", Codec.STRING),
                    (c, v, info) -> c.apiBind = v,    (c, info) -> c.apiBind).add()
            .append(new KeyedCodec<Integer>("ApiPort", Codec.INTEGER),
                    (c, v, info) -> c.apiPort = v,    (c, info) -> c.apiPort).add()
            .append(new KeyedCodec<String>("ApiToken", Codec.STRING),
                    (c, v, info) -> c.apiToken = v,   (c, info) -> c.apiToken).add()
            .append(new KeyedCodec<Integer>("ApiThreads", Codec.INTEGER),
                    (c, v, info) -> c.apiThreads = v, (c, info) -> c.apiThreads).add()
            .build();

    private String jdbcUrl    = "jdbc:mysql://localhost:3306/life?connectionTimeZone=UTC";
    private String dbUser     = "life";
    private String dbPassword = "change-me";
    private int    dbPoolSize = 10;
    private String apiBind    = "127.0.0.1";
    private int    apiPort    = 8080;
    private String apiToken   = "change-me-to-a-long-random-secret";
    private int    apiThreads = 4;

    public HubConfig() {}

    public String getJdbcUrl()    { return jdbcUrl; }
    public String getDbUser()     { return dbUser; }
    public String getDbPassword() { return dbPassword; }
    public int    getDbPoolSize() { return dbPoolSize; }
    public String getApiBind()    { return apiBind; }
    public int    getApiPort()    { return apiPort; }
    public String getApiToken()   { return apiToken; }
    public int    getApiThreads() { return apiThreads; }
}