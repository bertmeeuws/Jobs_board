CREATE TABLE public."users"(
    email text NOT NULL PRIMARY KEY,
    firstName text NOT NULL,
    lastName text NOT NULL,
    password text NOT NULL,
    role text NOT NULL,
    company text NOT NULL,
    createdAt bigint NOT NULL
);

INSERT INTO public."users" (email, firstName, lastName, password, company, role, createdAt)
VALUES ('john.doe@example.com', 'John', 'Doe', 'password123', 'BM Media', 'ADMIN',1623672000);
