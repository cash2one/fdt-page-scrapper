CREATE DATABASE `doorgen_banks` CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE TABLE `doorgen_banks`.`ref_attr` (
	ref_attr_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	attr_name VARCHAR(100),
	attr_name_desc VARCHAR(300)
) Type=InnoDB;

CREATE TABLE `doorgen_banks`.`ref_code` (
	ref_code_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	ref_attr_id INT,
	code_value INT,
	code_desc VARCHAR(100),
	CONSTRAINT FOREIGN KEY (`ref_attr_id`) REFERENCES `ref_attr` (`ref_attr_id`) ON DELETE CASCADE ON UPDATE CASCADE
) Type=InnoDB;

CREATE TABLE `doorgen_banks`.`region` (
	region_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	region_name VARCHAR(200) UNIQUE KEY
) Type=InnoDB;

CREATE TABLE `doorgen_banks`.`city` (
	city_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	region_id INT,
	city_name VARCHAR(200),
	CONSTRAINT FOREIGN KEY (`region_id`) REFERENCES `region` (`region_id`) ON DELETE CASCADE ON UPDATE CASCADE,
	UNIQUE KEY `city_name` (`city_name`,`region_id`)
) Type=InnoDB;

CREATE TABLE `doorgen_banks`.`case` (
	case_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	location_id INT,
	case_code_value INT,
	location_type_code_value INT,
	case_value VARCHAR(100)
) Type=InnoDB;

INSERT INTO `doorgen_banks`.`ref_attr`(`attr_name`,`attr_name_desc`) values 
('case','Падежная форма города/области/республики'),
('location_type','Тип локации: город или республика');

INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'1','Кто, Что' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'2','Кого, Чего' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'3','Кому, Чему' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'4','Кого, Что' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'5','Кем, Чем' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'6','О ком, О чём, В ком, В чём' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'7','Где' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'8','Откуда' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'9','Куда' from `doorgen_banks`.`ref_attr` where attr_name = 'case';

INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'1','Город' from `doorgen_banks`.`ref_attr` where attr_name = 'location_type';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'2','Регион' from `doorgen_banks`.`ref_attr` where attr_name = 'location_type';

