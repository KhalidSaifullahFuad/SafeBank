package utils;

import java.sql.*;

public class JDBCUtil {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/banking_system_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "123456";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }
}
