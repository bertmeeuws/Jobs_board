CREATE TABLE recoverytokens (
  email varchar(255) NOT NULL PRIMARY KEY,
  token varchar(255) NOT NULL,
  expiration bigint NOT NULL
);