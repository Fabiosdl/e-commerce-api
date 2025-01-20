DROP SCHEMA IF EXISTS `online_shop`;

CREATE SCHEMA `online_shop`;

USE `online_shop`;

-- User Table
CREATE TABLE `user` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(68) NOT NULL,
    address TEXT,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Role Table
CREATE TABLE `role` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_roles` (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    KEY fk_user_role_idx (user_id),
    CONSTRAINT fk_user_role FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES `roles`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Product Table
CREATE TABLE `product` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock INT DEFAULT 0,
    category VARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Basket (Cart) Table
CREATE TABLE `basket` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY fk_user_idx (user_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES `user`(id)
        ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- BasketItem Table
CREATE TABLE `basket_item` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    basket_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY fk_basket_idx (basket_id),
    KEY fk_product_idx (product_id),
    CONSTRAINT fk_basket FOREIGN KEY (basket_id) REFERENCES `basket`(id)
        ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES `product`(id)
        ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Order Table
CREATE TABLE `order` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    basket_id INT NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    payment_status VARCHAR(30) NOT NULL,
    order_status VARCHAR(30) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY fk_order_user_idx (user_id),
    KEY fk_order_basket_idx (basket_id),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES `user`(id)
        ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_order_basket FOREIGN KEY (basket_id) REFERENCES `basket`(id)
        ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- OrderItem Table
CREATE TABLE `order_item` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    product_name VARCHAR(50) NOT NULL,
    product_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY fk_order_idx (order_id),
    KEY fk_product_order_idx (product_id),
    CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES `order`(id)
        ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_product_order FOREIGN KEY (product_id) REFERENCES `product`(id)
        ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Payment Table
CREATE TABLE `payment` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    payment_method VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY fk_payment_order_idx (order_id),
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES `order`(id)
        ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert Sample Roles
INSERT INTO `role` (name) VALUES ('CUSTOMER');
INSERT INTO `role` (name) VALUES ('ADMIN');
INSERT INTO `role` (name) VALUES ('GUEST');

-- Insert Sample Products
INSERT INTO `product` (name, description, price, stock, category) VALUES
('Wireless Noise-Canceling Headphones', 'Premium over-ear headphones with advanced noise-canceling technology, 30-hour battery life, and a comfortable fit.', 199.99, 120, 'Electronics'),
('Ergonomic Office Chair', 'Adjustable height, lumbar support, and breathable mesh backrest for all-day comfort.', 149.99, 50, 'Furniture'),
('Stainless Steel Water Bottle (1L)', 'Insulated water bottle keeps drinks cold for 24 hours or hot for 12 hours, made with eco-friendly materials.', 24.99, 300, 'Outdoor'),
('Yoga Mat', 'Non-slip, extra-thick mat for yoga, pilates, and workouts. Includes a carrying strap.', 29.99, 200, 'Fitness'),
('Gourmet Coffee Beans (500g)', 'Rich, aromatic coffee beans sourced from sustainable farms for the perfect cup of coffee.', 14.99, 500, 'Grocery'),
('4K Ultra HD Smart TV (55")', 'Stunning picture quality with HDR, built-in apps, and voice assistant compatibility.', 699.99, 30, 'Electronics'),
('Leather Tote Bag', 'Stylish and durable tote bag with multiple compartments, perfect for work or travel.', 89.99, 80, 'Fashion'),
('Wireless Gaming Mouse', 'High-precision gaming mouse with customizable buttons, RGB lighting, and 70-hour battery life.', 49.99, 150, 'Electronics'),
('Running Shoes', 'Lightweight and breathable running shoes designed for comfort and performance.', 59.99, 100, 'Fitness'),
('Hardcover Notebook (200 Pages)', 'Durable hardcover notebook with dotted pages, ideal for journaling and note-taking.', 12.99, 250, 'Stationery');
