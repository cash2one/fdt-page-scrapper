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
	posted_time TIMESTAMP,
	CONSTRAINT FOREIGN KEY (`key_id`) REFERENCES `extra_key` (`key_id`) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT FOREIGN KEY (`city_id`) REFERENCES `city` (`city_id`) ON DELETE CASCADE ON UPDATE CASCADE,
	UNIQUE KEY `city_page_id` (`key_id`,`city_id`)
) Type=InnoDB;

CREATE FUNCTION `doorgen_banks`.`encodestring` (input_string VARCHAR(200))
RETURNS VARCHAR(255) DETERMINISTIC
RETURN REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(input_string),'а','a'),'б','b'),'в','v'),'г','g'),'д','d'),'е','e'),'ё','e'),'ж','j'),'з','z'),'и','i'),'й','y'),'к','k'),'л','l'),'м','m'),'н','n'),'о','o'),'п','p'),'р','r'),'с','s'),'т','t'),'у','u'),'ф','f'),'х','h'),'ц','ts'),'ч','ch'),'ш','sh'),'щ','sch'),'ъ','y'),'ы','yi'),'ь',''),'э','e'),'ю','yu'),'я','ya');