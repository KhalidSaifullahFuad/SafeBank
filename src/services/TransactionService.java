package services;

import utils.JDBCUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionService {

    public void deposit(int accountId, BigDecimal amount) {
        synchronized (this) {
            try {
                Connection conn = JDBCUtil.getConnection();
                conn.setAutoCommit(false);

                BigDecimal newBalance = getBalance(conn, accountId).add(amount);
                updateBalance(conn, accountId, newBalance);

                addTransaction(conn, accountId, "DEPOSIT", amount);

                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void withdraw(int accountId, BigDecimal amount) {
        synchronized (this) {
            try {
                Connection conn = JDBCUtil.getConnection();
                conn.setAutoCommit(false);

                BigDecimal currentBalance = getBalance(conn, accountId);
                if (currentBalance.compareTo(amount) < 0) {
                    throw new SQLException("Insufficient balance");
                }

                BigDecimal newBalance = currentBalance.subtract(amount);
                updateBalance(conn, accountId, newBalance);

                addTransaction(conn, accountId, "WITHDRAW", amount);

                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void transfer(int firstAccountId, int secondAccountId, BigDecimal amount) {
        synchronized (this) {
            try (Connection conn = JDBCUtil.getConnection()) {
                conn.setAutoCommit(false);

                BigDecimal firstAccBalance = getBalance(conn, firstAccountId);
                if (firstAccBalance.compareTo(amount) < 0) {
                    throw new SQLException("Insufficient balance");
                }

                BigDecimal updateFirstAccBalance = firstAccBalance.subtract(amount);
                BigDecimal updateSecondAccBalance = getBalance(conn, secondAccountId).add(amount);

                updateBalance(conn, firstAccountId, updateFirstAccBalance);
                updateBalance(conn, secondAccountId, updateSecondAccBalance);

                addTransaction(conn, firstAccountId, "TRANSFER", amount);
                addTransaction(conn, secondAccountId, "TRANSFER", amount);

                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private BigDecimal getBalance(Connection conn, int accountId) throws SQLException {
        String sql = "SELECT balance FROM Accounts WHERE account_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("balance");
                }
            }
        }
        throw new SQLException("Account not found");
    }

    private void updateBalance(Connection conn, int accountId, BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE Accounts SET balance = ? WHERE account_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, newBalance);
            stmt.setInt(2, accountId);
            stmt.executeUpdate();
        }
    }

    private void addTransaction(Connection conn, int accountId, String transactionType, BigDecimal amount) throws SQLException {
        String sql = "INSERT INTO Transactions (account_id, transaction_type, amount) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setString(2, transactionType);
            stmt.setBigDecimal(3, amount);
            stmt.executeUpdate();
        }
    }
}
