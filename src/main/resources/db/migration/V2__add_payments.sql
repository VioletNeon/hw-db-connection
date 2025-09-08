-- СЕКВЕНС для платежей
create sequence if not exists payment_seq start with 1 increment by 50;

-- Таблица платежей
create table if not exists payments (
    id          bigint primary key,
    product_id  bigint not null,
    author_id   bigint not null,
    amount      numeric(19,2) not null,
    status      varchar(20) not null,
    created_at  timestamp(6) with time zone not null,
    updated_at  timestamp(6) with time zone not null
);
