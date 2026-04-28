-- СЕКВЕНС для лимитов
create sequence if not exists limit_seq start with 1 increment by 50;

-- Таблица лимитов на день
create table if not exists limits (
    id         bigint primary key,
    client_id  bigint not null,
    day        date   not null,
    remaining  numeric(19,2) not null,
    version    bigint not null default 0
);

-- На одного клиента в один день — одна запись
create unique index if not exists ux_limits_client_day on limits(client_id, day);

-- Быстрый поиск по клиенту
create index if not exists ix_limits_client on limits(client_id);
