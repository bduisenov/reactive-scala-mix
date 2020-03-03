CREATE TABLE users
(
    id   SERIAL       NOT NULL
        CONSTRAINT users_pkey PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);
CREATE INDEX users_name_idx ON users (name);