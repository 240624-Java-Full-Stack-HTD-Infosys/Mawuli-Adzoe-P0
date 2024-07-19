package net.revature.labs.dao;

import net.revature.labs.dao.util.DBUtil;
import net.revature.labs.model.BankAccount;
import net.revature.labs.model.User;
import util.TestUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;


public class BankAccountDAOTest {
    private static BankAccountDAO bankAccountDAO;
    private static UserDAOImpl userDAO;
    private static Connection connection;

    @BeforeAll
    public static void setup() throws SQLException, IOException, ClassNotFoundException {
        TestUtil.setEnvironmentToTest();
        DBUtil.resetTestDatabase();
        connection = DBUtil.getConnection();
        bankAccountDAO = new BankAccountDAO();
        userDAO = new UserDAOImpl();
    }

    @AfterAll
    public static void tearDown() throws SQLException, IOException {
        if (connection != null) {
            connection.close(); // Close and release all pooled connections
        }
        DBUtil.resetTestDatabase();
    }


    //Test CRUD operations on Account
    //Test createAccount
    //Test updateAccount
    //Test deleteAccount
    //Test getAccountsByEmail
    //Test getAccountByEmail
    @Test
    public void testCreateAccount() throws SQLException, ClassNotFoundException, IOException {
        //create an account for a user by email

        //Arrange
        String userEmail = "john890@doe.com";
        //Act
        //register a user by email
        userDAO.registerUser(new User("John Doe", userEmail, "8901234567", "password", false));
        
        //create a bank account and get back account details
        BankAccount createdBankAccount = bankAccountDAO.createBankAccount(userEmail, "checking");

        //Assert
        //check if account details match user email and account type
        //also check if account has a balance of 0, and account number is not null
        //returned account should also have a valid account number(16 digit number)
        // returned account should have a list of 0 transactions
        assertNotNull(createdBankAccount);
        assertEquals(userEmail, createdBankAccount.getEmail());
        assertEquals("checking", createdBankAccount.getAccountType());
        assertEquals(new BigDecimal(0), createdBankAccount.getBalance());
        assertNotNull(createdBankAccount.getAccountNumber());
        assertEquals (10, String.valueOf(createdBankAccount.getAccountNumber()).length());
        assertEquals(0, createdBankAccount.getTransactions().size());
    }   

    @Test
    public void testInvalidUserCreation() throws SQLException {
        //create an account for a user by email who has not registered.
        //Arrange
        //Act
        String unregisteredEmail = "unregistered@user.com";

        //Assert
        //check if account creation fails and throws an IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> bankAccountDAO.createBankAccount(unregisteredEmail, "checking"));

    }
    @Test void testInvalidGetAccountsByEmailThrowsException() throws SQLException, ClassNotFoundException, IOException {
        //Arrange
        String userEmail = "john@dow.com";
        //Act
        //Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> 
                bankAccountDAO.getAccountsByEmail(userEmail)
        );
        assertEquals(IllegalArgumentException.class, exception.getClass());
        assertEquals( "User not found", exception.getMessage());
    }

    @Test void testGetAccountsByEmail() throws SQLException, ClassNotFoundException, IOException {
        //Arrange
        String userEmail = "jjrandom@web.com";
        //Act
        //register a user by email
        userDAO.registerUser(new User("John Doe", userEmail, "4498723456", "password", false));
        //create a bank account and get back account details
        bankAccountDAO.createBankAccount(userEmail, "checking");
        //Account has been created and should be in the database
        //get all of this user's accounts. The recently created account should be in the list.
        List<BankAccount> bankAccountList = bankAccountDAO.getAccountsByEmail(userEmail);
        //Assert
        //check if account details match user email and account type
        //also check if account has a balance of 0, and account number is not null
        //returned account should also have a valid account number(16 digit number)
        // returned account should have a list of 0 transactions
        assertNotNull(bankAccountList);
        assertEquals(1, bankAccountList.size());
        assertNotNull(bankAccountList.get(0).getAccountNumber());
        assertEquals (10, String.valueOf(bankAccountList.get(0).getAccountNumber()).length());
        assertEquals(0, bankAccountList.get(0).getTransactions().size());
        assertEquals(0, bankAccountList.get(0).getBalance().intValue());
        assertEquals(userEmail, bankAccountList.get(0).getEmail());
        assertEquals("checking", bankAccountList.get(0).getAccountType());
        //assert on the list of authorized users for the first account in the list is 0
        assertEquals(0, bankAccountList.get(0).getAuthorizedUserEmails().size());

    }

    @Test void testInvalidShareBankAccountWithEmail() throws SQLException, ClassNotFoundException, IOException {
        //Arrange
        //register a user by email
        String email = "user2002@world.com";
        userDAO.registerUser(new User("Charlie Chap", email, "8967543321", "password", false));
        //create a bank account and get back account details
        BankAccount bankAccount = bankAccountDAO.createBankAccount(email, "savings");
        //Act
        assertThrows( Exception.class, () -> bankAccountDAO.shareBankAccountWithEmail(email, bankAccount));
    }
    @Test void testShareBankAccountWithEmail() throws Exception {
        //Arrange
        //register a user by email
        String email = "roman11@reina.com";
        userDAO.registerUser(new User("Roman Reina", email, "9175698765", "password", false));
        //create a bank account and get back account details
        BankAccount bankAccount = bankAccountDAO.createBankAccount(email,"savings");
        String friendEmail = "friend2001@gmail.com";
        //Act
        bankAccountDAO.shareBankAccountWithEmail(friendEmail, bankAccount);
        //sharing updates bank account with the email of the person shared with
        //check if the email is updated in the bank account
        bankAccount.addSharedUser(email);
        List<String> listOfEmails = bankAccount.getAuthorizedUserEmails();

        //Assert
        //check if the email is updated in the bank account
        //check if the email is in the list of emails the account is shared with
        assertTrue(listOfEmails.contains(email));

    }

    @Test void testDeleteAccountByEmail() throws SQLException {
        //Arrange
        //Act
        //Assert
        //register a user by email
        //create a bank account
        //delete the account
        //verify the account is deleted
    }

    @Test
    void testDeleteAccountWithNonZeroBalanceFails(){
        //Arrange
        //Act
        //Assert
        //register as a user
        //create a bank account
        //deposit some money into the account
        //try to delete the account
        //check if the account is not deleted
    }

     @Test
    void guest_can_register() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
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
            HttpClient webClient = HttpClient.newHttpClient();
        HttpResponse<String> registerResponse = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("response body: " + registerResponse.body().toString());
        User createdUser = new User();
        ObjectMapper objectMapper   = new ObjectMapper();
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
}
