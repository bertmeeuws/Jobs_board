CREATE DATABASE board;
\c board

CREATE TABLE jobs(
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    date bigint NOT NULL,
    ownerEmail VARCHAR(255) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    title VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    externalUrl VARCHAR(255) NOT NULL,
    salaryLo INT,
    salaryHi INT,
    currency Text,
    remote BOOLEAN NOT NULL,
    country VARCHAR(255) NOT NULL,
    tags text[],
    image text NOT NULL,
    seniority VARCHAR(255) NOT NULL,
    other TEXT
);

CREATE TABLE users(
    email text NOT NULL PRIMARY KEY,
    firstName text NOT NULL,
    lastName text NOT NULL,
    password text NOT NULL,
    role text NOT NULL,
    company text NOT NULL,
    createdAt bigint NOT NULL
);

INSERT INTO public.users (email, firstName, lastName, password, company, role, createdAt)
VALUES ('john.doe@example.com', 'John', 'Doe', 'password123', 'BM Media', 'ADMIN',1623672000);

CREATE TABLE recoverytokens (
  email varchar(255) NOT NULL PRIMARY KEY,
  token varchar(255) NOT NULL,
  expiration bigint NOT NULL
);