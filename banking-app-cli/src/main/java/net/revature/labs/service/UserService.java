package net.revature.labs.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import net.revature.labs.dao.UserDAOImpl;
import net.revature.labs.model.User;

public class UserService {
    private UserDAOImpl userDAOImpl;
    public UserService() throws ClassNotFoundException, SQLException, IOException  {
        this.userDAOImpl = new UserDAOImpl();
    }
    public User registerUser(User user) throws ClassNotFoundException, SQLException, IOException {
        User registeredUser = userDAOImpl.registerUser(user);
        return registeredUser;
    }
    public User loginUser(String email, String password) throws ClassNotFoundException, SQLException, IOException {
       return userDAOImpl.loginUser(email, password);
    }

    public Boolean validateCookie(String userEmailCookie) {
       String userEmail = userEmailCookie;
        return userDAOImpl.doesUserAccountExist(userEmail);
    }
    public User updateUser(User user) throws SQLException {
        return userDAOImpl.updateUserAndReturnUser(user);
    }
    public User updateUserById(int ownerUserId, User user) throws SQLException {
        return userDAOImpl.updateUserById(ownerUserId, user);        
    }
    public List<User> getAllUsers() {
        return userDAOImpl.getAllUsers();
    }
    public User getUserByEmail(String email) throws SQLException {
        return userDAOImpl.getUserByEmail(email);
    }
    public User getUserById(int ownerUserId) {
        return userDAOImpl.getUserById(ownerUserId);
    }
}
