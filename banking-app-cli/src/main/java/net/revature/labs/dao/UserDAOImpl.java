package net.revature.labs.dao;

import net.revature.labs.dao.util.DBUtil;
import net.revature.labs.model.BankAccount;
import net.revature.labs.model.Transaction;
import net.revature.labs.model.User;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserDAOImpl extends DAO implements UserDAO {

    public UserDAOImpl() throws SQLException, IOException, ClassNotFoundException {
    }

    @Override
    public User registerUser(User user) throws SQLException, ClassNotFoundException, IOException {
        //avoid duplicate email for users
        //check if email already exists
        //if email exists, throw an exception
        //if email does not exist, add the user
        String email = user.getEmail();
        Boolean yes = doesUserAccountExist(email);
        if(yes){
            throw new SQLException("User with email " + email + " already exists");
        }
        String tableName = "users";
        String query = "INSERT INTO \"" + tableName + "\" (name, email, phone, password, isAdmin) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(query)) {
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getPassword());
            stmt.setBoolean(5, user.getIsAdmin());
            stmt.executeUpdate();
        }
        User registeredUser;
        registeredUser = getUserByEmail(email);
        return registeredUser;
    }


    @Override
    public User loginUser(String email, String password) throws SQLException, IOException , ClassNotFoundException{
        String query = "SELECT user_id, name, email, phone, password, isAdmin FROM \"users\" WHERE email = ? AND password = ?";
        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("user_id");
                    String name = rs.getString("name");
                    String userEmail = rs.getString("email");
                    String phone = rs.getString("phone");
                    String userPassword = rs.getString("password");
                    boolean isAdmin = rs.getBoolean("isAdmin");
                    return new User(id, name, userEmail, phone, userPassword, isAdmin);
                }
            }
        }
        return null; // Return null if no matching user found
    }

    @Override
    public User getUserByEmail(String email) throws SQLException {
        User foundUser = null;
        String query = "SELECT * FROM \"users\" WHERE email = ?";
        
        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(query)){
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                foundUser = new User(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("password"),
                    rs.getBoolean("isAdmin")
                );
            }
        }
        return foundUser;
    }


    @Override
    public void updateUser(User user) {
        String updateSQL= "UPDATE \"users\" SET name = ?, email = ?, phone = ?, password = ? WHERE user_id = ?";
        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(updateSQL, PreparedStatement.RETURN_GENERATED_KEYS)){
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getPassword());
            stmt.setInt(5, user.getUserId());
            int rowsUpdated = stmt.executeUpdate();// Use executeUpdate() instead of execute()
            System.out.println("rows updated?: " + rowsUpdated);
        } catch (SQLException e){
            e.printStackTrace();
        }

    }

    @Override
    public void elevateToAdmin(int id) throws SQLException {

    }

    @Override
    public void reduceToUser(int id) throws SQLException {

    }

    @Override
    public void deleteUser(int id) throws SQLException {

    }

    public void deleteUserByEmail(String email) throws SQLException {
        String query = "DELETE FROM \"users\" WHERE email = ?";

        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(query)){
            stmt.setString(1, email);
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteBankAccount(User user) {
    }   

    @Override
    public void addBankAccount(User user) {
    }

    @Override
    public List<BankAccount> getBankAccountsByUser(User user) throws SQLException {
        //fetch all bank accounts for a user
        // for each bank account fetch its transactions
        // then add the transactions to the bank account
        // then add the bank account to the list of bank accounts
        // and return the list of bank accounts

        List<BankAccount> bankAccounts = new ArrayList<>();
        List<Transaction> transactions;

        String bankAccountSQL = "SELECT * FROM \"bank_accounts\" WHERE user_id = ?";
        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(bankAccountSQL)){
            stmt.setInt(1, user.getUserId());
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                Integer userId = Integer.valueOf(rs.getString("user_id"));
                String accountType = rs.getString("account_type");
                String email = rs.getString("email");
                BigDecimal balance = rs.getBigDecimal("balance");
                String accountNumber = rs.getString("account_number");
                transactions = getTransactionsByAccountNumber(accountNumber);
                BankAccount account = new BankAccount(userId, accountType, email, balance, accountNumber, transactions);
                bankAccounts.add(account);
            }
            bankAccounts.stream()
                .map(account -> {
                    account.addTransactions(getTransactionsByAccountNumber(account.getAccountNumber()));
                    return account;
                }).collect(Collectors.toList());
            }
        return bankAccounts;
    }

    private List<Transaction> getTransactionsByAccountNumber(String accountNumber) {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT * FROM \"transactions\" WHERE account_number = ?";
        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(query)){
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                String txId = rs.getString("transaction_id");
                String txAccountNumber = accountNumber;
                String transactionType = rs.getString("transaction_type");
                BigDecimal txAmount = rs.getBigDecimal("amount");
                Timestamp txDateTime = rs.getTimestamp("transaction_date_time");
                String txFromAccountNumber = rs.getString("from_account_number");
                String txToAccountNumber = rs.getString("to_account_number");
                transactions.add(new Transaction(txId, txAccountNumber, transactionType, txAmount, txDateTime, txFromAccountNumber, txToAccountNumber));
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return transactions;
    }

    @Override
    public void updateBankAccount(User user) {
        // Implement the logic to update a bank account for a user
    }

    @Override
    public boolean doesUserAccountExist(String userEmail) {
        boolean yesNo = false;
        String query = "SELECT 1 FROM \"users\" WHERE email = ?";
        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(query)){
            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                yesNo = true;
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return yesNo;
    }

    public User updateUserAndReturnUser(User user) throws SQLException {
        User updatedUser=new User();
        updateUser(user);
        updatedUser = getUserByEmail(user.getEmail());
        return updatedUser;
    }

    public User updateUserById(int ownerUserId, User user) throws SQLException {
        User updatedUser;
        String updateSQL= "UPDATE \"users\" SET name = ?, email = ?, phone = ?, password = ? WHERE user_id = ?";
        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(updateSQL, PreparedStatement.RETURN_GENERATED_KEYS)){
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getPassword());
            stmt.setInt(5, ownerUserId);
            int rowsUpdated = stmt.executeUpdate();// Use executeUpdate() instead of execute()
            System.out.println("rows updated?: " + rowsUpdated);
        } catch (SQLException e){
            e.printStackTrace();
    }
        updatedUser = getUserById(ownerUserId);
        return updatedUser;
    }

    public User getUserById(int ownerUserId) {
        User foundUser = null;
        String query = "SELECT * FROM \"users\" WHERE user_id = ?";
        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(query)){
            stmt.setInt(1, ownerUserId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                foundUser = new User(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("password"),
                    rs.getBoolean("isAdmin")
                );
            }
        } catch (SQLException e){
            e.printStackTrace();
    }
    return foundUser;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM \"users\"";
        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(query)){
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                User user = new User(rs.getInt("user_id"), rs.getString("name"), rs.getString("email"),
                    rs.getString("phone"), rs.getString("password"), rs.getBoolean("isAdmin"));
                users.add(user);
            }
            //fetch BankAccounts and add them to user
            users.stream()
                .map(user -> {
                    try {
                        user.addBankAccounts(getBankAccountsByUser(user));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return user;

                }).collect(Collectors.toList());
        } catch (SQLException e){
            e.printStackTrace();
        }
        return users;
    }
}
