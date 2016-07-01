ALTER DATABASE binaryoptionshowtotrade CHARACTER SET utf8 COLLATE utf8_unicode_ci;

DROP TABLE IF EXISTS neighbor_item;
DROP TABLE IF EXISTS item;
DROP TABLE IF EXISTS category;

CREATE TABLE IF NOT EXISTS category (
	category_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	category_name VARCHAR(200) UNIQUE KEY,
	category_name_latin VARCHAR(200) UNIQUE KEY,
	abbr VARCHAR(127) UNIQUE KEY,
	title VARCHAR(1024) NOT NULL,
	meta_keywords VARCHAR(1024) NOT NULL,
	meta_description VARCHAR(2048) NOT NULL,
	tmpl_text VARCHAR(65535) NOT NULL,
	generated_text VARCHAR(65535) NOT NULL,
	upd_flg TINYINT(1) DEFAULT 0,	
	post_dt TIMESTAMP,
	upd_dt TIMESTAMP
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS item (
	item_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	category_id INT NOT NULL,
	item_name VARCHAR(200) NOT NULL,
	item_name_latin VARCHAR(200) NOT NULL,
	geo_placename VARCHAR(200),
	geo_position VARCHAR(50),
	geo_category VARCHAR(20),
	ICBM VARCHAR(50),
	lat DECIMAL(10, 8), 
	lng DECIMAL(11, 8),
	zip_code VARCHAR(6),
	country VARCHAR(255),
	tmpl_text VARCHAR(65535) NOT NULL,
	generated_text VARCHAR(65535) NOT NULL,
	upd_flg TINYINT(1) DEFAULT 0,	
	post_dt TIMESTAMP,
	upd_dt TIMESTAMP,
	CONSTRAINT FOREIGN KEY (category_id) REFERENCES category (category_id) ON DELETE CASCADE ON UPDATE CASCADE,
	UNIQUE KEY item_name (item_name,category_id)
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS neighbor_item (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	item_id INT,
	neighbor_item_id INT,
	upd_dt TIMESTAMP,
	CONSTRAINT FOREIGN KEY (item_id) REFERENCES item (item_id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT FOREIGN KEY (neighbor_item_id) REFERENCES item (item_id) ON DELETE CASCADE ON UPDATE CASCADE
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;
