package com.loginfy.plugin.database;

import com.loginfy.plugin.Loginfy;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private Connection connection;

    public void setupDatabase() {
        try {
            // Veri klasörü ve dosyası oluşturma
            File dbFile = new File(Loginfy.getInstance().getDataFolder(), "loginfy.db");
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }

            // SQLite bağlantısı
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath());

            Statement stmt = connection.createStatement();
            // users tablosunu oluşturma
            String createSql = "CREATE TABLE IF NOT EXISTS users (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "username TEXT NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "register_date TEXT NOT NULL, " +
                    "last_ip TEXT" +
                    ");";
            stmt.executeUpdate(createSql);

            // Eğer sütun eksikse ekleme (plugin güncellemesi için)
            try {
                String alterSql = "ALTER TABLE users ADD COLUMN last_ip TEXT;";
                stmt.executeUpdate(alterSql);
            } catch (SQLException ignored) {
                // Sütun zaten varsa hata yutulur
            }

            stmt.close();
            System.out.println("[Loginfy] Veritabanı başarıyla oluşturuldu.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
