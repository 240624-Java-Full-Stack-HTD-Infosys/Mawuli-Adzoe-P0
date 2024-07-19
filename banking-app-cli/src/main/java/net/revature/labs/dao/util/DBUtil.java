package net.revature.labs.dao.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.github.javafaker.Faker;

import net.revature.labs.model.BankAccount;
import net.revature.labs.model.Transaction;
import net.revature.labs.model.User;

public class DBUtil {
    private static Properties properties;
    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;
    private static String env = System.getProperty("env");
    private static String propFile;
    private static Connection connection = getConnection();
    static {
        init();
    }

    private static void init() {
        loadEnv();
        try {
            loadPropsAndStartPostgres();
        } catch (Exception e) {
            e.printStackTrace();
        }   
    }

    private static void loadEnv(){
        System.out.println("env is: " + DBUtil.env);
        properties = new Properties();
        env = System.getProperty("env", "dev");
        System.out.println("env: " + DBUtil.env);
        propFile = "application-" + DBUtil.env + ".properties";
        System.out.println("propFile: " + propFile);

    }

    private static void loadPropsAndStartPostgres() throws SQLException, ClassNotFoundException {
        try(InputStream input = DBUtil.class.getClassLoader().getResourceAsStream(propFile)){
            if(input == null){
                throw new FileNotFoundException("File not found");
            }
            DBUtil.properties.load(input);
            DBUtil.dbUrl = DBUtil.properties.getProperty(DBUtil.env + ".db.url");
            DBUtil.dbUser = DBUtil.properties.getProperty(DBUtil.env + ".db.user");
            DBUtil.dbPassword = DBUtil.properties.getProperty(DBUtil.env + ".db.password");
            System.out.println("dbUrl: " + dbUrl);
            if (connection == null) {
               Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            }
        } catch (IOException e){
            throw new RuntimeException("PostgreSQL Driver not found", e);
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                init();
                return connection;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Getter methods for dbUrl, dbUser, dbPassword
    public static String getDbUrl() {
        return dbUrl;
    }

    public static String getDbUser() {
        return dbUser;
    }

    public static String getDbPassword() {
        return dbPassword;
    }

    //drop and recreate database tables
    public static void resetTestDatabase() throws SQLException, IOException {
        //execute SQL script to drop database tables
        try (Statement stmt = connection.createStatement()) {
            String sql = new String(Files.readAllBytes(Paths.get("src/main/resources/TestDbSetup.sql")));
            stmt.execute(sql);
        }
    }

    public static List<User> insertAndReturnUsers(List<User> usersToInsert) throws SQLException {
        String sql = "INSERT INTO users (name, email, phone, password, isadmin) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;

        try{
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

            for (User user : usersToInsert) {
                stmt.setString(1, user.getName());
                stmt.setString(2, user.getEmail());
                stmt.setString(3, user.getPhone());
                stmt.setString(4, user.getPassword());
                stmt.setBoolean(5, user.getIsAdmin());
                stmt.addBatch();// Add to batch instead of executing immediately
            }
                int[] affectedRows = stmt.executeBatch();
                //Retrieve the generated keys
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    for (User user : usersToInsert) {
                        if (generatedKeys.next()) {
                            user.setUserId(generatedKeys.getInt(1));
                        } else {
                            throw new SQLException("Creating user failed, no ID obtained.");
                        }
                    }
                }
                conn.commit(); // Commit the transaction

                // Check the affected rows for each insert if needed
                for (int rows : affectedRows) {
                    if (rows == 0) {
                        // Handle the case where an insert did not affect any rows
                        throw new SQLException("Insert failed, no rows affected.");
                    }
                }
            } catch (SQLException e) {
                    if (conn != null) {
                        try {
                            conn.rollback(); // Rollback the transaction in case of any failure
                        } catch (SQLException ex) {
                            System.err.println("Error during transaction rollback: " + ex.getMessage());
                        }
                    }
                    e.printStackTrace();
                } finally {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    if (conn != null) {
                        try {
                            conn.setAutoCommit(true); // Restore auto-commit mode
                            conn.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
        return usersToInsert;
    }


    public static void insertBankAccount(BankAccount account) {
        String insertSQL = "INSERT INTO bank_accounts (user_id, account_type, balance) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertSQL)) {
            stmt.setInt(1, account.getUserId());
            stmt.setString(2, account.getAccountType());
            stmt.setBigDecimal(3, account.getBalance());
            int rowCount = stmt.executeUpdate();
            if (rowCount == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }
            System.out.println(rowCount + " row(s) inserted");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertTransaction(Transaction tx) {
        String insertSQL = "INSERT INTO transactions (account_number, transaction_type, amount, transaction_date_time, from_account_number, to_account_number) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(insertSQL)) {
            stmt.setInt(1, Integer.valueOf(tx.getAccountNumber()));
            stmt.setString(2, tx.getTransactionType());
            stmt.setBigDecimal(3, tx.getAmount());
            stmt.setTimestamp(4, tx.getTransactionDateTime());
            stmt.setInt(5, Integer.valueOf(tx.getFromAccountNumber()));
            stmt.setInt(6, Integer.valueOf(tx.getToAccountNumber()));
            int rowCount = stmt.executeUpdate();
            if (rowCount == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }
            System.out.println(rowCount + " row(s) inserted");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertBankAccounts(List<BankAccount> listOfBankAccounts) throws SQLException {
        //batch insert
        //turn off auto commit
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            // Ensure the connection is valid
            if (conn == null || conn.isClosed()) {
                System.err.println("Connection is closed or null.");
                return;
            }
            
            conn.setAutoCommit(false);
            // Assume there's code here for inserting bank accounts
            
            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback in case of exception
                } catch (SQLException ex) {
                    ex.printStackTrace(); // Log rollback exception
                }
            }
            e.printStackTrace(); // Log original exception
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Log exception during cleanup
            }
        }
    }

    public static void fetchUsersUsesThemToGenerateBankAccountsAndTransactions() throws SQLException {
        //fetch all users
        //use their id to generate bank accounts
        //use bank account number to generate transactions
        
        try {
             Connection conn = DBUtil.getConnection();
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Unable to establish a valid connection.");
            }
            conn.setAutoCommit(false); // Start transaction

            // selct only 10 users
            String sql = "SELECT * FROM users LIMIT 10";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    int userId = rs.getInt("user_id");
                    // Generate bank accounts
                    List<BankAccount> bankAccounts = generateBankAccounts(5, userId);
                    // Insert bank accounts
                    insertBankAccounts(bankAccounts);
                    // Generate transactions
                    List<Transaction> transactions = generateTransactions(3, bankAccounts);
                    // Insert transactions
                    insertTransactions(transactions);
                }
    // Before committing, check if the connection is still open
    if (conn != null && !conn.isClosed()) {
        conn.commit(); // Commit transaction
    }
    } catch (SQLException e) {
        if (conn != null) {
            try {
                // Before rolling back, also check if the connection is still open
                if (!conn.isClosed()) {
                    conn.rollback(); // Rollback transaction in case of any failure
                }
            } catch (SQLException ex) {
                ex.printStackTrace(); // Log rollback exception
            }
        }
        e.printStackTrace(); // Log original exception
    } finally {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close(); // Ensure connection is closed
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log exception during cleanup
        } finally {
            DBUtil.closeConnection(); // Close connection
        }
    }
    }finally {
    try {
        if (connection != null && !connection.isClosed()) {
            connection.close(); // Close connection
        }
    } catch (SQLException e) {
        e.printStackTrace(); // Log exception during cleanup
    }finally {
        DBUtil.closeConnection(); }}// Close connection
    }
    private static void insertTransactions(List<Transaction> transactions) {
        //batch insert
        //turn off auto commit
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            // Ensure the connection is valid
            if (conn == null || conn.isClosed()) {
                System.err.println("Connection is closed or null.");
                return;
            }
            
            conn.setAutoCommit(false);
            String insertSQL = "INSERT INTO transactions (account_number, transaction_type, amount, transaction_date_time, from_account_number, to_account_number) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
                for (Transaction tx : transactions) {
                    stmt.setInt(1, Integer.valueOf(tx.getAccountNumber()));
                    stmt.setString(2, tx.getTransactionType());
                    stmt.setBigDecimal(3, tx.getAmount());
                    stmt.setTimestamp(4, tx.getTransactionDateTime());
                    stmt.setInt(5, Integer.valueOf(tx.getFromAccountNumber()));
                    stmt.setInt(6, Integer.valueOf(tx.getToAccountNumber()));
                    stmt.addBatch();
                }
                int[] rowCount = stmt.executeBatch();
                for (int rows : rowCount) {
                    if (rows == 0) {
                        throw new SQLException("Insert failed, no rows affected.");
                    }
                }
            }
            
            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback in case of exception
                } catch (SQLException ex) {
                    ex.printStackTrace(); // Log rollback exception
                }
            }
            e.printStackTrace(); // Log original exception
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Log exception during cleanup
            }
        }
    }

    private static List<Transaction> generateTransactions(int i, List<BankAccount> bankAccounts) {
        List<Transaction> listOfTransactions = new ArrayList<>();
        List<Transaction> listOfDepostiTxes = generateDepositTransactionsForAccount(i, dbPassword);
        listOfTransactions.addAll(listOfDepostiTxes);
        List<Transaction> listOfWithdrawalTransactions = generateWithdrawalTransactionsForAccount(i, dbPassword);
        listOfTransactions.addAll(listOfWithdrawalTransactions);
        return listOfTransactions;
    }

    private static List<Transaction> generateWithdrawalTransactionsForAccount(int i, String accountNumber) {
        //generate i number of debit transactions
        List<Transaction> iTxes = new ArrayList<>();
            for(int j = 0; j < i; j++) {
                Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
                BigDecimal amount = new BigDecimal(Math.random() * 1000);
                Faker faker = new Faker();
                String fromAccountNumber = accountNumber;
                String toAccountNumber = faker.number().digits(7);
                String transactionType = "withdrawal";
                Transaction tx = new Transaction(accountNumber, transactionType, amount, timestamp, fromAccountNumber, toAccountNumber);
                iTxes.add(tx);
            }
        return iTxes;
    }

    private static List<Transaction> generateDepositTransactionsForAccount(int i, String accountNumber) {
        //generate i number of debit transactions
        List<Transaction> iTxes = new ArrayList<>();
            for(int j = 0; j < i; j++) {
                Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
                BigDecimal amount = new BigDecimal(Math.random() * 1000);
                Faker faker = new Faker();
                String fromAccountNumber = faker.number().digits(7);
                String toAccountNumber = accountNumber;
                String transactionType = "deposit";
                Transaction tx = new Transaction(accountNumber, transactionType, amount, timestamp, fromAccountNumber, toAccountNumber);
                iTxes.add(tx);
            }
        return iTxes;
    }

    static List<BankAccount> generateBankAccounts(int i, int userId) {
        List<BankAccount> bankAccounts = new ArrayList<>();
        for(int j = 0; j < i; j++) {
            Faker faker = new Faker();
            //random checking or savings account
            String accountType = faker.options().option("checking", "savings");
            BankAccount account = new BankAccount(userId, accountType);
            bankAccounts.add(account);
        }
        return bankAccounts;
    }
    private static List<User> generateUserAccounts(int i) {
        List<User> userAccounts = new ArrayList<>();
        for(int j = 0; j < i; j++) {
            Faker faker = new Faker();
            String name = faker.name().fullName().replaceAll("[^a-zA-Z -]", "");
            String email = faker.internet().emailAddress();
            String phone = faker.number().digits(10);
            String password = faker.internet().password();
            Boolean isAdmin = false;
            User user = new User(name, email, phone, password, isAdmin);
            userAccounts.add(user);
        }
        return userAccounts;
    }
}