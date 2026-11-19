-- PIET Canteen Billing System: safe, incremental MySQL migration
-- BACK UP piet_canteen_db before running. This script does not drop existing tables or rows.
CREATE DATABASE IF NOT EXISTS piet_canteen_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE piet_canteen_db;

-- Existing core tables are retained. Create them only if this is a fresh database.
CREATE TABLE IF NOT EXISTS users (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NULL,
  role ENUM('ADMIN','STAFF') NOT NULL DEFAULT 'STAFF'
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS food_items (
  item_id INT AUTO_INCREMENT PRIMARY KEY,
  item_name VARCHAR(150) NOT NULL,
  category VARCHAR(100) NOT NULL,
  price DECIMAL(10,2) NOT NULL DEFAULT 0,
  available BOOLEAN NOT NULL DEFAULT TRUE,
  stock_quantity INT NOT NULL DEFAULT 0,
  image_path VARCHAR(500) NULL
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS orders (
  order_id INT AUTO_INCREMENT PRIMARY KEY,
  order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  username VARCHAR(100) NOT NULL,
  subtotal DECIMAL(10,2) NOT NULL,
  discount DECIMAL(10,2) NOT NULL DEFAULT 0,
  gst DECIMAL(10,2) NOT NULL DEFAULT 0,
  final_total DECIMAL(10,2) NOT NULL,
  payment_method VARCHAR(30) NOT NULL
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS order_items (
  order_item_id INT AUTO_INCREMENT PRIMARY KEY,
  order_id INT NOT NULL,
  item_id INT NULL,
  item_name VARCHAR(150) NOT NULL,
  quantity INT NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL,
  line_total DECIMAL(10,2) NOT NULL,
  INDEX(order_id),
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(order_id)
) ENGINE=InnoDB;

DELIMITER $$
DROP PROCEDURE IF EXISTS piet_add_column_if_missing$$
CREATE PROCEDURE piet_add_column_if_missing(IN tbl VARCHAR(64), IN col VARCHAR(64), IN definition TEXT)
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name=tbl AND column_name=col) THEN
    SET @piet_sql = CONCAT('ALTER TABLE `', REPLACE(tbl,'`',''), '` ADD COLUMN `', REPLACE(col,'`',''), '` ', definition);
    PREPARE piet_stmt FROM @piet_sql; EXECUTE piet_stmt; DEALLOCATE PREPARE piet_stmt;
  END IF;
END$$
DELIMITER ;

CALL piet_add_column_if_missing('users','password_hash','VARCHAR(255) NULL');
CALL piet_add_column_if_missing('users','must_change_password','BOOLEAN NOT NULL DEFAULT FALSE');
CALL piet_add_column_if_missing('users','active','BOOLEAN NOT NULL DEFAULT TRUE');
CALL piet_add_column_if_missing('food_items','min_stock_threshold','INT NOT NULL DEFAULT 10');
CALL piet_add_column_if_missing('orders','status','ENUM(''COMPLETED'',''CANCELLED'',''REFUNDED'') NOT NULL DEFAULT ''COMPLETED''');
CALL piet_add_column_if_missing('orders','payment_status','ENUM(''PAID'',''CANCELLED'',''REFUNDED'',''PARTIALLY_REFUNDED'') NOT NULL DEFAULT ''PAID''');
CALL piet_add_column_if_missing('orders','gst_rate','DECIMAL(5,2) NOT NULL DEFAULT 5.00');
CALL piet_add_column_if_missing('orders','discount_percent','DECIMAL(5,2) NOT NULL DEFAULT 0.00');
CALL piet_add_column_if_missing('orders','status_reason','VARCHAR(500) NULL');
CALL piet_add_column_if_missing('orders','status_changed_by','VARCHAR(100) NULL');
CALL piet_add_column_if_missing('orders','status_changed_at','DATETIME NULL');
CALL piet_add_column_if_missing('order_items','item_id','INT NULL');

-- Permit legacy plaintext password column to be nulled after successful one-time hash migration.
ALTER TABLE users MODIFY COLUMN password VARCHAR(255) NULL;

CREATE TABLE IF NOT EXISTS stock_movements (
  movement_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  item_id INT NULL,
  previous_quantity INT NOT NULL,
  new_quantity INT NOT NULL,
  change_quantity INT NOT NULL,
  reason VARCHAR(500) NOT NULL,
  username VARCHAR(100) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_stock_movement_date(created_at),
  CONSTRAINT fk_stock_movement_item FOREIGN KEY(item_id) REFERENCES food_items(item_id) ON DELETE SET NULL
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS activity_logs (
  activity_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NULL,
  action_type VARCHAR(100) NOT NULL,
  details TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_activity_date(created_at), INDEX idx_activity_user(username)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS notifications (
  notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  notification_type VARCHAR(60) NOT NULL,
  message VARCHAR(1000) NOT NULL,
  is_read BOOLEAN NOT NULL DEFAULT FALSE,
  read_by VARCHAR(100) NULL,
  read_at DATETIME NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_notification_date(created_at)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS app_settings (
  setting_key VARCHAR(100) PRIMARY KEY,
  setting_value VARCHAR(500) NOT NULL,
  updated_by VARCHAR(100) NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
INSERT INTO app_settings(setting_key,setting_value,updated_by) VALUES('gst_percent','5.0','migration') ON DUPLICATE KEY UPDATE setting_key=VALUES(setting_key);

DROP PROCEDURE IF EXISTS piet_add_column_if_missing;

-- Existing orders are retained and default to COMPLETED so historic sales remain visible.
-- Legacy passwords are upgraded to PBKDF2 hashes on successful login and then nulled.
