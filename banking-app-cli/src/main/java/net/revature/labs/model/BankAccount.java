package net.revature.labs.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BankAccount {
    //bank_account table
    //accounts can only be created once. 
    //They cannot be updated or deleted after creation. Perhaps only by an admin.
    //The only thing that can be updated is the balance.
    // thus fields are final except for balance
    // TODO: add a test to confirm that account details cannot be modified after creation. Assert taht balance can be updated.

    private final Integer userId;
    private final String accountType;
    private BigDecimal balance;
    private final String accountNumber;
    private final String email;
    //transactions table
    private List<Transaction> transactions;
    //authorized_users table
    private List<String> authorizedUserEmails;



    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setAuthorizedUserEmails(List<String> authorizedUserEmails) {
        this.authorizedUserEmails = authorizedUserEmails;
    }

    @JsonCreator
    public BankAccount(
            @JsonProperty("userId") String userId, 
            @JsonProperty("accountType") String accountType, 
            @JsonProperty("email") String email, 
            @JsonProperty("balance") BigDecimal balance, 
            @JsonProperty("accountNumber") String accountNumber, 
            @JsonProperty("transactions") List<Transaction> transactions,
            @JsonProperty("authorizedUserEmails") List<String> authorizedUserEmails
            ){
                this.userId = Integer.parseInt(userId);
                this.accountType = accountType;
                this.email = email;
                this.balance = balance;
                this.accountNumber = accountNumber;
                this.transactions = transactions;
                this.authorizedUserEmails = authorizedUserEmails;
    }

    public BankAccount(Integer userId, String accountType) {
        if(userId == null){
            userId = randomUserId();
        };
        this.userId = userId;
        this.balance = new BigDecimal(0);
        this.accountType = accountType;
        // accountNumber is a 10 digit number random
        this.accountNumber = randomAccountNumber();
        this.transactions = new ArrayList<Transaction>();
        this.authorizedUserEmails = new ArrayList<>();
        this.email = "";
    }
    // public BankAccount(String email, String accountType) {
    //     // this.userId = randomUserId();
    //     this.email = email;
    //     this.balance = new BigDecimal(0);
    //     this.accountType = accountType;
    //     this.accountNumber = randomAccountNumber();
    //     this.transactions = new ArrayList<>();
    //     this.authorizedUserEmails = new ArrayList<>();
    // }

    private String randomAccountNumber() {
        // accountNumber is a 10 digit number random
        return String.valueOf(1000000000L + new Random().nextInt(900000000));
    }

    public BankAccount(User foundUser, String accountType) {
        this.userId=foundUser.getUserId();
        this.email = foundUser.getEmail();
        this.balance = new BigDecimal(0);
        this.accountType = accountType;
        this.accountNumber = randomAccountNumber();
        this.authorizedUserEmails = new ArrayList<>();
        this.transactions = new ArrayList<Transaction>();
    }

    public BankAccount(int userId, String accountType, String email, BigDecimal balance, String accountNumber,
            List<Transaction> transactions) {
        this.userId = userId;
        this.accountType = accountType;
        this.email = email;
        this.balance = balance;
        this.accountNumber = accountNumber;
        this.transactions = transactions;
        this.authorizedUserEmails = new ArrayList<>();
    }
    private Integer randomUserId() {
        return (int) (Math.random()*100000);
    }

    public BigDecimal getBalance() {
        return this.balance;
    }

    public Integer getUserId() {
        return this.userId;
    }

    public String getAccountType() {
        return this.accountType;
    }

    public void deposit(BigDecimal amount, String fromAccountNumber) {
        this.balance = this.balance.add(amount);
        String accountNumber = this.accountNumber;
        String transactionType = "Deposit";
        String toAccountNumber = this.accountNumber;
        Timestamp transactionDateTime = new Timestamp(System.currentTimeMillis());
        Transaction newTx = new Transaction(accountNumber, transactionType, amount, transactionDateTime, fromAccountNumber, toAccountNumber);
        this.transactions.add(newTx);
    }

    public boolean withdraw(BigDecimal amount) {
        if(this.balance.compareTo(amount) < 0){
            return false;
        }
        this.balance = this.balance.subtract(amount);
        return true;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }
    public String getEmail() {
        return this.email;
    }
    public List<Transaction> getTransactions() {
        return this.transactions;
    }

    public static List<BankAccount> fromResultSet(ResultSet rs) throws SQLException {
        List<BankAccount> accounts = new ArrayList<>();
        String accountType;
        String accountNumber;
        BigDecimal balance;
        int userId;
        String email;
        List<Transaction> transactions;
        BankAccount account;
        while (rs.next()) {
            accountNumber = String.valueOf(rs.getInt("account_number"));
            accountType = rs.getString("account_type");
            balance = rs.getBigDecimal("balance");
            userId = rs.getInt("user_id");
            email = rs.getString("email");
            transactions = new ArrayList<>();
            account = new BankAccount(userId, accountType, email, balance, accountNumber, transactions);
            accounts.add(account);
        }
        return accounts;
    }

    public void addSharedUser(String email) {
        this.authorizedUserEmails.add(email);
    }
    public List<String> getAuthorizedUserEmails() {
        return this.authorizedUserEmails;
    }
  
    public void addTransactions(List<Transaction> transactionsForBankAccount) {
        // this.transactions.addAll(transactionsForBankAccount);
        transactionsForBankAccount.forEach(tx -> {
            System.out.println("Adding transaction to account: " + tx);
            System.out.println("Transactionss: " + this.transactions);
            this.transactions.add(tx);
        });
    }
    public void addAuthorizedUsers(List<String> authorizedUsersForBankAccount) {
        this.authorizedUserEmails.addAll(authorizedUsersForBankAccount);
    }
}
