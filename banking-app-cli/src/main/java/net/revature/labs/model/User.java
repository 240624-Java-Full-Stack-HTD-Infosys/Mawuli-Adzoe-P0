package net.revature.labs.model;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.Objects;
import org.mindrot.jbcrypt.BCrypt;

    public class User {

        private String name;
        private boolean isAdmin;
        private String email;
        private String phone;
    
        private String password;
        private int userId;
        private List<BankAccount> bankAccounts;
    
    public User() {
        this.bankAccounts= new ArrayList<BankAccount>();
    }

    public User(String name, String email, String phone, String password, boolean isAdmin) {
        if (name == null || email == null || phone == null || password == null) {
            throw new IllegalArgumentException("Arguments 'name', 'email', 'phone', and 'password' cannot be null.");
        }
        if(!isValidName(name)) {
            throw new IllegalArgumentException("Invalid name format. Name should contain only letters, spaces, and may include a hyphen.");
        }else if(!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format.");
        }

        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.isAdmin = isAdmin;
        this.bankAccounts= new ArrayList<BankAccount>();
    }
    public User(int id, String name, String email, String phone, String password, boolean isAdmin) {
        this.userId = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.isAdmin = isAdmin;
        this.bankAccounts= new ArrayList<BankAccount>();
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public static boolean isValidName(String name) {
        // Name should contain only letters, spaces, and may include a hyphen
        return name.matches("^[A-Za-z\\s-]+$");
    }

    public static boolean isValidEmail(String email) {
        // Email validation using a simple regex pattern
        //email should contain 3 parts and and @ delimiter
        //1. pre-domain part: letters and numbers or the special characters _, +, &, *, -, .,
        //.2. domain part: letters and numbers or hyphens
        //3. top-level domain: letters matches between 2 to 7 letters.
        //it must also contail @ delimiter

        String preDomain = "[a-zA-Z0-9_+&*-].+";
        String domain = "[a-zA-Z0-9-]+";
        String topLevelDomain = "[a-zA-Z]{2,7}";
        String validEmailRegex = preDomain + "@" + domain + "." + topLevelDomain;
        return Pattern.compile(validEmailRegex).matcher(email).matches();

    }



    // Encrypts the password using BCrypt
    public static String encryptPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Checks if the provided password matches the encrypted password
    public static boolean checkPassword(String inputPassword, String hashedPassword) {
        return BCrypt.checkpw(inputPassword, hashedPassword);
    }



    public int getUserId() {
    return this.userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean isAdmin() {
        return this.isAdmin;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getPassword() {
        return this.password;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPassword(String newpassword) {
        this.password = newpassword;
    }

    public void setAdmin(boolean b) {
        this.isAdmin = b;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", admin=" + isAdmin +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId &&
                isAdmin == user.isAdmin &&
                Objects.equals(name, user.name) &&
                Objects.equals(email, user.email) &&
                Objects.equals(phone, user.phone) &&
                Objects.equals(password, user.password);
    }

    @Override
    //Computes the hash code based on all fields (id, name, email, phone, password, isAdmin) using Objects.hash().
    public int hashCode() {
        return Objects.hash(userId, name, email, phone, password, isAdmin);
    }

    public boolean getIsAdmin() {
        return this.isAdmin;
    }
    public List<BankAccount> getBankAccounts() {
        return this.bankAccounts;
    }
    public void addBankAccounts(List<BankAccount> bankAccounts) {
        this.bankAccounts.addAll(bankAccounts);
    }
    public void addBankAccount(BankAccount bankAccount) {
        this.bankAccounts.add(bankAccount);
    }
}
