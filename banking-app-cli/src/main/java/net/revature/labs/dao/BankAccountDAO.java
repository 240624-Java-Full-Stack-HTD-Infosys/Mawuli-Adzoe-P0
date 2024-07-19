package net.revature.labs.dao;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import net.revature.labs.dao.util.DBUtil;
import java.util.ArrayList;

import net.revature.labs.model.BankAccount;
import net.revature.labs.model.Transaction;
import net.revature.labs.model.User;

public class BankAccountDAO {
    public BankAccountDAO() throws ClassNotFoundException, SQLException, IOException {
    }

    public BankAccount createBankAccount(String userEmail, String accountType) throws SQLException {
      if(accountType.equals("checking") || accountType.equals("savings")) {
            // User foundUser = userDAO.getUserByEmail(userEmail);

            // if(foundUser == null || !foundUser.getEmail().equals(userEmail)){
            //     throw new IllegalArgumentException("User not found");
            // }
            //user has been found. Use user details to create bank account.

            // BankAccount bankAccount =  new BankAccount(foundUser, accountType);
            boolean userAccountExists = doesUserAccountExist(userEmail);
            if(!userAccountExists){
                throw new IllegalArgumentException("User already has an account");
            }
            User user = getUserByEmail(userEmail);
            BankAccount bankAccount =  new BankAccount(user, accountType);
            saveAccount(bankAccount);
            return bankAccount;
        }
        throw new IllegalArgumentException("Invalid account type. Must be checking or savings");
    }
            
    private User getUserByEmail(String userEmail) {
        User foundUser;
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql)){
            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                Integer userId = rs.getInt("user_id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String password = rs.getString("password");
                Boolean isAdmin = rs.getBoolean("isAdmin");
                foundUser = new User(userId, name, email, phone, password, isAdmin);
            }else{
                foundUser = null;
            }
        } catch (SQLException e){
            e.printStackTrace();
            foundUser = null;
        }
        return foundUser;
    }

    private boolean doesUserAccountExist(String userEmail) {
        //The SELECT 1 statement in SQL is used to check the existence of records in a table
        //  that match a specific condition, without actually retrieving any data from the table. 
        // When you use SELECT 1, the database engine checks for the presence of rows that satisfy 
        // the condition specified in the WHERE clause and returns a 1 for each row that matches the condition. 
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql)){
            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();
            Boolean yesNo = rs.next();
            return yesNo;
        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    // TODO: add a test that confirms that account details cannot be modified and SAVED after creation. Except for the balance. Assert that balance can be updated. 
    private void saveAccount(BankAccount bankAccount) throws SQLException {
        // check the bank account for null before saving
        if(bankAccount != null){
            // 3 things to save: 1) bank account, 2) transactions, 3) authorized users
            if(doesAccountExist(bankAccount.getAccountNumber())){
                // 1) bank account: update balance if account exists. If not insert new account.
                updateBalanceInBankAccountTable(bankAccount.getBalance(), bankAccount.getAccountNumber());
            }else{
                insertIntoBankAccountsTable(bankAccount);
            }
            // 2) transactions: no updates. Only NEW inserts/appends to the table.
            insertIntoTransactionsTable(bankAccount.getTransactions(), bankAccount.getAccountNumber());
            
            // 3) authorized users: No updates. Only NEW inserts/appends to the table.
            insertIntoAuthorizedUsersTable(bankAccount.getAuthorizedUserEmails(), bankAccount.getAccountNumber());
        }
    }

    private boolean doesAccountExist(String accountNumber) throws SQLException {
        //The SELECT 1 statement in SQL is used to check the existence of records in a table
        //  that match a specific condition, without actually retrieving any data from the table. 
        // When you use SELECT 1, the database engine checks for the presence of rows that satisfy 
        // the condition specified in the WHERE clause and returns a 1 for each row that matches the condition. 
        String sql = "SELECT 1 FROM bank_accounts WHERE account_number = ?";
        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql)){
            stmt.setInt(1, Integer.valueOf(accountNumber));
            ResultSet rs = stmt.executeQuery();
            Boolean yesNo = rs.next();
            return yesNo;
        }
    }

    private void insertIntoAuthorizedUsersTable(List<String> authorizedEmails, String accountNumber) throws SQLException {
        //Authorized users: just a list of emails and account_number the email is authorized to access.                        
            // Authorized users may be appended to the authorized_users table. NO UPDATES.
            // Even if user is authorized for an account twice(yes they will be added twice)
            //  but they will still be returned by isUserAuthorizedForAccount(String userEmail) as true.
        String sql2 = "INSERT INTO authorized_users (account_number, authorized_user_email) VALUES (?, ?)";
        try (PreparedStatement stmt2 = DBUtil.getConnection().prepareStatement(sql2, PreparedStatement.RETURN_GENERATED_KEYS)) {
            for(String authorizedEmail : authorizedEmails){
                stmt2.setInt(1, Integer.valueOf(accountNumber));
                stmt2.setString(2, authorizedEmail);
                int generatedKey = stmt2.executeUpdate();
                System.out.println("Generated key: " + generatedKey);
            }
        }
    }

    private void insertIntoTransactionsTable(List<Transaction> transactions, String accountNumber) throws SQLException {
        for(Transaction tx: transactions){
            insertIntoTransactionsTable(tx, accountNumber);
        }
    }

    private void insertIntoTransactionsTable(Transaction tx, String accountNumber) throws SQLException {
        String insertTransactionSQL = "INSERT INTO transactions (account_number, amount, transaction_type, transaction_date_time, from_account_number, to_account_number) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt3 = DBUtil.getConnection().prepareStatement(insertTransactionSQL, PreparedStatement.RETURN_GENERATED_KEYS)){
            stmt3.setInt(1, Integer.valueOf(accountNumber));
            stmt3.setBigDecimal(2, tx.getAmount());
            stmt3.setString(3, tx.getTransactionType());
            stmt3.setTimestamp(4, tx.getTransactionDateTime());
            stmt3.setInt(5, Integer.valueOf(tx.getFromAccountNumber()));
            stmt3.setInt(6, Integer.valueOf(tx.getToAccountNumber()));
            int generatedKey = stmt3.executeUpdate();
            System.out.println("Generated key: " + generatedKey);
        }
    }

    private void insertIntoBankAccountsTable(BankAccount bankAccount) throws NumberFormatException, SQLException {            
        String insertAccountSQL = "INSERT INTO bank_accounts (user_id, email, account_number, account_type, balance) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(insertAccountSQL, PreparedStatement.RETURN_GENERATED_KEYS)){
                stmt.setLong(1, bankAccount.getUserId());
                stmt.setString(2, bankAccount.getEmail());
                stmt.setInt(3, Integer.valueOf(bankAccount.getAccountNumber()));
                stmt.setString(4, bankAccount.getAccountType());
                stmt.setBigDecimal(5, new BigDecimal(0));
                int generatedKey = stmt.executeUpdate();
                System.out.println("Generated key: " + generatedKey);
            }
    }

    private void updateBalanceInBankAccountTable(BigDecimal balance, String accountNumber) throws SQLException { 
        // user bank account already exists so no insert. Update the account.
        // insert method exists for this purpose called insertBankAccount(bankdAccount) if you need it when the user already has an existing bank account
        // update details of the account to the database - 
            // - update bank account details in bank_accounts table
            // update only the balance. Other details should not be updated(e.g. account number, account type, email, user_id)
        // TODO: add a test that confirms that only the balance can be updated unless the user is an admin.

        String updateAccountSQL = "UPDATE bank_accounts SET balance = ? WHERE account_number = ?";
        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(updateAccountSQL, PreparedStatement.RETURN_GENERATED_KEYS)){
            stmt.setBigDecimal(1, balance);
            stmt.setInt(2, Integer.valueOf(accountNumber));
            int generatedKey = stmt.executeUpdate();
            System.out.println("Generated key: " + generatedKey);
        }
    }

    public List<BankAccount> getAccountsByEmail(String userEmail) throws SQLException {
        User foundUser = getUserByEmail(userEmail);
        if(foundUser == null || !foundUser.getEmail().equals(userEmail)){
            throw new IllegalArgumentException("User not found");
        }
        List<BankAccount> bankAccounts = new ArrayList<>();
        // Get all accounts for a user by email
        String sql = "SELECT * FROM bank_accounts WHERE email = ?";
        try(PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql)){
            stmt.setString(1, userEmail);
            // execute the query
            ResultSet rs = stmt.executeQuery();
            // return the list of accounts
            bankAccounts = BankAccount.fromResultSet(rs);
        }
        //for each account fetch its transactions and authorized users and add them to the account
        // for(BankAccount bankAccount : bankAccounts){
        //     bankAccount.addTransactions(findTransactionsForBankAccount(bankAccount.getAccountNumber()));
        //     bankAccount.addAuthorizedUsers(findAuthorizedUsersForBankAccount(bankAccount.getAccountNumber()));}
        bankAccounts = bankAccounts.stream()
            .map(bankAccount -> {
                bankAccount.addTransactions(this.findTransactionsForBankAccount(bankAccount.getAccountNumber()));
                bankAccount.addAuthorizedUsers(this.findAuthorizedUsersForBankAccount(bankAccount.getAccountNumber()));
                return bankAccount;// Return the modified bankAccount object
            })
            .collect(Collectors.toList()); // Collect the results back into a list
        return bankAccounts;
    }

    private List<String> findAuthorizedUsersForBankAccount(String accountNumber) throws NumberFormatException{
        List<String> authorizedUsersForBankAccount = new ArrayList<>();
        String sql = "SELECT * FROM authorized_users WHERE account_number  = ? ";
        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql)){
            stmt.setInt(1, Integer.valueOf(accountNumber));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                String authorizedUserEmail = rs.getString("authorized_user_email");
                authorizedUsersForBankAccount.add(authorizedUserEmail);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return authorizedUsersForBankAccount;
    }

    private List<Transaction> findTransactionsForBankAccount(String accountNumber) throws NumberFormatException{
        String sql = "SELECT * FROM transactions WHERE account_number = ?";
        List<Transaction> transactionsForBankAccount = new ArrayList<>();
        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql)){
            stmt.setInt(1, Integer.valueOf(accountNumber));
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                String transactionType = rs.getString("transaction_type");
                BigDecimal amount = rs.getBigDecimal("amount");
                Timestamp transactionDateTime = rs.getTimestamp("transaction_date_time");
                String transactionId = rs.getString("transaction_id");
                String fromAccountNumber = String.valueOf(rs.getString("from_account_number"));
                String toAccountNumber = String.valueOf(rs.getString("to_account_number"));
                // public Transaction(String accountNumber, String transactionType, BigDecimal amount, 
                // LocalDate transactionDate, LocalTime transactionTime,
                // String fromAccountNumber, String toAccountNumber) {

                Transaction tx = new Transaction(transactionId, accountNumber, transactionType, amount, transactionDateTime, 
                 fromAccountNumber, toAccountNumber);
                transactionsForBankAccount.add(tx);
            } 
        }catch(SQLException e){
                e.printStackTrace();
            }
        
            return transactionsForBankAccount;
    }

    public void shareBankAccountWithEmail(String email, BankAccount bankAccount) throws Exception {
        // Share a bank account with another user by email
        // add the email to the bank account
        if(email.equals(bankAccount.getEmail())){
            throw new Exception("Can't share with yourself!");
        }
        bankAccount.addSharedUser(email);
        saveAccount(bankAccount);
    }

    public List<BankAccount> getAllAccountsByUserEmail(String userEmail) throws SQLException {
        // Get all accounts for a user by email
        User foundUser = getUserByEmail(userEmail);
        if(foundUser == null || !foundUser.getEmail().equals(userEmail)){
            throw new IllegalArgumentException("User not found");
        }
        //user exists now get their accounts
        List<BankAccount> bankAccounts = new ArrayList<>();
        String sql = "SELECT * FROM bank_accounts WHERE email = ?";
        try(PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql)){
            stmt.setString(1, userEmail);
            // execute the query
            ResultSet rs = stmt.executeQuery();
            // return the list of accounts
            bankAccounts = BankAccount.fromResultSet(rs);
        }
        //for each account fetch its transactions and authorized users and add them to the account

        // for(BankAccount bankAccount : bankAccounts){
        //     bankAccount.addTransactions(findTransactionsForBankAccount(bankAccount.getAccountNumber()));
        //     bankAccount.addAuthorizedUsers(findAuthorizedUsersForBankAccount(bankAccount.getAccountNumber()));}
        // fetch using streams
        bankAccounts = bankAccounts.stream()
            .map(bankAccount -> {
                bankAccount.addTransactions(this.findTransactionsForBankAccount(bankAccount.getAccountNumber()));
                bankAccount.addAuthorizedUsers(this.findAuthorizedUsersForBankAccount(bankAccount.getAccountNumber()));
                return bankAccount;// Return the modified bankAccount object
            })
            .collect(Collectors.toList()); // Collect the results back into a list
        return bankAccounts;
    }

    public List<BankAccount> getBankAccountsByUser(User user) throws SQLException {
        List<BankAccount> bankAccounts = getAccountsByEmail(user.getEmail());    
        return bankAccounts;
    }

    public void deposit(BankAccount bankAccount, BigDecimal amount) throws NumberFormatException, SQLException {
        // update the balance in the bank_accounts table
        // insert a new transaction into the transactions table
        
        updateBalanceInBankAccountTable(amount, bankAccount.getAccountNumber());
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        Transaction newTx = new Transaction(bankAccount.getAccountNumber(), "deposit", amount, timestamp, bankAccount.getAccountNumber(), bankAccount.getAccountNumber());
        insertIntoTransactionsTable(newTx, bankAccount.getAccountNumber());
       
}

    public Boolean doesAccountExistByEmail(String userEmail) {
        Boolean doesAccountExist = false;
        String sql = "SELECT 1 FROM bank_accounts WHERE email = ?";
        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql)){
            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();
            doesAccountExist = rs.next();
        } catch (SQLException e){
            e.printStackTrace();
        }
        return doesAccountExist;
    }

    public BankAccount getAccount(String accountNumber) {
        BankAccount bankAccount = null;
        String sql = "SELECT * FROM bank_accounts WHERE account_number = ?";
        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql)){
            stmt.setInt(1, Integer.valueOf(accountNumber));
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                Integer userId = rs.getInt("user_id");
                String email = rs.getString("email");
                String accountType = rs.getString("account_type");
                BigDecimal balance = rs.getBigDecimal("balance");
                bankAccount = new BankAccount(userId, accountType, email, balance, accountNumber, new ArrayList<Transaction>());
            }
            //if account is found and balance is greater than 0
            //then get transactions for account
            // get transactions for account
            // get authorized users for account
            if(bankAccount != null && bankAccount.getBalance().compareTo(BigDecimal.ZERO) > 0){
                List<Transaction> txes = findTransactionsForBankAccount(accountNumber);
                bankAccount.addTransactions(txes);
                bankAccount.addAuthorizedUsers(findAuthorizedUsersForBankAccount(accountNumber));
            }
           
        } catch (SQLException e){
            e.printStackTrace();
        }
        return bankAccount;
    }

    public void deleteAccount(String accountNumber) {
        // delete the account from the bank_accounts table
        // delete all transactions for the account from the transactions table
        // delete all authorized users for the account from the authorized_users table
        String deleteAccountSQL = "DELETE FROM bank_accounts WHERE account_number = ?";
        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(deleteAccountSQL)){
            stmt.setInt(1, Integer.valueOf(accountNumber));
            stmt.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    

    public void withdraw(BankAccount foundBankAccount, BigDecimal amount) {   
        // update the balance in the bank_accounts table
        // insert a new transaction into the transactions table
        // if the balance is less than the amount to withdraw, throw an exception
        if(foundBankAccount.getBalance().compareTo(amount) < 0){
            throw new IllegalArgumentException("Insufficient funds");
        }
        BigDecimal newBalance = foundBankAccount.getBalance().subtract(amount);
        try {
            updateBalanceInBankAccountTable(newBalance, foundBankAccount.getAccountNumber());
            Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
            Transaction newTx = new Transaction(foundBankAccount.getAccountNumber(), "withdraw", amount, timestamp, foundBankAccount.getAccountNumber(), foundBankAccount.getAccountNumber());
            insertIntoTransactionsTable(newTx, foundBankAccount.getAccountNumber());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<BankAccount> getAllAccounts() {
        List<BankAccount> bankAccounts = new ArrayList<>();
        String sql = "SELECT * FROM bank_accounts";
        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql)){
            ResultSet rs = stmt.executeQuery();
            bankAccounts = BankAccount.fromResultSet(rs);
        } catch (SQLException e){
            e.printStackTrace();
        }
        return bankAccounts;
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions";
        try (PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql)){
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                String transactionType = rs.getString("transaction_type");
                BigDecimal amount = rs.getBigDecimal("amount");
                Timestamp transactionDateTime = rs.getTimestamp("transaction_date_time");
                String transactionId = rs.getString("transaction_id");
                String accountNumber = rs.getString("account_number");
                String fromAccountNumber = rs.getString("from_account_number");
                String toAccountNumber = rs.getString("to_account_number");
                Transaction tx = new Transaction(transactionId, accountNumber, transactionType, amount, transactionDateTime, fromAccountNumber, toAccountNumber);
                transactions.add(tx);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return transactions;
    }
}