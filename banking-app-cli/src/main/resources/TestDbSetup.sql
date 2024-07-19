    drop table if exists users CASCADE;
    drop table if exists bank_accounts CASCADE;
    drop table if exists transactions CASCADE;
    drop table if exists authorized_users CASCADE;
    create table users(
        user_id SERIAL primary key,
        name varchar(255) not null,
        email varchar(255) unique,
        phone varchar(10) unique,
        password varchar(255),
        isAdmin boolean
    );

    create table bank_accounts (
        account_number SERIAL primary key,
        user_id int not null,
        email varchar(255) not null,
        account_type varchar(255) not null,
        balance decimal(10,2) not null,
        foreign key (user_id) references users(user_id),
        foreign key (email) references users(email)
    );

    create table transactions (
        transaction_id SERIAL primary key,
        account_number int not null,
        transaction_type varchar(255),
        amount decimal(10,2) not null,
        transaction_date_time timestamp,
        from_account_number int,
        to_account_number int,
        foreign key (account_number) references bank_accounts(account_number)
    );

    create table authorized_users (
        id SERIAL primary key,
        authorized_user_email varchar(255) not null,
        account_number int not null,
        foreign key (account_number) references bank_accounts(account_number)
    );