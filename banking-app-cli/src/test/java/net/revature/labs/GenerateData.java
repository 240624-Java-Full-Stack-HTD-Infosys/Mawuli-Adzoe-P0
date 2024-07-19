package net.revature.labs;


import java.sql.SQLException;

import net.revature.labs.dao.util.DBUtil;

public class GenerateData {
    public static void main(String[] args) throws SQLException {
        // Faker faker = new Faker();
        // String name = faker.name().fullName().replaceAll("[^a-zA-Z -]", "");
        // String email = faker.internet().emailAddress();
        // String phone = faker.phoneNumber().cellPhone();
        // String password = faker.internet().password();
        // Boolean isAdmin = false;
        // User user = new User(name, email, phone, password, isAdmin);
        // System.out.println(user);
        //fetch all users
        //use their id to generate bank accounts
        //use bank account number to generate transactions
        DBUtil.fetchUsersUsesThemToGenerateBankAccountsAndTransactions();

    }
   

    
}

