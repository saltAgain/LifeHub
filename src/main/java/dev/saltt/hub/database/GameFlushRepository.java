package dev.saltt.hub.database;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class GameFlushRepository<T> {

    protected final Jdbi jdbi;
    protected final String table;
    protected final List<String> keyColumns;
    protected final List<String> columns;
    protected final RowMapper<T> mapper;
    private final String upsertSql;

    protected GameFlushRepository(Jdbi jdbi, String table, List<String> keyColumns,
                                  List<String> columns, RowMapper<T> mapper) {
        this.jdbi = jdbi;
        this.table = table;
        this.keyColumns = List.copyOf(keyColumns);
        this.columns = List.copyOf(columns);
        this.mapper = mapper;
        this.upsertSql = buildUpsert();
    }

    /** Column name -> value. Use HashMap when any value may be null. */
    protected abstract Map<String, Object> toRow(T entity);

    private String buildUpsert() {
        String cols = String.join(", ", columns);
        String vals = columns.stream().map(c -> ":" + c).collect(Collectors.joining(", "));
        List<String> nonKey = columns.stream()
                .filter(c -> !keyColumns.contains(c))
                .collect(Collectors.toList());

        if (nonKey.isEmpty()) {
            return "INSERT IGNORE INTO " + table + " (" + cols + ") VALUES (" + vals + ")";
        }
        String updates = nonKey.stream()
                .map(c -> c + " = VALUES(" + c + ")")
                .collect(Collectors.joining(", "));
        return "INSERT INTO " + table + " (" + cols + ") VALUES (" + vals + ") "
                + "ON DUPLICATE KEY UPDATE " + updates;
    }

    public void save(T entity) {
        jdbi.useHandle(h -> save(h, entity));
    }

    /** Participates in a caller-managed transaction. */
    public void save(Handle handle, T entity) {
        handle.createUpdate(upsertSql).bindMap(toRow(entity)).execute();
    }

    public void saveAll(Collection<T> entities) {
        jdbi.useTransaction(h -> entities.forEach(e -> save(h, e)));
    }

    public List<T> find(String whereClause, Map<String, Object> params) {
        return jdbi.withHandle(h -> h.createQuery("SELECT * FROM " + table + " WHERE " + whereClause)
                .bindMap(params).map(mapper).list());
    }

    public Optional<T> findOne(String whereClause, Map<String, Object> params) {
        return jdbi.withHandle(h -> h.createQuery("SELECT * FROM " + table + " WHERE " + whereClause)
                .bindMap(params).map(mapper).findOne());
    }

    public List<T> all() {
        return jdbi.withHandle(h -> h.createQuery("SELECT * FROM " + table).map(mapper).list());
    }

    public int delete(String whereClause, Map<String, Object> params) {
        return jdbi.withHandle(h -> h.createUpdate("DELETE FROM " + table + " WHERE " + whereClause)
                .bindMap(params).execute());
    }
}