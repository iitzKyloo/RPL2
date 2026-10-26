-- Jalankan di phpMyAdmin (tab SQL) setelah MySQL di XAMPP menyala
CREATE DATABASE IF NOT EXISTS simka;
USE simka;

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(64) NOT NULL
);

-- Akun awal: admin / admin123 (password disimpan dalam bentuk hash SHA-256)
INSERT INTO users (username, password)
VALUES ('admin', SHA2('admin123', 256));
