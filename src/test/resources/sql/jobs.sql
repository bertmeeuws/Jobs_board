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

INSERT INTO jobs (id, date, ownerEmail, active, title, company, location, description, externalUrl, salaryLo, salaryHi, currency, remote, country, tags, image, seniority, other) VALUES (
    '6ea79557-3112-4c84-a8f5-1d1e2c300949',
    1623672000,
    'example@example.com',
    TRUE,
    'Software Engineer',
    'ABC Company',
    'New York',
    'We are seeking a talented Software Engineer to join our team...',
    'https://www.example.com/job123',
    NULL,
    NULL,
    NULL,
    TRUE,
    'United States',
    ARRAY['backend', 'java', 'sql'],
    'https://www.example.com/images/job123.png',
    'Senior',
    NULL
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
