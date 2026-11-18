package com.veterinaria.veterinariaAnimalVet.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/** Removes the legacy global unique index on fecha_hora, which prevented
 * different veterinarians from seeing patients at the same time. */
@Component
public class CitaSchemaMigration implements ApplicationRunner {
    private final DataSource dataSource;

    public CitaSchemaMigration(DataSource dataSource) { this.dataSource = dataSource; }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String table = null;
            try (ResultSet tables = connection.getMetaData().getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    if ("citas".equalsIgnoreCase(tables.getString("TABLE_NAME"))) {
                        table = tables.getString("TABLE_NAME");
                        break;
                    }
                }
            }
            if (table == null) return;
            String product = connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);
            if (product.contains("h2")) {
                // H2 reports the backing index name (ending in _INDEX_B) through JDBC metadata,
                // while DROP CONSTRAINT requires the actual constraint name.
                String constraintSql = "SELECT tc.CONSTRAINT_NAME " +
                        "FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc " +
                        "JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu " +
                        "ON tc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME AND tc.TABLE_NAME = kcu.TABLE_NAME " +
                        "WHERE tc.TABLE_NAME = ? AND tc.CONSTRAINT_TYPE = 'UNIQUE' " +
                        "GROUP BY tc.CONSTRAINT_NAME " +
                        "HAVING COUNT(*) = 1 AND MAX(LOWER(kcu.COLUMN_NAME)) = 'fecha_hora'";
                List<String> constraints = new ArrayList<>();
                try (var query = connection.prepareStatement(constraintSql)) {
                    query.setString(1, table.toUpperCase(Locale.ROOT));
                    try (ResultSet rows = query.executeQuery()) {
                        while (rows.next()) constraints.add(rows.getString("CONSTRAINT_NAME"));
                    }
                }
                for (String constraint : constraints) {
                    String safeConstraint = constraint.replace("\"", "\"\"");
                    String safeTableName = table.replace("\"", "\"\"");
                    try (Statement statement = connection.createStatement()) {
                        statement.execute("ALTER TABLE \"" + safeTableName + "\" DROP CONSTRAINT IF EXISTS \"" + safeConstraint + "\"");
                    }
                }
                return;
            }
            Map<String, Set<String>> columnsByIndex = new HashMap<>();
            Set<String> uniqueIndexes = new HashSet<>();
            try (ResultSet indexes = connection.getMetaData().getIndexInfo(connection.getCatalog(), null, table, true, false)) {
                while (indexes.next()) {
                    String index = indexes.getString("INDEX_NAME");
                    String column = indexes.getString("COLUMN_NAME");
                    if (index != null && column != null) {
                        columnsByIndex.computeIfAbsent(index, ignored -> new HashSet<>()).add(column.toLowerCase(Locale.ROOT));
                        if (!indexes.getBoolean("NON_UNIQUE")) uniqueIndexes.add(index);
                    }
                }
            }
            for (String index : uniqueIndexes) {
                if (!Set.of("fecha_hora").equals(columnsByIndex.get(index))) continue;
                String safeIndex = index.replace("\"", "\"\"");
                String safeTable = table.replace("`", "``");
                String sql = product.contains("mysql")
                        ? "ALTER TABLE `" + safeTable + "` DROP INDEX `" + index.replace("`", "``") + "`"
                        : "ALTER TABLE \"" + table.replace("\"", "\"\"") + "\" DROP CONSTRAINT \"" + safeIndex + "\"";
                try (Statement statement = connection.createStatement()) { statement.execute(sql); }
            }
        }
    }
}
