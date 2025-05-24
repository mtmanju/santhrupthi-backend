CREATE TABLE volunteers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    age INT NOT NULL,
    occupation VARCHAR(100) NOT NULL,
    availability VARCHAR(20) NOT NULL,
    experience TEXT,
    motivation TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
); 