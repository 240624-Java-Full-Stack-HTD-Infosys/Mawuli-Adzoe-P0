package net.revature.labs.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import net.revature.labs.model.BankAccount;
import net.revature.labs.model.User;
import net.revature.labs.service.AccountService;
import net.revature.labs.service.UserService;

import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

public class BankingAPIController{
    // In this setup, the app object is fully managed within the BankingAPIController class. 
    // The stopAPI() method ensures the server is properly stopped after all tests have run.
    //  This approach maintains encapsulation and prevents multiple starts of the app object.
    private static Javalin app;
    private AccountService accountService;
    private UserService userService;
    private ObjectMapper objectMapper = new ObjectMapper();

    public BankingAPIController() throws ClassNotFoundException, SQLException, IOException {
        this.accountService = new AccountService();
        this.userService = new UserService();
        if (app == null) {
            startAPI();  // Start the API only if it hasn't been started yet
        }
    }

    private void startAPI() {
        app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.allowHost("http://localhost:3000"); // Allow requests from localhost:3000
                    it.allowCredentials = true; // Allow credentials
                });
            });
        }).start(7000);
        
        registerRoutes();
    }

    private void registerRoutes() {
        // Define all the routes and their handlers here
        app.post("/user/register", this::registerAccountHandler);
        app.post("/account/create", this::createBankAccountHandler);
        app.post("/user/login", this::loginHandler);
        app.post("/account/deposit/{accountNumber}", this::depositHandler);
        app.put("/user/update/{user_id}", this::updateUserHandler);
        app.delete("/account/{account_number}", this::deleteAccountHandler);
        app.get("/account/{account_number}", this::getAccountHandler);
        app.post("/account/withdraw/{account_number}", this::withdrawHandler);
        app.post("/account/transfer/{account_number}", this::transferHandler);
        app.get("/admin/users", this::getAllUsersForAdminHandler);
        app.get("/admin/accounts", this::getAllAccountsForAdminHandler);
        app.get("/admin/transactions", this::getAllTransactionsForAdminHandler);
    }

    public static void stopAPI() {
        if (app != null) {
            app.stop();
            app = null;
        }
    }

    
 
       











    //-======================================-==============Handlers=======================-=====================================
    //-=====================================-=====================================-=====================================
    public void registerAccountHandler(Context ctx) throws SQLException, ClassNotFoundException, IOException {
        logRequest(ctx);

        //register new user
       // return user object
        ObjectMapper om = new ObjectMapper();
        User user = om.readValue(ctx.body(), User.class);

        //register the new user
        User registeredUser = userService.registerUser(user);
        //return the registered user as json
        logResponse(ctx, registeredUser.toString());
        ctx.json(registeredUser);
    }

    public void loginHandler(Context ctx) throws ClassNotFoundException, SQLException, IOException {
        logRequest(ctx);
        ObjectMapper om = new ObjectMapper();
        //get the user object from the request
        //use the email and password to login
        //return a cookie with the user object
        User user = om.readValue(ctx.body(), User.class);
        User foundUser = userService.loginUser(user.getEmail(), user.getPassword());

        if(foundUser == null){
            ctx.result("User not found. Please register or check your credentials.");
            ctx.status(401);
        } else {
            //reply with cookie
            ctx.cookie("Auth", om.writeValueAsString(user.getEmail()));
            ctx.status(200);
            ctx.result(om.writeValueAsString(foundUser));
            logResponse(ctx, foundUser.toString());
        }
    }

    public void createBankAccountHandler(Context ctx) throws JsonMappingException, JsonProcessingException, SQLException{
        //validate user by cookie 'Auth' which is their email address.
        //accept email and account type for creating accounts
        JSONObject json = new JSONObject(ctx.body());
        String accountType = json.getString("accountType");
        String userEmailCookie = ctx.cookie("Auth");
        Boolean validated = userService.validateCookie(userEmailCookie);
        ObjectMapper om = new ObjectMapper();
        logRequest(ctx);
        if(validated){
            ctx.status(200);
            BankAccount createdBankAccount = accountService.createBankAccount(userEmailCookie, accountType);
            logResponse(ctx, createdBankAccount.toString());
            ctx.json(om.writeValueAsString(createdBankAccount));
        }
        // BankAccount addedAccount = accountService.createAccount(account);
        // if(addedAccount == null){
        //     ctx.status(400);
        // } else{
        //     ctx.json(om.writeValueAsString(addedAccount));
        // }
    }
    
    public void updateUserHandler(Context ctx) throws JsonMappingException, JsonProcessingException, SQLException {
        String userEmailCookie = ctx.cookie("Auth");
        Boolean validated = userService.validateCookie(userEmailCookie);
        ObjectMapper om = new ObjectMapper();
        logRequest(ctx);

        if (validated) {
            int ownerUserId = Integer.parseInt(ctx.pathParam("user_id"));
            User user = om.readValue(ctx.body(), User.class);
            User authenticatedUser = userService.getUserByEmail(userEmailCookie);
    
            // Ensure only admins can update the isAdmin field
            User existingUser = userService.getUserById(ownerUserId);
            if (!authenticatedUser.isAdmin() && user.isAdmin() != existingUser.isAdmin()) {
                ctx.status(403).result("Only admins can update the admin status.");
                return;
            }
    
            // Allow users to update their own details, or admins to update any user
            if (authenticatedUser.isAdmin() || authenticatedUser.getUserId() == ownerUserId) {
                User updatedUser = userService.updateUserById(ownerUserId, user);
                logResponse(ctx, updatedUser.toString());
                ctx.status(200).json(updatedUser);
            } else {
                logResponse(ctx, "Unauthorized to update user details.");
                ctx.status(403).result("Unauthorized to update user details.");
            }
        } else {
            logResponse(ctx, "Unauthorized access");
            ctx.status(401).result("Unauthorized access");
        }
    }

    
    public void depositHandler(Context ctx) throws JsonMappingException, JsonProcessingException, SQLException{
        //validate user by cookie 'Auth' which is their email address.
        //accept account number and amount to deposit
        logRequest(ctx);
        JSONObject json = new JSONObject(ctx.body());
        String accountNumber = ctx.pathParam("accountNumber");
        BigDecimal amount = json.getBigDecimal("amount");
        String userEmailCookie = ctx.cookie("Auth");
        Boolean validated = userService.validateCookie(userEmailCookie);
        ObjectMapper om = new ObjectMapper();
        if(validated){
            BankAccount bankAccount = accountService.getAccount(accountNumber);
            accountService.deposit(bankAccount, amount);
            BankAccount updatedBankAccount = accountService.getAccount(accountNumber);
            logResponse(ctx, updatedBankAccount.toString());
            ctx.json(om.writeValueAsString(updatedBankAccount));
            ctx.status(200);
        }
    }
    
    public void deleteAccountHandler(Context ctx) throws SQLException {
        String accountNumber = ctx.pathParam("account_number");
        String userEmailCookie = ctx.cookie("Auth");
        logRequest(ctx);
        
        if (userService.validateCookie(userEmailCookie)) {
            User user = userService.getUserByEmail(userEmailCookie);
            BankAccount bankAccount = accountService.getAccount(accountNumber);
            if (user.isAdmin()){
                accountService.deleteAccountForAdmin(accountNumber, user);
                logResponse(ctx, "Account deleted succesfully");
                ctx.status(200).result("Account deleted successfully");
            }else if ( bankAccount.getBalance().compareTo(BigDecimal.ZERO) == 0) {
                accountService.deleteAccount(accountNumber);
                logResponse(ctx, "Account deleted succesfully");
                ctx.status(200).result("Account deleted successfully");
            } else {
                logResponse(ctx, "Cannot delete account with a non-zero balance");
                ctx.status(400).result("Cannot delete account with non-zero balance");
            }
        } else {
            logResponse(ctx, "Unauthorized access");
            ctx.status(401).result("Unauthorized access");
        }
    }
    
    public void getAccountHandler(Context ctx) throws JsonProcessingException{
        //validate user by cookie 'Auth' which is their email address.
        //accept account number to get account details
        logRequest(ctx);
        String accountNumber = ctx.pathParam("account_number");
        String userEmailCookie = ctx.cookie("Auth");
        Boolean validated = userService.validateCookie(userEmailCookie);
        ObjectMapper om = new ObjectMapper();
        if(validated){
            BankAccount bankAccount = accountService.getAccount(accountNumber);
            logResponse(ctx, bankAccount.toString());
            ctx.json(om.writeValueAsString(bankAccount));
            ctx.status(200);
        }
    }
    public void withdrawHandler(Context ctx) throws JsonProcessingException{
        //validate user by cookie 'Auth' which is their email address.
        //accept account number and amount to withdraw
        logRequest(ctx);
        JSONObject json = new JSONObject(ctx.body());
        String accountNumber = ctx.pathParam("account_number");
        BigDecimal amount = json.getBigDecimal("amount");
        //no negative amounts allowed
        if(amount.compareTo(BigDecimal.ZERO) < 0){
            ctx.status(400);
            ctx.result("Cannot withdraw negative amount");
            return;
        }
        String userEmailCookie = ctx.cookie("Auth");
        Boolean validated = userService.validateCookie(userEmailCookie);
        ObjectMapper om = new ObjectMapper();
        if(validated){
            BankAccount foundBankAccount = accountService.getAccount(accountNumber);
            accountService.withdraw(foundBankAccount, amount);
            BankAccount updatedBankAccount = accountService.getAccount(accountNumber);
            logResponse(ctx, updatedBankAccount.toString());
            ctx.json(om.writeValueAsString(updatedBankAccount));
            ctx.status(200);
        }
    }
    /**
     * @param ctx
     * @throws NumberFormatException
     * @throws SQLException
     * @throws JsonProcessingException
     */
    public void transferHandler(Context ctx) throws NumberFormatException, SQLException, JsonProcessingException{
        //validate user by cookie 'Auth' which is their email address.
        //accept account number and amount to transfer
        //accept destination account number
        //check if destination account exists
        //check if source account has enough balance
        //withdraw from source account
        //deposit to destination account
        //return updated source account
        //return updated destination account
        //return error if any of the checks fail
        logRequest(ctx);
        JSONObject json = new JSONObject(ctx.body());
        String accountNumber = ctx.pathParam("account_number");
        BigDecimal amount = json.getBigDecimal("amount");
        String destinationAccountNumber = json.getString("toAccountNumber");
        // String originAccountNumber = json.getString("fromAccountNumber");
        String userEmailCookie = ctx.cookie("Auth");
        Boolean validated = userService.validateCookie(userEmailCookie);
        ObjectMapper om = new ObjectMapper();
        if(validated){
            BankAccount foundBankAccount = accountService.getAccount(accountNumber);
            BankAccount destinationBankAccount = accountService.getAccount(destinationAccountNumber);
            if(destinationBankAccount == null){
                logResponse(ctx, "Destination account does not exist");
                ctx.status(400);
                ctx.result("Destination account does not exist");
                return;
            }
            if(foundBankAccount.getBalance().compareTo(amount) < 0){
                logResponse(ctx, "Insufficient funds");
                ctx.status(400);
                ctx.result("Insufficient funds");
                return;
            }
            accountService.withdraw(foundBankAccount, amount);
            accountService.deposit(destinationBankAccount, amount);
            BankAccount senderBankAccount = accountService.getAccount(accountNumber);
            logResponse(ctx, senderBankAccount.toString());
            ctx.json(om.writeValueAsString(senderBankAccount));
            ctx.status(200);
        }
    }
    
    
    public void getAllUsersForAdminHandler(Context ctx){
        logRequest(ctx);  // Log incoming request
        String adminCookie = ctx.cookie("Auth");
        Boolean validated = userService.validateCookie(adminCookie);
        if(validated){
            try {
                String jsonResponse = objectMapper.writeValueAsString(userService.getAllUsers());
                ctx.result(jsonResponse);
                ctx.status(200);
                logResponse(ctx, jsonResponse);  // Log outgoing response
            } catch (Exception e) {
                ctx.status(500);
                ctx.result("Error processing request");
                logResponse(ctx, "Error processing request");
            }
            return;
        }
        ctx.status(401);
        ctx.result("Unauthorized access");
        logResponse(ctx, "Unauthorized access");
    }

    public void getAllAccountsForAdminHandler(Context ctx){
        logRequest(ctx);
        String adminCookie = ctx.cookie("Auth");
        Boolean validated = userService.validateCookie(adminCookie);
        if(validated){
            try {
                String jsonResponse = objectMapper.writeValueAsString(accountService.getAllAccounts());
                ctx.result(jsonResponse);
                ctx.status(200);
                logResponse(ctx, jsonResponse);
            } catch (Exception e) {
                ctx.status(500);
                ctx.result("Error processing request");
                logResponse(ctx, "Error processing request");
            }
        } else {
            ctx.status(401);
            ctx.result("Unauthorized access");
            logResponse(ctx, "Unauthorized access");
        }
    }

    public void getAllTransactionsForAdminHandler(Context ctx){
        logRequest(ctx);
        String adminCookie = ctx.cookie("Auth");
        Boolean validated = userService.validateCookie(adminCookie);
        if(validated){
            try {
                String jsonResponse = objectMapper.writeValueAsString(accountService.getAllTransactions());
                ctx.result(jsonResponse);
                ctx.status(200);
                logResponse(ctx, jsonResponse);
            } catch (Exception e) {
                ctx.status(500);
                ctx.result("Error processing request");
                logResponse(ctx, "Error processing request");
            }
        } else {
            ctx.status(401);
            ctx.result("Unauthorized access");
            logResponse(ctx, "Unauthorized access");
        }
    }

    private void logRequest(Context ctx) {
        System.out.println("Request Received:--=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=" + //
                        "");
        System.out.println("Method: " + ctx.method());
        System.out.println("Path: " + ctx.path());
        System.out.println("Headers: " + ctx.headerMap());
        System.out.println("Body: " + ctx.body());
    }

    private void logResponse(Context ctx, String responseBody) {
        if (responseBody == null) {
            responseBody = "";
        }
        System.out.println("Response Sent:--=-=-=-=-=-==--=-=-=-=-==-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        System.out.println("Status: " + ctx.status());
        System.out.println("Body: " + responseBody);
    }
}