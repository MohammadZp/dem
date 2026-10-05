package org.example.databaseApp;


import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DatabaseApp {

    public static void main(String[] args) {

        Properties properties = loadProperties();

        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        String createTableSql =
                properties.getProperty("db.createTableSql");

        String insertSql =
                properties.getProperty("db.insertSql");

        String selectSql =
                properties.getProperty("db.selectSql");

        try (Connection conn =
                     DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(createTableSql);
            stmt.executeUpdate(insertSql);

            try (ResultSet rs = stmt.executeQuery(selectSql)) {

                while (rs.next()) {
                    System.out.println(
                            rs.getInt("id")
                                    + ": "
                                    + rs.getString("name")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Properties loadProperties() {

        Properties properties = new Properties();

        try (InputStream input =
                     DatabaseApp.class
                             .getClassLoader()
                             .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new IllegalStateException(
                        "db.properties not found"
                );
            }

            properties.load(input);

            return properties;

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load database configuration",
                    e
            );
        }
    }
}
