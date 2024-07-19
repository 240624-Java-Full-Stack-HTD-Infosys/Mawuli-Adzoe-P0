package net.revature.labs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.revature.labs.controller.BankingAPIController;
import net.revature.labs.dao.util.DBUtil;
import net.revature.labs.model.BankAccount;
import net.revature.labs.model.Transaction;
import net.revature.labs.model.User;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import util.TestUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
// Separation of Cleanup Logic:
//  @AfterEach for cleaning up test-specific resources, 
//  @AfterAll handles stopping the server and database resets. 
// This keeps cleanup organized and ensures the server only stops once after all tests have run.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BankingAPITest {
    private BankingAPIController bankingAPIController;
    private HttpClient webClient;
    private ObjectMapper objectMapper;

    @BeforeAll
    public static void setUp() throws InterruptedException, SQLException, IOException, ClassNotFoundException {
        TestUtil.setEnvironmentToTest();
        DBUtil.resetTestDatabase();
       Thread.sleep(1000); // ensure the environment is ready
    }

    @BeforeEach
    public void init() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        bankingAPIController = new BankingAPIController();
        webClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
        Thread.sleep(2000); // Add delay to ensure server is fully started
    }
    
    @AfterEach
    public void tearDown() throws SQLException, IOException {
        webClient = null;
        objectMapper = null;
    }

    @AfterAll
    public void cleanUp() throws SQLException, IOException{
        BankingAPIController.stopAPI();
        DBUtil.resetTestDatabase();
    }
    
    @Test
    void guest_can_register_a_new_user_account_and_get_back_a_user_object_json() throws IOException, InterruptedException {
        // As a user, I should be able to create a new Account on the endpoint POST /register. 
        // Arrange
        User guestUser = new User("Jane Doe", "jane@doe.com", "787123456", "password", false);
        HttpRequest postRequest = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:7000/user/register"))
        .header("Content-Type", "application/json") // Ensure the server knows to expect JSON
        .POST(HttpRequest.BodyPublishers.ofString("{" +
            "\"name\":\"" + guestUser.getName() + "\", " +
            "\"email\": \"" + guestUser.getEmail() + "\", " +
            "\"phone\": \"" + guestUser.getPhone() + "\", " +
            "\"password\": \"" + guestUser.getPassword() + "\", " +
            "\"isAdmin\": " + guestUser.isAdmin() + 
        "}"))
        .build();
        HttpResponse<String> registerResponse = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("response body: " + registerResponse.body().toString());
        User createdUser = new User();
        //Check if the response status code is 200
        if(registerResponse.statusCode() >= 200 && registerResponse.statusCode() < 300){
            // ok to parse the response body
            createdUser = objectMapper.readValue(registerResponse.body(), User.class);
        } else{
            // its an error
            // lets see what the error is
            System.out.println("Error: " + registerResponse.statusCode());
            throw new RuntimeException("Error: " + registerResponse.statusCode());
        }
        // Assert
        assertEquals(registerResponse.body(), objectMapper.writeValueAsString(createdUser));
        assertEquals(200, registerResponse.statusCode());
        assertEquals(guestUser.getName(), createdUser.getName());
        assertEquals(guestUser.getEmail(), createdUser.getEmail());
        assertEquals(guestUser.getPhone(), createdUser.getPhone());
        assertEquals(guestUser.getPassword(), createdUser.getPassword());
        assertEquals(guestUser.isAdmin(), createdUser.isAdmin());
    }

    @Test
    void guest_can_login_and_create_a_new_bank_account_and_get_back_bank_account_json() throws IOException, InterruptedException {
        // As a user, I should be able to create a new Bank Account on the endpoint POST /login. 
        // Arrange
        //register a new user
        String name = "Jane Doe";
        String email = "Jane@dow.com";
        String phone = "4568790546";
        String password = "password";
        User guestUser = new User(name, email, phone, password, false);
        User registeredUser = registerAsUser(guestUser);
        //login
        User loggedInUser = loginAsUser(registeredUser);
        
        //create a new bank account
        //user cookie 'Auth' = userEmail to authenticate
        String accountType = "checking";
        BankAccount createdAccount = createBankAccount(loggedInUser, accountType);
        
        // Assert
        //verify bank account details
            //Integer userId;
            // String accountType;
            // BigDecimal balance;
            // String accountNumber;
        assertEquals(loggedInUser.getUserId(), createdAccount.getUserId());
        assertEquals("checking", createdAccount.getAccountType());
        assertEquals(new BigDecimal("0"), createdAccount.getBalance());
        assertEquals(loggedInUser.getEmail(), createdAccount.getEmail());
        assertEquals(10, createdAccount.getAccountNumber().length());
        //verify account has list of transactions
        //verify account has list of authorized users
        assertEquals(0, createdAccount.getTransactions().size());
        assertEquals(0, createdAccount.getAuthorizedUserEmails().size());
    }
        @Test
        // As a user, I should be able to deposit positive dollar amounts into a bank account, increasing the balance
        void logged_in_user_can_deposit_money_into_the_account_and_get_back_bank_account_json() throws IOException, InterruptedException {
           //registered user details
           String name = "Rich Jane";
           String email = "rich@jane.com";
           String phone = "7776651234";
           String password = "prettypowerpass";
           User guestUser = new User(name, email, phone, password, false);
           User registeredUser = registerAsUser(guestUser);
           //login as user
           User loggedInUser = loginAsUser(registeredUser);

           //create a bank account
            BankAccount createdAccount = createBankAccount(loggedInUser, "checking");
           //deposit some money
           // verify the balance is updated
              // verify the transaction was added to the list of transactions
                // verify the transaction details

            //deposit some money using the account number on url path: account/deposit/{accountNumber}
            BigDecimal amount = new BigDecimal("1500");
            BankAccount accountAfterDeposit = depositIntoAccount(createdAccount.getAccountNumber(), loggedInUser.getEmail(), amount);
            
             
            assertEquals("checking", accountAfterDeposit.getAccountType());
            assertEquals("rich@jane.com", accountAfterDeposit.getEmail());
            assertEquals(new BigDecimal("1500.00"), accountAfterDeposit.getBalance());
            assertEquals(10, accountAfterDeposit.getAccountNumber().length());
            assertEquals(1, accountAfterDeposit.getTransactions().size());

            //verify transaction details
            assertEquals(1, accountAfterDeposit.getTransactions().size());
            assertEquals(createdAccount.getAccountNumber(), accountAfterDeposit.getTransactions().get(0).getAccountNumber());
            assertEquals("deposit", accountAfterDeposit.getTransactions().get(0).getTransactionType());
            assertEquals(new BigDecimal("1500"), accountAfterDeposit.getTransactions().get(0).getAmount());
            assertEquals(10, accountAfterDeposit.getTransactions().get(0).getFromAccountNumber().length());
            assertEquals(10, accountAfterDeposit.getTransactions().get(0).getAccountNumber().length());
            assertEquals(new BigDecimal("1500"), accountAfterDeposit.getTransactions().get(0).getAmount());
            assertEquals(10, accountAfterDeposit.getTransactions().get(0).getToAccountNumber().length());
            //verfify the  tx toNumber
            assertEquals(createdAccount.getAccountNumber(), accountAfterDeposit.getTransactions().get(0).getToAccountNumber());
            //assert tx date is from today. ignore time
            assertEquals(LocalDate.now(), accountAfterDeposit.getTransactions().get(0).getTransactionDateTime().toLocalDateTime().toLocalDate());
            
            
            //verify user cant delete account with non zero balance
            testUserCantDeleteAccountWithNonZeroBalance(accountAfterDeposit, loggedInUser);
           
           
            // test that user can Withdraw positive dollar amounts from a bank account, reducing the balance
            testUserCanWithdrawMoneyFromTheAccountAndBalanceIsReduced(accountAfterDeposit, loggedInUser);

    }

    private void testUserCanWithdrawMoneyFromTheAccountAndBalanceIsReduced(BankAccount accountAfterDeposit,
                User loggedInUser) throws JsonMappingException, JsonProcessingException {
            //withdraw some money
            //verify the balance is updated
            //verify the account balance is reduced

            //withdraw some money using the account number on url path: account/withdraw/{accountNumber}
            BigDecimal withdrawalAmount = new BigDecimal("500.00");
            HttpRequest withdrawRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:7000/account/withdraw/" + accountAfterDeposit.getAccountNumber()))
                .header("Content-Type", "application/json") // Ensure the server knows to expect JSON
                .header("Cookie", "Auth=" + loggedInUser.getEmail()) // Add the Auth cookie with userEmail
                .POST(HttpRequest.BodyPublishers.ofString("{" +
                    "\"amount\": \""+ withdrawalAmount + "\", " + // Convert BigDecimal to String
                    "\"fromAccountNumber\": \"" + accountAfterDeposit.getAccountNumber() + "\"" + // Ensure accountNumber is treated as a string
                "}"))
                .build();
            HttpResponse<String> withdrawResponse = null;
            try {
                withdrawResponse = webClient.send(withdrawRequest, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("response body: " + withdrawResponse.body());
            BankAccount accountAfterWithdrawal;
            //Check if the response status code is 200
            if(withdrawResponse.statusCode() >= 200 && withdrawResponse.statusCode() < 300){
                // ok to parse the response body
                accountAfterWithdrawal = objectMapper.readValue(withdrawResponse.body(), BankAccount.class);
            } else{
                // its an error
                // lets see what the error is
                System.out.println("Error: " + withdrawResponse.statusCode());
                throw new RuntimeException("Error: " + withdrawResponse.statusCode());
            }
            assertEquals("checking", accountAfterWithdrawal.getAccountType());
            //verify balance is reduced by the amount withdrawn
            // assertEquals(new BigDecimal("1000.00"), accountAfterWithdrawal.getBalance());
            assertEquals(withdrawalAmount, accountAfterDeposit.getBalance().subtract(accountAfterWithdrawal.getBalance()));
            //try withdraawing a negative amount
            //verify the account is not deleted and a message is in the response body
            //response message is "Cannot withdraw negative amount"
            testUserCantWithdrawNegativeAmount(accountAfterWithdrawal, loggedInUser);
        }
    
    private void testUserCantWithdrawNegativeAmount(BankAccount accountAfterWithdrawal, User loggedInUser) {
        //try withdrawing a negative amount
        //verify the response message is "Cannot withdraw negative amount"
        BigDecimal negativeAmount = new BigDecimal("-500");
        HttpRequest withdrawRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:7000/account/withdraw/" + accountAfterWithdrawal.getAccountNumber()))
            .header("Content-Type", "application/json") // Ensure the server knows to expect JSON
            .header("Cookie", "Auth=" + loggedInUser.getEmail()) // Add the Auth cookie with userEmail
            .POST(HttpRequest.BodyPublishers.ofString("{" +
                "\"amount\": \""+ negativeAmount + "\", " + // Convert BigDecimal to String
                "\"fromAccountNumber\": \"" + accountAfterWithdrawal.getAccountNumber() + "\"" + // Ensure accountNumber is treated as a string
            "}"))
            .build();
        HttpResponse<String> withdrawResponse = null;
        try {
            withdrawResponse = webClient.send(withdrawRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("response body: " + withdrawResponse.body());
        //Assert
        assertEquals(400, withdrawResponse.statusCode());
        assertEquals("Cannot withdraw negative amount", withdrawResponse.body());
    }
        
    private void testUserCantDeleteAccountWithNonZeroBalance(BankAccount updatedAccount, User loggedInUser) {
        //try to delete the account
        //verify the account is not deleted
        //verify the account is still in the list of accounts
        //verify the account is still in the database
        
        HttpRequest deleteRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:7000/account/" + updatedAccount.getAccountNumber()))
            .header("Content-Type", "application/json") // Ensure the server knows to expect JSON
            .header("Cookie", "Auth=" + loggedInUser.getEmail()) // Add the Auth cookie with userEmail
            .DELETE()
            .build();   
        HttpResponse<String> deleteResponse = null;
        try {
            deleteResponse = webClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("response body: " + deleteResponse.body().toString());
        //Assert
        assertEquals(400, deleteResponse.statusCode());
        assertEquals("Cannot delete account with non-zero balance", deleteResponse.body());
    }

    @Test
    // As a user, I should be able to change my user information (name, phone, email, etc)
    void logged_in_user_can_change_user_information_and_get_back_user_object_json() throws IOException, InterruptedException {
        //registered user details
        // Arrange
        String name = "Bob Ross";
        String email = "bob@ross.com";
        String phone = "4347769981";
        String password = "boringpass";
        User guestUser = new User(name, email, phone, password, false);
        User registeredUser = registerAsUser(guestUser);
        //login as user
         //user details to update: name, phone, email, password 
        //update user details
        //verify the updated user details
        //such as name, phone, email, password
        //Act
        name = "JJ Rosseau";
        email = "jj@france.com";
        phone = "7656765432";
        password = "jesuisunhomme";
        User fancyUser = new User(name, email, phone, password, false);
        User loggedInUser = loginAsUser(registeredUser);
        HttpRequest updateRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:7000/user/update/" + loggedInUser.getUserId()))
            .header("Content-Type", "application/json") // Ensure the server knows to expect JSON
            .header("Cookie", "Auth=" + loggedInUser.getEmail()) // Add the Auth cookie with userEmail
            .PUT(HttpRequest.BodyPublishers.ofString("{" +
                "\"name\":\"" + fancyUser.getName() + "\", " +
                "\"email\": \"" + fancyUser.getEmail() + "\", " +
                "\"phone\": \"" + fancyUser.getPhone() + "\", " +
                "\"password\": \"" + fancyUser.getPassword() + "\", " +
                "\"isAdmin\": " + fancyUser.isAdmin() +
            "}"))
            .build();
        HttpResponse<String> updatedUserResponse = webClient.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("response body: " + updatedUserResponse.body().toString());
        User updatedUser = new User();
        //Check if the response status code is 200
        if(updatedUserResponse.statusCode() >= 200 && updatedUserResponse.statusCode() < 300){
            // ok to parse the response body
             updatedUser = objectMapper.readValue(updatedUserResponse.body(), User.class);
        } else{
            // its an error
            // lets see what the error is
            System.out.println("Error: " + updatedUserResponse.statusCode());
            // throw new RuntimeException("Error: " + updatedUserResponse.statusCode());
            }

        //Assert
        //verify the updated user details against original user details
        //such as name, phone, email, password
        assertEquals(200, updatedUserResponse.statusCode());
        assertEquals(fancyUser.getName(), updatedUser.getName());
        assertEquals(fancyUser.getPhone(), updatedUser.getPhone());
        assertEquals(fancyUser.getEmail(), updatedUser.getEmail());
        assertEquals(fancyUser.getPassword(), updatedUser.getPassword());
        assertEquals(false, updatedUser.isAdmin());
    }

    @Test
    // Delete a bank account which has a $0.00 balance
    void logged_in_user_can_delete_a_bank_account_with_a_zero_balance_and_get_back_a_message_json() throws IOException, InterruptedException {
        //registered user details
        // Arrange
        String name = "Angry Joe";
        String email = "hedeletes@gmail.com";
        String phone = "8887776665";
        String password = "destroyer242";
        User guestUser = new User(name, email, phone, password, false);
        User registeredUser = registerAsUser(guestUser);
        //login as user
        User loggedInUser = loginAsUser(registeredUser);
        //create a bank account
        BankAccount createdAccount = createBankAccount(loggedInUser, "checking");
        //balance is zero try deleting it
        //verify the account is deleted

        HttpRequest deleteRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:7000/account/" + createdAccount.getAccountNumber()))
            .header("Content-Type", "application/json") // Ensure the server knows to expect JSON
            .header("Cookie", "Auth=" + loggedInUser.getEmail()) // Add the Auth cookie with userEmail
            .DELETE()
            .build();   
        HttpResponse<String> deleteResponse = webClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("response body: " + deleteResponse.body().toString());
        //Assert
        assertEquals(200, deleteResponse.statusCode());
        assertEquals("Account deleted successfully", deleteResponse.body());
        //verify the account is deleted
        //verify the account is not in the list of accounts
        testListOfAccountsDoesNotContainDeletedAccount(loggedInUser, createdAccount);
    }

    private void testListOfAccountsDoesNotContainDeletedAccount(User loggedInUser, BankAccount createdAccount) {
        //get the list of accounts
        //verify the account is not in the list of accounts
        //verify the account is not in the database
        //verify the account is not in the list of accounts
        //verify the account is not in the database
        HttpRequest getAccountsRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:7000/account/" + createdAccount.getAccountNumber()))
            .header("Content-Type", "application/json") // Ensure the server knows to expect JSON
            .header("Cookie", "Auth=" + loggedInUser.getEmail()) // Add the Auth cookie with userEmail
            .GET()
            .build();   
        HttpResponse<String> getAccountsResponse = null;
        try {
            getAccountsResponse = webClient.send(getAccountsRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("response body: " + getAccountsResponse.body().toString());
        //Assert
        // assertEquals(200, getAccountsResponse.statusCode());
        //verify the account is not in the list of accounts
        //verify the account is not in the database
        assertEquals(false, getAccountsResponse.body().contains(createdAccount.getAccountNumber()));
    }
    
    @Test
    // Transfer money between bank accounts, reducing the balance of the source account and increasing the balance of the destination account    
    void logged_in_user_can_transfer_money_between_accounts_and_get_back_bank_account_json() throws IOException, InterruptedException {
        //registered user details
        String name = "Emon Lusk";
        String email = "moneyman@world.com";
        String phone = "9232398763";
        String password = "passwXAEord";
        User guestUser = new User(name, email, phone, password, false);
        User registeredUser = registerAsUser(guestUser);
        //login as user
        User loggedInUser = loginAsUser(registeredUser);

        //create 2 bank accounts
        //deposit some money into the source account
        //transfer money to the second account
        
        //create a bank account
         BankAccount senderAccount = createBankAccount(loggedInUser, "checking");
        BankAccount receiverAccount = createBankAccount(loggedInUser, "savings");
        //deposit some money
        depositIntoAccount(senderAccount.getAccountNumber(), loggedInUser.getEmail(), new BigDecimal("1500"));
        //transfer some money
        //verify the source account balance is reduced
        //verify the destination account balance is increased
        
        //transfer some money using the account number on url path: account/transfer/{accountNumber}
        BigDecimal transferAmount = new BigDecimal("500.00");
        HttpRequest transferRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:7000/account/transfer/" + senderAccount.getAccountNumber()))
            .header("Content-Type", "application/json") // Ensure the server knows to expect JSON
            .header("Cookie", "Auth=" + loggedInUser.getEmail()) // Add the Auth cookie with userEmail
            .POST(HttpRequest.BodyPublishers.ofString("{" +
                "\"amount\": \""+ transferAmount.toString() + "\", " + // Convert BigDecimal to String
                "\"fromAccountNumber\": \"" + senderAccount.getAccountNumber() + "\", " + // Ensure accountNumber is treated as a string
                "\"toAccountNumber\": \"" + receiverAccount.getAccountNumber() + "\"" + // Ensure accountNumber is treated as a string
            "}"))
            .build();
        HttpResponse<String> senderTransferResponse = webClient.send(transferRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("response body: " + senderTransferResponse.body().toString());
        BankAccount senderAccountAfterTransfer = null;
        //Check if the response status code is 200
        if(senderTransferResponse.statusCode() >= 200 && senderTransferResponse.statusCode() < 300){
            // ok to parse the response body
            senderAccountAfterTransfer = objectMapper.readValue(senderTransferResponse.body(), BankAccount.class);
        } else{
            // its an error
            // lets see what the error is
            System.out.println("Error: " + senderTransferResponse.statusCode());
            throw new RuntimeException("Error: " + senderTransferResponse.statusCode());
        }
        //Assert
        //verify balance is reduced by the amount transferred
        assertEquals(new BigDecimal("1000.00"), senderAccountAfterTransfer.getBalance());
        //verify the destination account balance is increased
        testGetAccountBalanceIsIncreased(receiverAccount, loggedInUser, transferAmount);
    }


    private void testGetAccountBalanceIsIncreased(BankAccount receiverAccount, User loggedInUser,
            BigDecimal transferAmount) throws JsonMappingException, JsonProcessingException {
        
        //get the account details
        //verify the account balance is increased
        HttpRequest getAccountRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:7000/account/" + receiverAccount.getAccountNumber()))
            .header("Content-Type", "application/json") // Ensure the server knows to expect JSON
            .header("Cookie", "Auth=" + loggedInUser.getEmail()) // Add the Auth cookie with userEmail
            .GET()
            .build();
        HttpResponse<String> getAccountResponse = null;
        try {
            getAccountResponse = webClient.send(getAccountRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("response body: " + getAccountResponse.body().toString());
        BankAccount receiverAccountAfterTransfer = null;
        //Check if the response status code is 200
        if(getAccountResponse.statusCode() >= 200 && getAccountResponse.statusCode() < 300){
            // ok to parse the response body
            receiverAccountAfterTransfer = objectMapper.readValue(getAccountResponse.body(), BankAccount.class);
        } else{
            // its an error
            // lets see what the error is
            System.out.println("Error: " + getAccountResponse.statusCode());
            throw new RuntimeException("Error: " + getAccountResponse.statusCode());
        }
        //Assert
        //verify the account balance is increased
        assertEquals(transferAmount, receiverAccountAfterTransfer.getBalance());
    }
    @Test
    void admin_can_view_all_users_accounts_and_transactions_and_get_back_list_json() throws IOException, InterruptedException {
        // As an admin, I should be able to view all users on the endpoint GET /admin/users. 
        // Arrange
        //register a new user
        String name = "Admin";
        String email = "admin@localhost.com";
        String phone = "1234567890";
        String password = "adminpassword";
        User adminUser;
        adminUser = new User(name, email, phone, password, true);
        User registeredAdminUser = registerAsUser(adminUser);
        //login as Admin user
        User loggedInAdminUser = loginAsUser(registeredAdminUser);

          //create a bank account
          BankAccount adminAccount = createBankAccount(loggedInAdminUser, "checking");
          //deposit some money
          depositIntoAccount(adminAccount.getAccountNumber(), loggedInAdminUser.getEmail(), new BigDecimal("1500"));

        //get all users
        //verify the list of users
        //Assert
        List<User> allUsers = fetchAllUsers(loggedInAdminUser);
        // list of users should be greater than 0
        assertTrue(allUsers.size() > 0);

        List<BankAccount> allBankAccounts = fetchAllBankAccounts(loggedInAdminUser);
        assertTrue(allBankAccounts.size() > 0);
        
        List<Transaction> allTransactions = fetchAllTransactions(loggedInAdminUser);
        assertTrue(allTransactions.size() > 0);
    }
    
    private List<Transaction> fetchAllTransactions(User loggedInAdmin) throws IOException, InterruptedException {
        HttpRequest getTransactionsRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:7000/admin/transactions"))
            .header("Content-Type", "application/json") // Ensure the server knows to expect JSON
            .header("Cookie", "Auth=" + loggedInAdmin.getEmail()) // Add the Auth cookie with userEmail
            .GET()
            .build();
        HttpResponse<String> getTransactionsResponse = webClient.send(getTransactionsRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("response body: " + getTransactionsResponse.body().toString());
        List<Transaction> transactions = objectMapper.readValue(getTransactionsResponse.body(), new TypeReference<List<Transaction>>(){});
        return transactions;
    }
    private List<BankAccount> fetchAllBankAccounts(User loggedInAdmin) throws IOException, InterruptedException {
       HttpRequest getAccountsRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:7000/admin/accounts"))
            .header("Content-Type", "application/json") // Ensure the server knows to expect JSON
            .header("Cookie", "Auth=" + loggedInAdmin.getEmail()) // Add the Auth cookie with userEmail
            .GET()
            .build();
        HttpResponse<String> getAccountsResponse = webClient.send(getAccountsRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("response body: " + getAccountsResponse.body().toString());
        List<BankAccount> accounts = objectMapper.readValue(getAccountsResponse.body(), new TypeReference<List<BankAccount>>(){});
        return accounts;
    }
    private List<User> fetchAllUsers(User loggedInAdmin) throws IOException, InterruptedException {
      HttpRequest getUsersRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:7000/admin/users"))
            .header("Content-Type", "application/json") // Ensure the server knows to expect JSON
            .header("Cookie", "Auth=" + loggedInAdmin.getEmail()) // Add the Auth cookie with userEmail
            .GET()
            .build();
        HttpResponse<String> getUsersResponse = webClient.send(getUsersRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("response body: " + getUsersResponse.body().toString());
        List<User> users = objectMapper.readValue(getUsersResponse.body(), new TypeReference<List<User>>(){});
        return users;
    }
    // -=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    // -=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    // -=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    // -=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    // -=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    // -=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    // -=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    // -=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    // -=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    // -=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    // -=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    // -=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    // -=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    // -=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-PRIVATE METHODS--==--=-=-==--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    private User loginAsUser(User registeredUser) throws IOException, InterruptedException{
        HttpRequest loginPostRequest = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:7000/user/login"))
        .header("Content-Type", "application/json") // Ensure the server knows to expect JSON
        .POST(HttpRequest.BodyPublishers.ofString("{" +
            "\"email\": \"" + registeredUser.getEmail() + "\", " +
            "\"password\": \"" + registeredUser.getPassword() + "\"" +
        "}"))
        .build();
        HttpResponse<String> loginResponse = webClient.send(loginPostRequest, HttpResponse.BodyHandlers.ofString());
        User loggedInUser = new User();
        //Check if the response status code is 200
        if(loginResponse.statusCode() >= 200 && loginResponse.statusCode() < 300){
            // ok to parse the response body
            loggedInUser = objectMapper.readValue(loginResponse.body(), User.class);
        } else{
            // its an error
            // lets see what the error is
            System.out.println("Error: " + loginResponse.statusCode());
            throw new RuntimeException("Error: " + loginResponse.statusCode() + loginResponse.body().toString());
        }
        return loggedInUser;
    }

    private User registerAsUser(User guestUser) throws IOException, InterruptedException{
        HttpRequest postRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:7000/user/register"))
            .header("Content-Type", "application/json") // Ensure the server knows to expect JSON
            .POST(HttpRequest.BodyPublishers.ofString("{" +
                "\"name\":\"" + guestUser.getName() + "\", " +
                "\"email\": \"" + guestUser.getEmail() + "\", " +
                "\"phone\": \"" + guestUser.getPhone() + "\", " +
                "\"password\": \"" + guestUser.getPassword() + "\", " +
                "\"isAdmin\": " + guestUser.isAdmin() + 
            "}"))
            .build();
        HttpResponse<String> registrationResponse = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("response body: " + registrationResponse.body().toString());
        User registeredUser = new User();
        //Check if the response status code is 200
        if(registrationResponse.statusCode() >= 200 && registrationResponse.statusCode() < 300){
            // ok to parse the response body
            registeredUser = objectMapper.readValue(registrationResponse.body(), User.class);
        } else{
            // its an error
            // lets see what the error is
            System.out.println("Error: " + registrationResponse.statusCode());
            throw new RuntimeException("Error: " + registrationResponse.statusCode());
        }
        return registeredUser;
    }

    private BankAccount createBankAccount(User loggedInUser, String accountType) throws IOException, InterruptedException{
        HttpRequest createBankAccountPostRequest = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:7000/account/create"))
        .header("Content-Type", "application/json") // Ensure the server knows to expect JSON
        .header("Cookie", "Auth=" + loggedInUser.getEmail()) // Add the Auth cookie with userEmail
        .POST(HttpRequest.BodyPublishers.ofString("{" +
            "\"userEmail\":" + loggedInUser.getEmail() + ", " +
            "\"accountType\": \"" + accountType + "\"" +
        "}"))
        .build();
        
        // // Act
        HttpResponse<String> createBankAccountResponse = webClient.send(createBankAccountPostRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("response body: " + createBankAccountResponse.body().toString());
        
        BankAccount createdAccount = null;
        //Check if the response status code is 200
        if(createBankAccountResponse.statusCode() >= 200 && createBankAccountResponse.statusCode() < 300){
            // ok to parse the response body
            createdAccount = objectMapper.readValue(createBankAccountResponse.body(), BankAccount.class);
        } else{
            // its an error
            // lets see what the error is
            System.out.println("Error: " + createBankAccountResponse.statusCode());
            throw new RuntimeException("Error: " + createBankAccountResponse.statusCode());
        }
        assertEquals(createBankAccountResponse.body(), objectMapper.writeValueAsString(createdAccount));
        assertEquals(200, createBankAccountResponse.statusCode());
        return createdAccount;
    }
    private BankAccount depositIntoAccount(String accountNumber, String emailAuthCookie, BigDecimal amount) throws IOException, InterruptedException {
        System.out.println(amount.toString());
        HttpRequest depositRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:7000/account/deposit/" + accountNumber))
            .header("Content-Type", "application/json") // Ensure the server knows to expect JSON
            .header("Cookie", "Auth=" + emailAuthCookie) // Add the Auth cookie with userEmail
            .POST(HttpRequest.BodyPublishers.ofString("{" +
                "\"amount\": \""+ amount.toString() + "\", " + // Convert BigDecimal to String
                "\"fromAccountNumber\": \"" + accountNumber + "\"" + // Ensure accountNumber is treated as a string
            "}"))
            .build();
            HttpResponse<String> depositResponse = webClient.send(depositRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("response body: " + depositResponse.body().toString());
            BankAccount updatedAccount = null;
            //Check if the response status code is 200
            if(depositResponse.statusCode() >= 200 && depositResponse.statusCode() < 300){
                // ok to parse the response body in parts
                //extract account part
                //extract list of transactions part
                // updatedAccount = objectMapper.readValue(depositResponse.body(), BankAccount.class);
                JSONObject jsonResponse = new JSONObject(depositResponse.body());
                int userId = jsonResponse.getInt("userId");
                String accountType = jsonResponse.getString("accountType");
                String email = jsonResponse.getString("email");
                BigDecimal balance = new BigDecimal(jsonResponse.getBigDecimal("balance").toString());
                String acctNumber = jsonResponse.getString("accountNumber");
                List<Transaction> extractedTransactions = objectMapper.readValue(jsonResponse.getJSONArray("transactions").toString(), new TypeReference<List<Transaction>>(){});
                updatedAccount = new BankAccount(userId, accountType, email, balance, acctNumber, extractedTransactions);
      
            } else{
                // its an error
                // lets see what the error is
                System.out.println("Error: " + depositResponse.statusCode());
                throw new RuntimeException("Error: " + depositResponse.statusCode());
            }
            return updatedAccount;
    }
}
