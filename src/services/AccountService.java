package services;

import models.Account;
import utils.JDBCUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountService {
    public void createAccount(String accountHolderName, BigDecimal initialBalance) {
        String sql = "INSERT INTO Accounts (account_holder_name, balance) VALUES (?, ?)";

        try {
            Connection conn = JDBCUtil.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql);

            statement.setString(1, accountHolderName);
            statement.setBigDecimal(2, initialBalance);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Account getAccountDetails(int accountId) {
        String sql = "SELECT * FROM Accounts WHERE account_id = ?";
        Account account = null;

        try {
            Connection conn = JDBCUtil.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql);

            statement.setInt(1, accountId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    account = new Account();
                    account.setAccountId(rs.getInt("account_id"));
                    account.setAccountHolderName(rs.getString("account_holder_name"));
                    account.setBalance(rs.getBigDecimal("balance"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return account;
    }
}
