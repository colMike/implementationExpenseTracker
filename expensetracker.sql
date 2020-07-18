create database expensetracker;

create table et_categories
(
    category_id int auto_increment
        primary key,
    user_id     int         not null,
    title       varchar(20) not null,
    description varchar(50) not null
);

create table et_transactions
(
    transaction_id   int auto_increment
        primary key,
    category_id      int            not null,
    user_id          int            not null,
    amount           decimal(10, 2) not null,
    note             varchar(50)    not null,
    transaction_date bigint         not null
);

create table et_users
(
    user_id    int auto_increment
        primary key,
    first_name varchar(20) not null,
    last_name  varchar(20) not null,
    email      varchar(30) not null,
    password   text        not null
);

alter table et_categories add constraint cat_users_fk
    foreign key (user_id) references et_users(user_id);

alter table et_transactions add constraint trans_cat_fk
    foreign key (category_id) references et_categories(category_id);
alter table et_transactions add constraint trans_users_fk
    foreign key (user_id) references et_users(user_id);