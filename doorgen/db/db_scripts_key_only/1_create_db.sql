DROP TABLE IF EXISTS `snippets`;
DROP TABLE IF EXISTS `cached_page`;
DROP TABLE IF EXISTS `city_page`;
DROP TABLE IF EXISTS `extra_key`;
DROP TABLE IF EXISTS `case`;
DROP TABLE IF EXISTS `city`;
DROP TABLE IF EXISTS `region`;
DROP TABLE IF EXISTS `ref_code`;
DROP TABLE IF EXISTS `ref_attr`;

CREATE TABLE IF NOT EXISTS `ref_attr` (
	ref_attr_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	attr_name VARCHAR(100),
	attr_name_desc VARCHAR(300)
) Type=InnoDB;

CREATE TABLE IF NOT EXISTS `ref_code` (
	ref_code_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	ref_attr_id INT,
	code_value INT,
	code_desc VARCHAR(100),
	CONSTRAINT FOREIGN KEY (`ref_attr_id`) REFERENCES `ref_attr` (`ref_attr_id`) ON DELETE CASCADE ON UPDATE CASCADE
) Type=InnoDB;

CREATE TABLE IF NOT EXISTS `region` (
	region_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	region_name VARCHAR(200) UNIQUE KEY,
	region_name_latin VARCHAR(200) UNIQUE KEY
) Type=InnoDB;

CREATE TABLE IF NOT EXISTS `city` (
	city_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	region_id INT,
	city_name VARCHAR(200),
	city_name_latin VARCHAR(200),
	CONSTRAINT FOREIGN KEY (`region_id`) REFERENCES `region` (`region_id`) ON DELETE CASCADE ON UPDATE CASCADE,
	UNIQUE KEY `city_name` (`city_name`,`region_id`)
) Type=InnoDB;

CREATE TABLE IF NOT EXISTS `case` (
	case_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	location_id INT,
	case_code_value INT,
	location_type_code_value INT,
	case_value VARCHAR(100)
) Type=InnoDB;

CREATE TABLE IF NOT EXISTS `extra_key` (
	key_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	key_value VARCHAR(200),
	key_value_latin VARCHAR(200)
) Type=InnoDB;

CREATE TABLE IF NOT EXISTS `city_page` (
	city_page_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	key_id INT,
	city_id INT,
	city_page_key VARCHAR(200),
	anchor_name VARCHAR(200),
	posted_time TIMESTAMP,
	CONSTRAINT FOREIGN KEY (`key_id`) REFERENCES `extra_key` (`key_id`) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT FOREIGN KEY (`city_id`) REFERENCES `city` (`city_id`) ON DELETE CASCADE ON UPDATE CASCADE,
	UNIQUE KEY `city_page_id` (`key_id`,`city_id`)
) Type=InnoDB;

CREATE TABLE IF NOT EXISTS `cached_page` (
	cached_page_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	cached_page_url VARCHAR(1024) NOT NULL,
	cached_page_title VARCHAR(1024) NOT NULL,
	cached_page_meta_keywords VARCHAR(1024) NOT NULL,
	cached_page_meta_description VARCHAR(2048) NOT NULL,
	cached_time TIMESTAMP
) Type=InnoDB;

CREATE TABLE IF NOT EXISTS `snippets` (
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

DROP FUNCTION IF EXISTS `encodestring`;
CREATE FUNCTION `encodestring` (input_string VARCHAR(200))
RETURNS VARCHAR(255) DETERMINISTIC
RETURN REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(input_string),'а','a'),'б','b'),'в','v'),'г','g'),'д','d'),'е','e'),'ё','e'),'ж','j'),'з','z'),'и','i'),'й','y'),'к','k'),'л','l'),'м','m'),'н','n'),'о','o'),'п','p'),'р','r'),'с','s'),'т','t'),'у','u'),'ф','f'),'х','h'),'ц','ts'),'ч','ch'),'ш','sh'),'щ','sch'),'ъ','y'),'ы','yi'),'ь',''),'э','e'),'ю','yu'),'я','ya');

DROP FUNCTION IF EXISTS `gen_city_page_key`;
delimiter //
CREATE FUNCTION `gen_city_page_key` (key_id INT, city_id INT)
RETURNS VARCHAR(255) DETERMINISTIC
READS SQL DATA
BEGIN
DECLARE gen_city_key_value VARCHAR(255);
SELECT CONCAT(REPLACE(c.city_name_latin," ","-"),"-",REPLACE(ek.key_value_latin," ","-")) INTO gen_city_key_value FROM city c, extra_key ek WHERE c.city_id = city_id AND ek.key_id = key_id;
RETURN gen_city_key_value;
END
//
delimiter ;

DROP PROCEDURE IF EXISTS `fill_city_news`;
delimiter //
CREATE PROCEDURE `fill_city_news`()
BEGIN
  DECLARE done INT DEFAULT FALSE;
  DECLARE city_id, key_id, mod_value INT;
  DECLARE city_name, key_value, anchor_name, case_value VARCHAR(200);
  DECLARE prep_0, prep_1, prep_2 VARCHAR(10);
  
  DECLARE city_key_list CURSOR FOR select c.city_id, ek.key_id  from city c, extra_key ek where (ek.key_id, c.city_id) not in (select cp.key_id, cp.city_id from city_page cp) ORDER BY c.city_id,ek.key_id ASC;
  
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
  
  set prep_0 = " в городе ";
  set prep_1 = " в г. ";
  set prep_2 = " в ";
  
  OPEN city_key_list;
  
  city_key_list_loop: LOOP
    FETCH city_key_list INTO city_id,key_id;
	IF done THEN
		LEAVE city_key_list_loop;
    END IF;
	
	SELECT c.city_name, ek.key_value INTO city_name, key_value FROM `city` c, `extra_key` ek WHERE c.city_id = city_id AND ek.key_id = key_id;
	SET mod_value = (city_id + key_id) % 3;
	
	IF mod_value = 0 THEN begin
			set anchor_name = CONCAT(UPPER(SUBSTR(key_value,1,1)),SUBSTR(key_value,2), prep_0, city_name);
		end;
    ELSEIF mod_value = 1 THEN begin
			set anchor_name = CONCAT(UPPER(SUBSTR(key_value,1,1)),SUBSTR(key_value,2), prep_1, city_name);
		end;
	ELSE begin
			SELECT cs.case_value INTO case_value FROM `case` cs WHERE cs.location_type_code_value = 1 AND cs.location_id = city_id AND case_code_value = 6;
			set anchor_name = CONCAT(UPPER(SUBSTR(key_value,1,1)),SUBSTR(key_value,2), prep_2, case_value);
		end;
    END IF;
	
	INSERT INTO `city_page` (`key_id`,`city_id`,`city_page_key`,`posted_time`,`anchor_name`) select key_id,city_id,gen_city_page_key(key_id,city_id),now() + INTERVAL 10 YEAR,anchor_name;		
  END LOOP;
  CLOSE city_key_list;
END//
delimiter ;