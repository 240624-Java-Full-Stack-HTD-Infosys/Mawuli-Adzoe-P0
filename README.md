# Mawuli-Adzoe-P0

## How to run frontend(React.js)
  - `cd ./bank-accounts-viewer`
  - `npm install`
  - `npm start`

## How to run backend(Java Spring Boot)
  - `cd ./banking-app-cli`
  - `mvn spring-boot:run`

Two ways to check if this code works: PostMan or the automated tests in BankingAPITest.java

BankingAPITest.java tests that the Banking API works correctly from a user's perspective. 
Each test case is written as a user story to illustrate how users interact with the system.

User Stories
1. Guest User Registration
Story: As a guest, I want to register a new user account so that I can start using the banking services.
Test Method: guest_can_register_a_new_user_account_and_get_back_a_user_object_json()


2. Guest User Login and Account Creation
Story: As a guest, I want to log in and create a new bank account so that I can start managing my finances.
Test Method: guest_can_login_and_create_a_new_bank_account_and_get_back_bank_account_json()


3. Deposit Money into Account
Story: As a logged-in user, I want to deposit money into my bank account so that I can increase my account balance.
Test Method: logged_in_user_can_deposit_money_into_the_account_and_get_back_bank_account_json()

4. Update User Information
Story: As a logged-in user, I want to update my user information (name, phone, email, etc.) so that my account details are current.
Test Method: logged_in_user_can_change_user_information_and_get_back_user_object_json()

5. Delete Bank Account with Zero Balance
Story: As a logged-in user, I want to delete my bank account that has a zero balance so that I can close the account.
Test Method: logged_in_user_can_delete_a_bank_account_with_a_zero_balance_and_get_back_a_message_json()

6. Transfer Money Between Accounts
Story: As a logged-in user, I want to transfer money between my accounts so that I can manage my funds effectively.
Test Method: logged_in_user_can_transfer_money_between_accounts_and_get_back_bank_account_json()

7. Admin View All Users, Accounts, and Transactions
Story: As an admin, I want to view all users, accounts, and transactions so that I can manage the banking system effectively.
Test Method: admin_can_view_all_users_accounts_and_transactions_and_get_back_list_json()
