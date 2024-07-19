package net.revature.labs.model;


import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

// import net.revature.labs.model.BankAccount;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountTest {

    @Test
    public void testAccountInitialization() {
        //register user
        //create bank account with user
        User user = new User("John Doe", "email@web.com", "1112223456", "password", false);
        BankAccount account = new BankAccount(user, "Savings");

        assertEquals(user.getUserId(), account.getUserId());
        assertEquals("Savings", account.getAccountType());
        assertEquals(new BigDecimal("0"), account.getBalance());
        assertEquals(user.getEmail(), account.getEmail());
        assertEquals(10, account.getAccountNumber().length());
        //verify account has list of transactions
        //verify account has list of authorized users
        assertEquals(0, account.getTransactions().size());
        assertEquals(0, account.getAuthorizedUserEmails().size());
    }

    @Test
    public void testDeposit() {
        //create bank account with user
        //deposit some money
        User user = new User("John Doe", "johndoe@internet.com", "92223334567", "password", false);
        BankAccount account = new BankAccount(user, "Savings");
        String fromAccountNumber = "9997778881";
        account.deposit(new BigDecimal("1500.00"), fromAccountNumber);

        assertEquals(new BigDecimal("1500.00"), account.getBalance());
        //verify transaction was added to list of transactions
        //verify transaction details
        assertEquals(1, account.getTransactions().size());
        Transaction transaction = account.getTransactions().get(0);
        assertEquals(account.getAccountNumber(), transaction.getAccountNumber());
        assertEquals("Deposit", transaction.getTransactionType());
        assertEquals(new BigDecimal("1500.00"), transaction.getAmount());
        assertEquals(fromAccountNumber, transaction.getFromAccountNumber());
        //verfify the  tx toNumber
        assertEquals(account.getAccountNumber(), transaction.getToAccountNumber());
        //assert tx date  is from today
        assertEquals(LocalDate.now(), transaction.getTransactionDateTime());
    }

    @Test
    public void testWithdrawSufficientBalance() {
        User user1 = new User("Joe Bloh", "joe@bloh.com", "7178907634", "password", false);
 
        BankAccount account = new BankAccount(user1, "Savings");
        account.deposit(new BigDecimal("1000.00"), "9997778881");
        boolean success = account.withdraw(new BigDecimal("500.00"));

        assertTrue(success);
        assertEquals(new BigDecimal("500.00"), account.getBalance());
    }

    @Test
    public void testWithdrawInsufficientBalance() {
        User userBetty = new User("Betty White", "betty@world.co", "8189334512", "password", false);
        BankAccount account = new BankAccount(userBetty, "Savings");
        //created account should have a balance of 0
        // thus the withdrawal should fail.
        boolean success = account.withdraw(new BigDecimal("1500.00"));

        assertFalse(success);
        assertEquals(new BigDecimal("0"), account.getBalance());
    }
}
