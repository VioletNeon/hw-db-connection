-- СЕКВЕНСЫ (шаг 50)
create sequence if not exists author_seq start with 1 increment by 50;
create sequence if not exists article_seq start with 1 increment by 50;
create sequence if not exists product_seq start with 1 increment by 50;

-- AUTHORS
create table if not exists authors (
    id        bigint primary key,
    name      varchar(30) not null unique
);

-- ARTICLES
create table if not exists articles (
    id          bigint primary key,
    content     varchar(1000) not null,
    created_at  timestamp(6) with time zone not null,
    updated_at  timestamp(6) with time zone not null,
    author_id   bigint not null
);

alter table articles
  add constraint fk_articles_author
  foreign key (author_id) references authors(id);

-- PRODUCTS
create table if not exists products (
    id              bigint primary key,
    account_number  varchar(32) not null unique,
    balance         numeric(19,2) not null default 0,
    type            varchar(20)  not null,
    created_at      timestamp(6) with time zone not null,
    updated_at      timestamp(6) with time zone not null,
    author_id       bigint not null
);

alter table products
  add constraint fk_products_author
  foreign key (author_id) references authors(id);

create index if not exists ix_products_author on products(author_id);
