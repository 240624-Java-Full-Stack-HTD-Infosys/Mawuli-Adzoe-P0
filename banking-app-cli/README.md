# Project 0 - CRUD Banking API
For our first project we are building a simple banking app where users can deposit and withdraw money from one or more bank accounts. At first we will be building this as a console application until we cover Javalin, then we will build out an API to access the functionality.  
  
This project will use a PostGres SQL database hosted in AWS for persistence. We will build DAOs (Data Access Objects) which can interact with the database via JDBC (Java Database Conection). We will build a 3-tier server with data, service, and presentation layers. In the early days of the project we can interact with it via manual testing, JUnit testing, or a console UI, but by week 3 we will be adding an API so that we can interact with our server using [Postman](https://www.postman.com/downloads/).

## Dates
 - Code Freeze: EOB 7/10/24
 - Due Date: 7/12/24
 - Presentations: 7/12/24 - 10:30 AM ET

## Technologies
 - Java
 - SQL
 - REST
 - HTTP
 - Javalin
 - JUnit
 - Mockito

## Presentations
The project is due on Friday 7/12/24 at 10:00 AM ET. We will each be presenting our project and giving a quick demo. The presentation should only be 5-10 minutes per person, take some time to discuss the project, how it turned out, challenges you faced, your favorite or least favorite parts, etc. I would advise against stepping through the code during the presentation. 

Before the due date there is a suggested code freeze date at the end of the day on Wednessday 7/10/24. This means you should stop making major changes to the project, and instead focus on bug fixing, polish, and presentation. You should aim to have your project basically complete by that time.

## User Stories
As a guest I should be able to...
 - Register a new user account
 - Log into a user account

As a user I should be able to...
 - Create a new bank account
 - View my bank accounts
 - View a transaction history for each of my bank accounts
 - Change my user information (name, phone, email, etc)
 - Delete a bank account which has a $0.00 balance
 - Deposit positive dollar amounts into a bank account, increasing the balance
 - Withdraw positive dollar amounts from a bank account, reducing the balance
 - Transfer money between bank accounts, reducing the balance of the source account and increasing the balance of the destination account

### Stretch Goals
As a user I should be able to...
 - Share one or more bank accounts with another user, giving them joint access
 - Un-share, or restrict access to my bank accounts, removing another user's access

As an admin I should be able to...
 - Do everything a regular user can do
 - Elevate other user accounts to admins
 - Reduce other admins to regular users
 - Delete user accounts
 - Delete bank accounts


## Functional Requirements
 - Users should be able to register and login
 - Users should be able to preform CRUD operations on their resources via HTTP API
 - Users should be able to create one or more bank accounts and manage them
 - Users should be able to deposit into, withdraw from, and transfer between bank accounts
 - Users should be able to view a transaction history for each of their bank accounts

### Stretch Goals
 - There should be two roles, user and admin
 - Admins should be able change other user's role to/from admin
 - Admins should be able to delete other user accounts
 - Admins should be able to delete bank accounts
 - Users should be able to share their bank accounts with other users, giving them joint access


## Non-functional Requirements
 - User input should be validated for negative balances, out-of-bounds, types, special characters, ranges, etc.
 - Application should be sanitized and injection-resistant
 - Bank account balances should not suffer from floating point percision problems

### Stretch Goals
 - User sessions should be stateless (no session state on server, use tokens or cookies instead)

## Checklist
These are the MVP features your project should have complete. 
 - Registration
 - Login
 - Create bank account
 - View bank accounts
 - Deposit money
 - Withdraw money
 - Transfer money
 - Delete bank account
 - View transactions per account
 - Change user account info (name, address, email, etc)
 - Validate user input
 - Resist SQL injection (use PreparedStatement and parameterize)
 - Avoid floating point percision (no millionths of pennies)

## Custom Projects
If you wanted to put your own creative spin on this project, it doesn't need to be a banking app. It needs to be a webapp with an HTTP API which exposes CRUD functionality. If you have another idea, come talk to me and we can create a unique set of requirements for your project. It will still need to cover all of the same programming principles. 
