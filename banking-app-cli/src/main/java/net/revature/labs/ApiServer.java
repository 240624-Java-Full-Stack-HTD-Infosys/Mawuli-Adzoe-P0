package net.revature.labs;

import java.io.IOException;
import java.sql.SQLException;

import io.javalin.Javalin;
import io.javalin.http.Context;
import net.revature.labs.service.AccountService;
import net.revature.labs.service.UserService;

public class ApiServer {
    private static Javalin instance;
    private static UserService userService = getUserService();
    private static AccountService accountService = getAccountService();

    public static AccountService getAccountService() {
        AccountService newAccountService = null;
        try {
            newAccountService = new AccountService();
            return newAccountService;
        } catch (ClassNotFoundException | SQLException | IOException e) {
            e.printStackTrace();
        }
        return newAccountService;
    }

    public static UserService getUserService() {
        UserService newUserService = null;
        try {
            newUserService = new UserService();
            return newUserService;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newUserService;
    }

    public static Javalin getInstance() {
        if (instance == null) {
            instance = Javalin.create(config -> {
                config.bundledPlugins.enableCors(cors -> {
                    cors.addRule(it -> {
                        it.allowHost("http://localhost:3000");
                        it.anyHost(); // Or use specific hosts as needed
                    });
                });
            }).start(0);

            int port = instance.port();
            System.out.println("Javalin Server started on random port: " + port);

            instance.get("admin/users", ApiServer::getAllUsersForAdminHandler);
            instance.get("admin/accounts", ApiServer::getAllAccountsForAdminHandler);
            instance.get("admin/transactions", ApiServer::getAllTransactionsForAdminHandler);
        }
        return instance;
    }

    private static void getAllUsersForAdminHandler(Context ctx){
        String adminCookie =ctx.cookie("Auth");
        //fetch all users for admin
        Boolean validated = userService.validateCookie(adminCookie);
        if(validated){
            ctx.json(userService.getAllUsers());
            ctx.status(200);
            return;
        }
        ctx.status(401);
    }
    
    private static void getAllAccountsForAdminHandler(Context ctx){
        String adminCookie =ctx.cookie("Auth");
        //fetch all accounts for admin
        Boolean validated = userService.validateCookie(adminCookie);
        if(validated){
            ctx.json(accountService.getAllAccounts());
            ctx.status(200);
        }
        ctx.status(401);

    }

    public static void getAllTransactionsForAdminHandler(Context ctx){
        //fetch all transactions for admin
        String adminCookie =ctx.cookie("Auth");
        Boolean validated = userService.validateCookie(adminCookie);
        if(validated){
            ctx.json(accountService.getAllTransactions());
            ctx.status(200);
        }
        ctx.status(401);
    }

}
