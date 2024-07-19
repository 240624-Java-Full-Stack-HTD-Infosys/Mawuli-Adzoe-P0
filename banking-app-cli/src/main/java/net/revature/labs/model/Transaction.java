package net.revature.labs.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Transaction {
    // - transactions are are only allowed to be created once(via constructor) and inserted once. 
    // No updates ever to transactions table. 
    // Transactions are immutable and for historical purposes. Thus all are final and have no setters.
    private Integer transactionId;
    private final String accountNumber;
    private final String transactionType;//deposit or withdrawal
    private final BigDecimal amount;
    private final Timestamp transactionDateTime;
    private final String fromAccountNumber;
    private final String toAccountNumber;


    // public Transaction(String accountNumber, String transactionType, BigDecimal amount, Timestamp transactionDateTime,
    //          String fromAccountNumber, String toAccountNumber) {
    //     this.accountNumber = accountNumber;
    //     this.transactionType = transactionType;
    //     this.amount = amount;
    //     this.transactionDateTime = transactionDateTime;
    //     this.fromAccountNumber = fromAccountNumber;
    //     this.toAccountNumber = toAccountNumber;
    // }
    
    @JsonCreator
    public Transaction(@JsonProperty("accountNumber") String accountNumber, 
                       @JsonProperty("transactionType") String transactionType, 
                       @JsonProperty("amount") BigDecimal amount, 
                       @JsonProperty("transactionDateTime") Timestamp transactionDateTime,
                       @JsonProperty("fromAccountNumber") String fromAccountNumber, 
                       @JsonProperty("toAccountNumber") String toAccountNumber) {
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionDateTime = transactionDateTime;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
    }


   
    
    public Transaction(String transactionId, String accountNumber, String transactionType, BigDecimal amount,
            Timestamp transactionDateTime, String fromAccountNumber,
            String toAccountNumber) {

        this.transactionId = Integer.parseInt(transactionId);
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionDateTime = transactionDateTime;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;

    }

    public String getTransactionType() {
        return this.transactionType;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public Timestamp getTransactionDateTime() {
        return transactionDateTime;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
