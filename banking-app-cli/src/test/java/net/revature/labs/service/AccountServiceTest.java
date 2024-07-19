package net.revature.labs.service;

import net.revature.labs.dao.UserDAOImpl;
import net.revature.labs.dao.util.DBUtil;
import net.revature.labs.model.BankAccount;
import net.revature.labs.model.Transaction;
import net.revature.labs.model.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.TestUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AccountServiceTest {
    private AccountService accountService;
    private UserDAOImpl userDAO;

    @BeforeAll
    public static void setup() throws IOException {
        try {
            TestUtil.setEnvironmentToTest();
            DBUtil.resetTestDatabase();
        } catch (SQLException e) {
            fail("Failed to establish database connection: " + e.getMessage());
        }
    }

    @BeforeEach
    public void setupTestObjects() throws ClassNotFoundException, SQLException, IOException{
        accountService = new AccountService();
        userDAO = new UserDAOImpl();
    }
    
    // private void createTestData() throws SQLException, IOException, ClassNotFoundException {
    //     accountService = new AccountService();
    //     userDAO = new UserDAOImpl();
    //     // Register a user for account-related tests
    //     User user = new User("John Doe", "john@example.com", "1234567890", "password", false);
    //     userDAO.registerUser(user);
    //     User registeredUser = userDAO.loginUser(user.getEmail(), user.getPassword());
    //     assertNotNull(registeredUser);
    //     userId = registeredUser.getUserId();
    // }


    @AfterAll
    public static void tearDown() throws SQLException, IOException {
        DBUtil.resetTestDatabase();
    }

    @Test
    public void testCreateewBankAccountWithInvalidInputThrowsException() throws SQLException {
        String userEmail = "random@place.net";
        String invalidAccountType = "Savings Account";
        assertThrows(Exception.class, () -> accountService.createBankAccount(userEmail, invalidAccountType));
        String invalidEmail = "random";
        String validAccountType = "Savings";
        assertThrows(Exception.class, () -> accountService.createBankAccount(invalidEmail, validAccountType));
    }

    @Test
    public void testCreateNewBankAccount() throws SQLException, ClassNotFoundException, IOException {
        //register user
        User newUser = new User("John McAfee", "john@mcafee.com", "7712390876", "passwordgame", false);
        userDAO.registerUser(newUser);
        String email=newUser.getEmail();
        accountService.createBankAccount(email, "savings");
        List<BankAccount> listOfNewUsersAccountsFromDB = accountService.getAllAccountsByUserEmail(newUser.getEmail());
        //verify email
        // verify details of account that was created such as:
            // account type
            // balance
            // account number
            // transactions
            // authorized users
        BankAccount foundAccount = listOfNewUsersAccountsFromDB.get(0);

        assertEquals(newUser.getEmail(), foundAccount.getEmail());
        assertEquals("savings", foundAccount.getAccountType());
        
        BigDecimal accountBalance = new BigDecimal("0.00");
        assertEquals(accountBalance, foundAccount.getBalance());
        
        assertEquals(10, foundAccount.getAccountNumber().length());
        assertEquals(0, foundAccount.getTransactions().size());
        assertEquals(0, foundAccount.getAuthorizedUserEmails().size());
    }

    @Test
    public void testViewBankAccounts() throws SQLException, ClassNotFoundException, IOException {
         //register user
         User userJames = new User("James Bond", "james@bond.com", "9983451223", "strongpass", false);
         userDAO.registerUser(userJames);
         String email=userJames.getEmail();
         accountService.createBankAccount(email, "savings");
         List<BankAccount> listOfJamesAccountsFromDB = accountService.getAllAccountsByUserEmail(userJames.getEmail());
        assertEquals(1, listOfJamesAccountsFromDB.size());
        //verify bankAccount details
        //verify email
        // verify details of account that was created such as:
            // account type
            // balance
            // account number
            // transactions
            // authorized users
        BankAccount foundAccount = listOfJamesAccountsFromDB.get(0);
        assertEquals("savings", foundAccount.getAccountType());
        assertEquals(new BigDecimal("0.00"), foundAccount.getBalance());
        assertEquals(10, foundAccount.getAccountNumber().length());
        assertEquals(0, foundAccount.getTransactions().size());
        assertEquals(0, foundAccount.getAuthorizedUserEmails().size());

    }

    @Test
    public void testViewTransactionHistory() throws SQLException, ClassNotFoundException, IOException {
          //register user
          User newUser = new User("John McAfee", "john@mcafee.com", "7712390876", "passwordgame", false);
          userDAO.registerUser(newUser);
          String email=newUser.getEmail();
          //create bank account
          BankAccount bankAccount = accountService.createBankAccount(email, "savings");
          List<BankAccount> listOfNewUsersAccountsFromDB = accountService.getAllAccountsByUserEmail(newUser.getEmail());
        //do transactions
        BankAccount foundAccount = listOfNewUsersAccountsFromDB.get(0);
        accountService.deposit(bankAccount, new BigDecimal("100.00"));
        accountService.withdraw(bankAccount, new BigDecimal("50.00"));
        //verify transaction history
        List<Transaction> transactions = accountService.getTransactionHistory(foundAccount.getEmail());
        assertEquals(2, transactions.size());
        


        // accountService.createAccount(userId, "Savings Account", BigDecimal.ZERO);
        // BankAccount account = accountService.getAllAccountsByUserEmail(userId).get(0);
        // accountService.deposit(account.getEmail(), new BigDecimal("100.00"));
        // accountService.withdraw(account.getEmail(), new BigDecimal("50.00"));
        // List<Transaction> transactions = accountService.getTransactionHistory(account.getEmail());
        // assertEquals(2, transactions.size());
    }

    @Test
    public void testChangeUserInfo() throws SQLException, ClassNotFoundException, IOException {
        User user = userDAO.loginUser("john@example.com", "password");
        user.setName("John Smith");
        userDAO.updateUser(user);
        User updatedUser = userDAO.loginUser("john@example.com", "password");
        assertEquals("John Smith", updatedUser.getName());
    }

    @Test
    public void testDeleteBankAccount() throws SQLException {
        String userEmail = "deleter@web.com";
        // BankAccount bankAccount = accountService.createBankAccount( userEmail,"savings"); //create account
        // BankAccount foundAccount = accountService.getAllAccountsByUserEmail(userEmail).get(0);
        accountService.deleteAccountByEmail(userEmail);
        Boolean doesAccountExist = accountService.doesAccountExistByEmail(userEmail);
        assertFalse(doesAccountExist);
    }

    @Test
    public void testDeposit() throws SQLException, ClassNotFoundException, IOException {
        //register user
        User user1 = new User("John McAfee", "john@mcafee.com", "7712390876", "passwordgame", false);
        userDAO.registerUser(user1);
        String email=user1.getEmail();
        //create bank account
        BankAccount bankAccount = accountService.createBankAccount(email, "savings");
        //make deposit
        accountService.deposit(bankAccount, new BigDecimal("100.00"));
        List<BankAccount> user1sAccountsFromDB = accountService.getAllAccountsByUserEmail(user1.getEmail());
        //verify balance
        BankAccount depositAccount = user1sAccountsFromDB.get(0);
        assertEquals(new BigDecimal("100.00"), depositAccount.getBalance());
    }
    
    @Test
    public void testWithdraw() throws SQLException, ClassNotFoundException, IOException {
        // register user
        User ramboUser = new User("Sylvester Stallone", "rambo@hollywood.com", "9882346549", "gunsgunsrock", false);
        userDAO.registerUser(ramboUser);
        String userEmail=ramboUser.getEmail();
        //create bank account
        BankAccount bankAccount = accountService.createBankAccount(userEmail, "savings");
        //make deposit
        accountService.deposit(bankAccount, new BigDecimal("100.00"));
        // withdraw
        accountService.withdraw(bankAccount, new BigDecimal("50.00"));

        BankAccount foundAccount = accountService.getAccount(bankAccount.getAccountNumber());
        assertEquals(new BigDecimal("50.00"), foundAccount.getBalance());
        // verify balance
        // verify transaction was added to list of transactions
        // verify transaction details
        assertEquals(bankAccount, foundAccount);
        assertEquals(new BigDecimal("50.00"), foundAccount.getBalance());
        assertEquals(1, foundAccount.getTransactions().size());
        Transaction transaction = foundAccount.getTransactions().get(0);
        assertEquals(bankAccount.getAccountNumber(), transaction.getAccountNumber());
        assertEquals("Withdraw", transaction.getTransactionType());
        assertEquals(new BigDecimal("50.00"), transaction.getAmount());
        assertEquals(bankAccount.getAccountNumber(), transaction.getFromAccountNumber());
        assertEquals(bankAccount.getAccountNumber(), transaction.getToAccountNumber());
        assertEquals(1, foundAccount.getTransactions().size());
    }

    @Test
    public void testTransferMoney() throws SQLException {
        
        // accountService.createAccount(userId, "Savings Account", new BigDecimal("100.00"));
        // accountService.createAccount(userId, "Checking Account", BigDecimal.ZERO);
        // List<BankAccount> accounts = accountService.getAllAccountsByUserEmail(userId);
        // BankAccount sourceAccount = accounts.get(0);
        // BankAccount destinationAccount = accounts.get(1);
        // accountService.transferByEmail(sourceAccount.getEmail(), destinationAccount.getEmail(), new BigDecimal("50.00"));
        // BankAccount updatedSourceAccount = accountService.getAccountByEmail(sourceAccount.getEmail());
        // BankAccount updatedDestinationAccount = accountService.getAccountByEmail(destinationAccount.getEmail());
        // assertEquals(new BigDecimal("50.00"), updatedSourceAccount.getBalance());
        // assertEquals(new BigDecimal("50.00"), updatedDestinationAccount.getBalance());
    }

    @Test
    public void testNegativeDeposit() throws SQLException {
        // accountService.createAccount(userId, "Savings Account", BigDecimal.ZERO);
        // BankAccount account = accountService.getAllAccountsByUserEmail(userId).get(0);
        // assertThrows(IllegalArgumentException.class, () -> accountService.deposit(account.getEmail(), new BigDecimal("-100.00")));
    }

    @Test
    public void testNegativeWithdraw() throws SQLException {
        // accountService.createAccount(userId, "Savings Account", new BigDecimal("100.00"));
        // BankAccount account = accountService.getAllAccountsByUserEmail(userId).get(0);
        // assertThrows(IllegalArgumentException.class, () -> accountService.withdraw(account.getEmail(), new BigDecimal("-50.00")));
    }

    @Test
    public void testExceedWithdraw() throws SQLException {
        // accountService.createAccount(userId, "Savings Account", new BigDecimal("50.00"));
        // BankAccount account = accountService.getAllAccountsByUserEmail(userId).get(0);
        // assertThrows(IllegalArgumentException.class, () -> accountService.withdraw(account.getEmail(), new BigDecimal("100.00")));
    }

    @Test
    public void testAdminElevateUserToAdmin() throws SQLException, ClassNotFoundException, IOException {
        User secondUser = new User("Jane Doe", "jane@example.com", "0987654321", "password", false);
        userDAO.registerUser(secondUser);
        User registeredSecondUser = userDAO.loginUser("jane@example.com", "password");
        userDAO.elevateToAdmin(registeredSecondUser.getUserId());
        User elevatedUser = userDAO.getUserByEmail(registeredSecondUser.getEmail());
        assertTrue(elevatedUser.isAdmin());
    }

    @Test
    public void testAdminReduceAdminToUser() throws SQLException, ClassNotFoundException, IOException {
        User adminUser = new User("Admin User", "admin@example.com", "admin123", "password", true);
        userDAO.registerUser(adminUser);
        User registeredAdminUser = userDAO.loginUser("admin@example.com", "password");
        userDAO.reduceToUser(registeredAdminUser.getUserId());
        User reducedUser = userDAO.getUserByEmail(registeredAdminUser.getEmail());
        assertFalse(reducedUser.isAdmin());
    }

    @Test
    public void testAdminDeleteUserAccount() throws SQLException, ClassNotFoundException, IOException {
        User user = new User("Delete User", "delete@example.com", "delete123", "password", false);
        userDAO.registerUser(user);
        User registeredUser = userDAO.loginUser("delete@example.com", "password");
        userDAO.deleteUser(registeredUser.getUserId());
        User deletedUser = userDAO.getUserByEmail(registeredUser.getEmail());
        assertNull(deletedUser);
    }

    @Test
    public void testAdminDeleteBankAccount() throws SQLException {
        // accountService.createAccount(userId, "Savings Account", BigDecimal.ZERO);
        // BankAccount account = accountService.getAllAccountsByUserEmail(userId).get(0);
        // accountService.deleteAccountByEmail(account.getEmail());
        // BankAccount deletedAccount = accountService.getAccountByEmail(account.getEmail());
        // assertNull(deletedAccount);
    }

    @Test
    public void testBigDecimalPrecision() throws SQLException {
        // accountService.createAccount(userId, "Savings Account", BigDecimal.ZERO);
        // BankAccount account = accountService.getAllAccountsByUserEmail(userId).get(0);
        // accountService.deposit(account.getEmail(), new BigDecimal("0.1"));
        // accountService.deposit(account.getEmail(), new BigDecimal("0.2"));
        // BankAccount updatedAccount = accountService.getAccountByEmail(account.getEmail());
        // assertEquals(new BigDecimal("0.3"), updatedAccount.getBalance());
    }
}
