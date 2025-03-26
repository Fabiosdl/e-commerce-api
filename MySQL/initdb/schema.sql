DROP SCHEMA IF EXISTS `e-commerce`;

CREATE SCHEMA `e-commerce`;

USE `e-commerce`;

-- User Table
CREATE TABLE `user` (
    id BINARY(16) PRIMARY KEY,
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
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_roles` (
    user_id BINARY(16) NOT NULL,
    role_id BINARY(16) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    KEY fk_user_role_idx (user_id),
    CONSTRAINT fk_user_role FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES `role`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Product Table
CREATE TABLE `product` (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    image_src VARCHAR(250),
    stock INT DEFAULT 0,
    category VARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Basket (Cart) Table
CREATE TABLE `basket` (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY fk_user_idx (user_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES `user`(id)
        ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- BasketItem Table
CREATE TABLE `basket_item` (
    id BINARY(16) PRIMARY KEY,
    basket_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
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
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    basket_id BINARY(16) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    payment_status VARCHAR(30) NOT NULL,
    order_status VARCHAR(30) NOT NULL,
    paypal_order_id VARCHAR(100),
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
    id BINARY(16) PRIMARY KEY,
    order_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
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
    id BINARY(16) PRIMARY KEY,
    order_id BINARY(16) NOT NULL,
    payment_method VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY fk_payment_order_idx (order_id),
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES `order`(id)
        ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert Sample Roles
INSERT INTO `role` (id,name) VALUES (UUID_TO_BIN(UUID()),'ROLE_CUSTOMER');
INSERT INTO `role` (id,name) VALUES (UUID_TO_BIN(UUID()),'ROLE_ADMIN');
INSERT INTO `role` (id,name) VALUES (UUID_TO_BIN(UUID()),'ROLE_GUEST');
INSERT INTO `role` (id,name) VALUES (UUID_TO_BIN(UUID()),'ROLE_VENDOR');

-- Insert Sample Products
INSERT INTO `product` (id,name, description, price, image_src, stock, category) VALUES
(UUID_TO_BIN(UUID()),'Wireless Noise-Canceling Headphones', 'Premium over-ear headphones with advanced noise-canceling technology, 30-hour battery life, and a comfortable fit.', 199.99, 'https://e-commerce-3lsn09a38-fabio-limas-projects-b6f96bd6.vercel.app/images/headphone.jpg',120, 'Electronics'),
(UUID_TO_BIN(UUID()),'Ergonomic Office Chair', 'Adjustable height, lumbar support, and breathable mesh backrest for all-day comfort.', 149.99, 'https://e-commerce-3lsn09a38-fabio-limas-projects-b6f96bd6.vercel.app/images/office-chair.jpg', 50, 'Furniture'),
(UUID_TO_BIN(UUID()),'Stainless Steel Water Bottle (1L)', 'Insulated water bottle keeps drinks cold for 24 hours or hot for 12 hours, made with eco-friendly materials.', 24.99, 'https://e-commerce-3lsn09a38-fabio-limas-projects-b6f96bd6.vercel.app/images/bottle.jpg', 300, 'Outdoor'),
(UUID_TO_BIN(UUID()),'Yoga Mat', 'Non-slip, extra-thick mat for yoga, pilates, and workouts. Includes a carrying strap.',29.99, 'https://e-commerce-3lsn09a38-fabio-limas-projects-b6f96bd6.vercel.app/images/yoga-mat.jpg', 200, 'Fitness'),
(UUID_TO_BIN(UUID()),'Gourmet Coffee Beans (500g)', 'Rich, aromatic coffee beans sourced from sustainable farms for the perfect cup of coffee.', 14.99, 'https://e-commerce-3lsn09a38-fabio-limas-projects-b6f96bd6.vercel.app/images/coffee.jpg', 500, 'Grocery'),
(UUID_TO_BIN(UUID()),'4K Ultra HD Smart TV (55")', 'Stunning picture quality with HDR, built-in apps, and voice assistant compatibility.', 699.99, 'https://e-commerce-3lsn09a38-fabio-limas-projects-b6f96bd6.vercel.app/images/tv.jpg', 30, 'Electronics'),
(UUID_TO_BIN(UUID()),'Leather Tote Bag', 'Stylish and durable tote bag with multiple compartments, perfect for work or travel.', 89.99, 'https://e-commerce-3lsn09a38-fabio-limas-projects-b6f96bd6.vercel.app/images/bag.jpg', 80, 'Fashion'),
(UUID_TO_BIN(UUID()),'Wireless Gaming Mouse', 'High-precision gaming mouse with customizable buttons, RGB lighting, and 70-hour battery life.', 49.99, 'https://e-commerce-3lsn09a38-fabio-limas-projects-b6f96bd6.vercel.app/images/mouse.jpg', 150, 'Electronics');