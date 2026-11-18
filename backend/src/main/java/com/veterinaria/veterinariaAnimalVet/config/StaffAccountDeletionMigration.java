package com.veterinaria.veterinariaAnimalVet.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;

/** Allows historical clinical records to retain their provider snapshot after account deletion. */
@Component
public class StaffAccountDeletionMigration implements ApplicationRunner {
    private final DataSource dataSource;

    public StaffAccountDeletionMigration(DataSource dataSource) { this.dataSource = dataSource; }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String product = connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);
            String table = findTable(connection, "consultas_medicas");
            if (table == null || isNullable(connection, table, "veterinario_id")) return;

            String sql;
            if (product.contains("mysql")) {
                sql = "ALTER TABLE `" + table.replace("`", "``") + "` MODIFY COLUMN `veterinario_id` BIGINT NULL";
            } else if (product.contains("h2")) {
                sql = "ALTER TABLE \"" + table.replace("\"", "\"\"") + "\" ALTER COLUMN veterinario_id DROP NOT NULL";
            } else {
                throw new IllegalStateException("La migración de eliminación de personal no reconoce la base de datos: " + product);
            }
            try (Statement statement = connection.createStatement()) { statement.execute(sql); }
        }
    }

    private String findTable(Connection connection, String expected) throws Exception {
        try (ResultSet tables = connection.getMetaData().getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"})) {
            while (tables.next()) if (expected.equalsIgnoreCase(tables.getString("TABLE_NAME"))) return tables.getString("TABLE_NAME");
        }
        return null;
    }

    private boolean isNullable(Connection connection, String table, String column) throws Exception {
        try (ResultSet columns = connection.getMetaData().getColumns(connection.getCatalog(), null, table, "%")) {
            while (columns.next()) {
                if (column.equalsIgnoreCase(columns.getString("COLUMN_NAME"))) {
                    return "YES".equalsIgnoreCase(columns.getString("IS_NULLABLE"));
                }
            }
        }
        return true;
    }
}
