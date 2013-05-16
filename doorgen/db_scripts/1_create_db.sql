DROP DATABASE IF EXISTS `doorgen_banks`;

CREATE DATABASE IF NOT EXISTS `doorgen_banks` CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE TABLE IF NOT EXISTS `doorgen_banks`.`ref_attr` (
	ref_attr_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	attr_name VARCHAR(100),
	attr_name_desc VARCHAR(300)
) Type=InnoDB;

CREATE TABLE IF NOT EXISTS `doorgen_banks`.`ref_code` (
	ref_code_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	ref_attr_id INT,
	code_value INT,
	code_desc VARCHAR(100),
	CONSTRAINT FOREIGN KEY (`ref_attr_id`) REFERENCES `ref_attr` (`ref_attr_id`) ON DELETE CASCADE ON UPDATE CASCADE
) Type=InnoDB;

CREATE TABLE IF NOT EXISTS `doorgen_banks`.`region` (
	region_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	region_name VARCHAR(200) UNIQUE KEY,
	region_name_latin VARCHAR(200) UNIQUE KEY
) Type=InnoDB;

CREATE TABLE IF NOT EXISTS `doorgen_banks`.`city` (
	city_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	region_id INT,
	city_name VARCHAR(200),
	city_name_latin VARCHAR(200),
	CONSTRAINT FOREIGN KEY (`region_id`) REFERENCES `region` (`region_id`) ON DELETE CASCADE ON UPDATE CASCADE,
	UNIQUE KEY `city_name` (`city_name`,`region_id`)
) Type=InnoDB;

CREATE TABLE IF NOT EXISTS `doorgen_banks`.`case` (
	case_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	location_id INT,
	case_code_value INT,
	location_type_code_value INT,
	case_value VARCHAR(100)
) Type=InnoDB;

CREATE TABLE IF NOT EXISTS `doorgen_banks`.`extra_key` (
	key_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	key_value VARCHAR(200),
	key_value_latin VARCHAR(200)
) Type=InnoDB;

CREATE TABLE IF NOT EXISTS `doorgen_banks`.`city_page` (
	city_page_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	key_id INT,
	city_id INT,
	city_page_key VARCHAR(200),
	posted_time TIMESTAMP,
	CONSTRAINT FOREIGN KEY (`key_id`) REFERENCES `extra_key` (`key_id`) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT FOREIGN KEY (`city_id`) REFERENCES `city` (`city_id`) ON DELETE CASCADE ON UPDATE CASCADE,
	UNIQUE KEY `city_page_id` (`key_id`,`city_id`)
) Type=InnoDB;

CREATE TABLE IF NOT EXISTS `doorgen_banks`.`cached_page` (
	cached_page_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	cached_page_url VARCHAR(1024) NOT NULL,
	cached_page_title VARCHAR(1024) NOT NULL,
	cached_page_meta_keywords VARCHAR(1024) NOT NULL,
	cached_page_meta_description VARCHAR(2048) NOT NULL,
	cached_time TIMESTAMP
) Type=InnoDB;

CREATE TABLE IF NOT EXISTS `doorgen_banks`.`snippets` (
	snippets_page_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	snippets_index INT NOT NULL,
	cached_page_id INT NOT NULL,
	snippets_title VARCHAR(1024),
	snippets_content VARCHAR(1024),
	snippets_image_large VARCHAR(1024),
	snippets_image_small VARCHAR(1024),
	created_time TIMESTAMP,
	CONSTRAINT FOREIGN KEY (`cached_page_id`) REFERENCES `cached_page` (`cached_page_id`) ON DELETE CASCADE ON UPDATE CASCADE
) Type=InnoDB;

CREATE FUNCTION `doorgen_banks`.`encodestring` (input_string VARCHAR(200))
RETURNS VARCHAR(255) DETERMINISTIC
RETURN REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(input_string),'а','a'),'б','b'),'в','v'),'г','g'),'д','d'),'е','e'),'ё','e'),'ж','j'),'з','z'),'и','i'),'й','y'),'к','k'),'л','l'),'м','m'),'н','n'),'о','o'),'п','p'),'р','r'),'с','s'),'т','t'),'у','u'),'ф','f'),'х','h'),'ц','ts'),'ч','ch'),'ш','sh'),'щ','sch'),'ъ','y'),'ы','yi'),'ь',''),'э','e'),'ю','yu'),'я','ya');

delimiter //
CREATE FUNCTION `doorgen_banks`.`gen_city_page_key` (key_id INT, city_id INT)
RETURNS VARCHAR(255) DETERMINISTIC
READS SQL DATA
BEGIN
DECLARE gen_city_key_value VARCHAR(255);
SELECT CONCAT(REPLACE(c.city_name_latin," ","-"),"-",REPLACE(ek.key_value_latin," ","-")) INTO gen_city_key_value FROM city c, extra_key ek WHERE c.city_id = city_id AND ek.key_id = key_id;
RETURN gen_city_key_value;
END
//