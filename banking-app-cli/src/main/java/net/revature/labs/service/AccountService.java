package net.revature.labs.service;

import net.revature.labs.dao.BankAccountDAO;
import net.revature.labs.model.BankAccount;
import net.revature.labs.model.Transaction;
import net.revature.labs.model.User;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class AccountService {
    private BankAccountDAO accountDAO;
    public AccountService() throws SQLException, IOException, ClassNotFoundException {
        this.accountDAO = new BankAccountDAO();
    }

    public BankAccount createBankAccount(String userEmail, String accountType) throws SQLException {
        return accountDAO.createBankAccount(userEmail, accountType);
    }

    public List<BankAccount> getAllAccountsByUserEmail(String userEmail) throws SQLException {
        return accountDAO.getAllAccountsByUserEmail(userEmail);
    }
    public void deposit(BankAccount bankAccount, BigDecimal amount) throws NumberFormatException, SQLException{
        accountDAO.deposit(bankAccount, amount);
    }


    public List<Transaction> getTransactionHistory(String email) {
        return null;
    }

    public void deleteAccount(int id) {

    }

    public BankAccount getAccount(String accountNumber) {
        return accountDAO.getAccount(accountNumber);
    }

    public void transfer(int id, int id1, BigDecimal bigDecimal) {

    }

    public void deleteAccountByEmail(String email) {
    }

    public BankAccount getAccountByEmail(String email) {
        return null;
    }

    public void transferByEmail(String email, String email1, BigDecimal bigDecimal) {
    }

    public Boolean doesAccountExistByEmail(String userEmail) {
        return accountDAO.doesAccountExistByEmail(userEmail);
    }

    public BankAccount createAccount(BankAccount account) throws SQLException {
        String userEmail = account.getEmail();
        String accountType = account.getAccountType();
        BankAccount addedAccount = accountDAO.createBankAccount(userEmail, accountType);
        return addedAccount;
    }

    public void deposit(BankAccount bankAccount, String amount) throws NumberFormatException, SQLException {
        accountDAO.deposit(bankAccount, BigDecimal.valueOf(Long.valueOf(amount)));
    }

    public void deleteAccount(String accountNumber) {
        BankAccount account = accountDAO.getAccount(accountNumber);
        if(account.getBalance().compareTo(BigDecimal.ZERO) == 0){
            accountDAO.deleteAccount(accountNumber);
        }else{
            throw new IllegalArgumentException("Account is not empty");
        }
    }

    public void withdraw(BankAccount foundBankAccount, BigDecimal amount) {
        accountDAO.withdraw(foundBankAccount, amount);   
    }

    public List<BankAccount> getAllAccounts() {
        return accountDAO.getAllAccounts();
    }

    public List<Transaction> getAllTransactions() {
        return accountDAO.getAllTransactions();
    }

    public void deleteAccountForAdmin(String accountNumber, User user) {
       if(user.isAdmin()){
            accountDAO.deleteAccount(accountNumber);
       }
    }
}
