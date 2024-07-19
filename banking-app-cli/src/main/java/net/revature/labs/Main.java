package net.revature.labs;

import net.revature.labs.controller.BankingAPIController;

import java.io.IOException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        try {
            BankingAPIController apiController = new BankingAPIController();
            // ApiServer.getInstance(); // This will start the server
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}