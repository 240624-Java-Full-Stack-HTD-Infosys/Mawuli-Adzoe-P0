package net.revature.labs.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import net.revature.labs.model.BankAccount;
import net.revature.labs.model.User;

public interface UserDAO {
    // Create
    User registerUser(User user) throws SQLException, ClassNotFoundException, IOException;
    // Read
    User loginUser(String email, String password) throws SQLException, ClassNotFoundException, IOException;

    User getUserByEmail(String email) throws SQLException;

    // Update
    void updateUser(User user) throws SQLException;

    // public void addUser(User user) throws SQLException;

    void elevateToAdmin(int id) throws SQLException;

    void reduceToUser(int id) throws SQLException;

    // Delete
    void deleteUser(int id) throws SQLException;
    // List<BankAccount> bankAccounts CRUD methods
    void addBankAccount(User user) throws SQLException;
    void updateBankAccount(User user) throws SQLException;
    void deleteBankAccount(User user) throws SQLException;
    List<BankAccount> getBankAccountsByUser(User user) throws SQLException;

    boolean doesUserAccountExist(String userEmail);
    

}