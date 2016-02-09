ALTER DATABASE adaus_test CHARACTER SET utf8 COLLATE utf8_unicode_ci;

DROP TABLE IF EXISTS upd_rules;
DROP TABLE IF EXISTS content_detail;
DROP TABLE IF EXISTS page_content;
DROP TABLE IF EXISTS snippets;
DROP TABLE IF EXISTS pages;
DROP TABLE IF EXISTS door_keys;
DROP TABLE IF EXISTS cases;
DROP TABLE IF EXISTS neighbor_city;
DROP TABLE IF EXISTS city;
DROP TABLE IF EXISTS region;
DROP TABLE IF EXISTS ref_code;
DROP TABLE IF EXISTS ref_attr;
DROP TABLE IF EXISTS extra_key;

CREATE TABLE IF NOT EXISTS extra_key (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	key_value VARCHAR(200),
	key_value_latin VARCHAR(200)
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS ref_attr (
	ref_attr_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	attr_name VARCHAR(100),
	attr_name_desc VARCHAR(300)
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS ref_code (
	ref_code_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	ref_attr_id INT,
	code_value INT,
	code_desc VARCHAR(100),
	CONSTRAINT FOREIGN KEY (ref_attr_id) REFERENCES ref_attr (ref_attr_id) ON DELETE CASCADE ON UPDATE CASCADE
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS region (
	region_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	region_name VARCHAR(200) UNIQUE KEY,
	region_name_latin VARCHAR(200) UNIQUE KEY,
	abbr VARCHAR(2) UNIQUE KEY,
	title VARCHAR(1024) NOT NULL,
	meta_keywords VARCHAR(1024) NOT NULL,
	meta_description VARCHAR(2048) NOT NULL
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS city (
	city_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	region_id INT,
	city_name VARCHAR(200),
	city_name_latin VARCHAR(200),
	geo_placename VARCHAR(200),
	geo_position VARCHAR(50),
	geo_region VARCHAR(20),
	ICBM VARCHAR(50),
	zip_code VARCHAR(6),
	country VARCHAR(255),
	CONSTRAINT FOREIGN KEY (region_id) REFERENCES region (region_id) ON DELETE CASCADE ON UPDATE CASCADE,
	UNIQUE KEY city_name (city_name,region_id)
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS neighbor_city (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	city_id INT,
	neighbor_city_id INT,
	upd_dt TIMESTAMP,
	CONSTRAINT FOREIGN KEY (city_id) REFERENCES city (city_id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT FOREIGN KEY (neighbor_city_id) REFERENCES city (city_id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT uc_key_value UNIQUE (city_id,neighbor_city_id)
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS cases (
	case_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	location_id INT,
	case_code_value INT,
	location_type_code_value INT,
	case_value VARCHAR(100)
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS door_keys (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	ex_key_id INT,
	city_id INT,
	key_value VARCHAR(200),
	key_value_latin VARCHAR(200),
	upd_dt TIMESTAMP,
	CONSTRAINT FOREIGN KEY (ex_key_id) REFERENCES extra_key (id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT FOREIGN KEY (city_id) REFERENCES city (city_id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT uc_key_value UNIQUE (key_value,key_value_latin, city_id, ex_key_id)
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS pages (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	key_id INT NOT NULL UNIQUE,
	title VARCHAR(1024) NOT NULL,
	meta_keywords VARCHAR(1024) NOT NULL,
	meta_description VARCHAR(2048) NOT NULL,
	upd_dt TIMESTAMP,
	upd_rule_id INT,
	upd_rule_type VARCHAR(10),
	CONSTRAINT FOREIGN KEY (key_id) REFERENCES door_keys (id) ON DELETE CASCADE ON UPDATE CASCADE
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS snippets (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	key_id INT NOT NULL,
	title VARCHAR(1024),
	description VARCHAR(1024),
	image_large VARCHAR(1024),
	image_small VARCHAR(1024),
	upd_dt TIMESTAMP,
	CONSTRAINT FOREIGN KEY (key_id) REFERENCES door_keys (id) ON DELETE CASCADE ON UPDATE CASCADE
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS page_content (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	page_id INT NOT NULL,
	post_dt TIMESTAMP,
	upd_flg TINYINT(1) DEFAULT 0,
	upd_dt TIMESTAMP,
	CONSTRAINT FOREIGN KEY (page_id) REFERENCES pages (id) ON DELETE CASCADE ON UPDATE CASCADE
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS content_detail (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	page_content_id INT NOT NULL,
	snippet_id INT NOT NULL,
	snippets_index INT NOT NULL,
	main_flg TINYINT(1) DEFAULT 0,
	upd_dt TIMESTAMP,
	CONSTRAINT FOREIGN KEY (page_content_id) REFERENCES page_content (id) ON DELETE CASCADE ON UPDATE CASCADE
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS upd_rules (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	upd_dt TIMESTAMP
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

DROP FUNCTION IF EXISTS encodestring;
CREATE FUNCTION encodestring (input_string VARCHAR(200))
RETURNS VARCHAR(255) DETERMINISTIC
RETURN REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(input_string),'а','a'),'б','b'),'в','v'),'г','g'),'д','d'),'е','e'),'ё','eo'),'ж','j'),'з','z'),'и','i'),'й','y'),'к','k'),'л','l'),'м','m'),'н','n'),'о','o'),'п','p'),'р','r'),'с','s'),'т','t'),'у','u'),'ф','f'),'х','h'),'ц','ts'),'ч','ch'),'ш','sh'),'щ','sch'),'ъ','y'),'ы','yi'),'ь',''),'э','e'),'ю','yu'),'я','ya');

DROP FUNCTION IF EXISTS gen_city_page_key;
delimiter //
CREATE FUNCTION gen_city_page_key (key_id INT, city_id INT)
RETURNS VARCHAR(255) DETERMINISTIC
READS SQL DATA
BEGIN
DECLARE gen_city_key_value VARCHAR(255);
SELECT CONCAT(REPLACE(c.city_name_latin," ","-"),"-",REPLACE(ek.key_value_latin," ","-")) INTO gen_city_key_value FROM city c, extra_key ek WHERE c.city_id = city_id AND ek.id = key_id;
RETURN gen_city_key_value;
END
//
delimiter ;

DROP PROCEDURE IF EXISTS fill_city_news;
delimiter //
CREATE PROCEDURE fill_city_news()
BEGIN
  DECLARE done INT DEFAULT FALSE;
  DECLARE city_id, key_id, mod_value INT;
  DECLARE city_name, key_value, anchor_name, case_value VARCHAR(200);
  DECLARE prep_0, prep_1, prep_2 VARCHAR(10);
  
  DECLARE city_key_list CURSOR FOR select c.city_id, ek.id  from city c, extra_key ek where (ek.id, c.city_id) not in (select cp.ex_key_id, cp.city_id from door_keys cp) ORDER BY c.city_id,ek.id ASC;
  
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
	
	SELECT c.city_name, ek.key_value INTO city_name, key_value FROM city c, extra_key ek WHERE c.city_id = city_id AND ek.id = key_id;
	SET mod_value = (city_id + key_id) % 3;
	
	IF mod_value = 0 THEN begin
			set anchor_name = CONCAT(UPPER(SUBSTR(key_value,1,1)),SUBSTR(key_value,2), prep_0, city_name);
		end;
    ELSEIF mod_value = 1 THEN begin
			set anchor_name = CONCAT(UPPER(SUBSTR(key_value,1,1)),SUBSTR(key_value,2), prep_1, city_name);
		end;
	ELSE begin
			SELECT cs.case_value INTO case_value FROM cases cs WHERE cs.location_type_code_value = 1 AND cs.location_id = city_id AND case_code_value = 6;
			set anchor_name = CONCAT(UPPER(SUBSTR(key_value,1,1)),SUBSTR(key_value,2), prep_2, case_value);
		end;
    END IF;
	
	INSERT INTO door_keys (ex_key_id,city_id,key_value_latin,key_value,upd_dt) select key_id,city_id,gen_city_page_key(key_id,city_id),anchor_name,now();
	
  END LOOP;
  CLOSE city_key_list;
END
//
delimiter ;

DROP PROCEDURE IF EXISTS fill_region_pages;
delimiter //
CREATE PROCEDURE fill_region_pages()
BEGIN
  DECLARE done INT DEFAULT FALSE;
  DECLARE mod_value INT;
  DECLARE region_name, region_id, anchor_name, site_name VARCHAR(200);
  DECLARE prep_0, prep_1, prep_2, prep_3, prep_4, prep_5, prep_6, prep_7, prep_8, prep_9 VARCHAR(127);
  DECLARE prep_10, prep_11, prep_12, prep_13, prep_14, prep_15, prep_16, prep_17, prep_18, prep_19 VARCHAR(127);
  DECLARE prep_20, prep_21, prep_22, prep_23 VARCHAR(127);
  
  DECLARE region_list CURSOR FOR select r.region_id, r.region_name from region r;
  
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
  
  set site_name = "vtopaxmira.ru";
  
    set prep_0 = "Регионы и Округи, Кредиты в России, Банки России, Области";
	set prep_1 = "Регионы и Округи, Области, Банки России, Кредиты в России";
	set prep_2 = "Кредиты в России, Области, Банки России, Регионы и Округи";
	set prep_3 = "Регионы и Округи, Банки России, Области, Кредиты в России";
	set prep_4 = "Кредиты в России, Регионы и Округи, Области, Банки России";
	set prep_5 = "Области, Регионы и Округи, Банки России, Кредиты в России";
	set prep_6 = "Кредиты в России, Области, Регионы и Округи, Банки России";
	set prep_7 = "Банки России, Регионы и Округи, Области, Кредиты в России";
	set prep_8 = "Области, Кредиты в России, Регионы и Округи, Банки России";
	set prep_9 = "Области, Кредиты в России, Банки России, Регионы и Округи";
	set prep_10 = "Банки России, Регионы и Округи, Кредиты в России, Области";
	set prep_11 = "Регионы и Округи, Области, Кредиты в России, Банки России";
	set prep_12 = "Кредиты в России, Банки России, Области, Регионы и Округи";
	set prep_13 = "Регионы и Округи, Банки России, Кредиты в России, Области";
	set prep_14 = "Области, Регионы и Округи, Кредиты в России, Банки России";
	set prep_15 = "Кредиты в России, Банки России, Регионы и Округи, Области";
	set prep_16 = "Области, Банки России, Кредиты в России, Регионы и Округи";
	set prep_17 = "Области, Банки России, Регионы и Округи, Кредиты в России";
	set prep_18 = "Банки России, Кредиты в России, Области, Регионы и Округи";
	set prep_19 = "Банки России, Области, Регионы и Округи, Кредиты в России";
	set prep_20 = "Банки России, Кредиты в России, Регионы и Округи, Области";
	set prep_21 = "Банки России, Области, Кредиты в России, Регионы и Округи";
	set prep_22 = "Кредиты в России, Регионы и Округи, Банки России, Области";
	set prep_23 = "Регионы и Округи, Кредиты в России, Области, Банки России";
	
  
  OPEN region_list;
  
  region_list_loop: LOOP
    FETCH region_list INTO region_id,region_name;
	IF done THEN
		LEAVE region_list_loop;
    END IF;
	
	SET mod_value = region_id % 24;
	
	IF mod_value = 0 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_0, ' | ', site_name);
	end;
    ELSEIF mod_value = 1 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_1, ' | ', site_name);
	end;
	ELSEIF mod_value = 2 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_2, ' | ', site_name);
	end;
	ELSEIF mod_value = 3 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_3, ' | ', site_name);
	end;
	ELSEIF mod_value = 4 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_4, ' | ', site_name);
	end;
	ELSEIF mod_value = 5 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_5, ' | ', site_name);
	end;
	ELSEIF mod_value = 6 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_6, ' | ', site_name);
	end;
	ELSEIF mod_value = 7 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_7, ' | ', site_name);
	end;
	ELSEIF mod_value = 8 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_8, ' | ', site_name);
	end;
	ELSEIF mod_value = 9 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_9, ' | ', site_name);
	end;
	ELSEIF mod_value = 10 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_10, ' | ', site_name);
	end;
	ELSEIF mod_value = 11 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_11, ' | ', site_name);
	end;
	ELSEIF mod_value = 12 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_12, ' | ', site_name);
	end;
	ELSEIF mod_value = 13 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_13, ' | ', site_name);
	end;
	ELSEIF mod_value = 14 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_14, ' | ', site_name);
	end;
	ELSEIF mod_value = 15 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_15, ' | ', site_name);
	end;
	ELSEIF mod_value = 16 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_16, ' | ', site_name);
	end;
	ELSEIF mod_value = 17 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_17, ' | ', site_name);
	end;
	ELSEIF mod_value = 18 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_18, ' | ', site_name);
	end;
	ELSEIF mod_value = 19 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_19, ' | ', site_name);
	end;
	ELSEIF mod_value = 20 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_20, ' | ', site_name);
	end;
	ELSEIF mod_value = 21 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_21, ' | ', site_name);
	end;
	ELSEIF mod_value = 22 THEN begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_22, ' | ', site_name);
	end;
	ELSE begin
		set anchor_name = CONCAT(UPPER(SUBSTR(region_name,1,1)),SUBSTR(region_name,2), " - ", prep_23, ' | ', site_name);
	end;
    END IF;
	
	UPDATE region r SET r.title = anchor_name, r.meta_keywords = anchor_name, r.meta_description = anchor_name WHERE r.region_id = region_id;
	
  END LOOP;
  CLOSE region_list;
END
//
delimiter ;


