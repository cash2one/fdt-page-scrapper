DROP TABLE IF EXISTS content_detail;
DROP TABLE IF EXISTS page_content;
DROP TABLE IF EXISTS snippets;
DROP TABLE IF EXISTS pages;
DROP TABLE IF EXISTS door_keys;

CREATE TABLE IF NOT EXISTS door_keys (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	key_value VARCHAR(200),
	key_value_latin VARCHAR(200),
	upd_dt TIMESTAMP,
	CONSTRAINT uc_key_value UNIQUE (key_value,key_value_latin)
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
	CONSTRAINT FOREIGN KEY (page_content_id) REFERENCES page_content (id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT FOREIGN KEY (snippet_id) REFERENCES snippets (id) ON DELETE CASCADE ON UPDATE CASCADE
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

DROP FUNCTION IF EXISTS `encodestring`;
CREATE FUNCTION `encodestring` (input_string VARCHAR(200))
RETURNS VARCHAR(255) DETERMINISTIC
RETURN REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(input_string),'à','a'),'á','b'),'â','v'),'ã','g'),'ä','d'),'å','e'),'¸','eo'),'æ','j'),'ç','z'),'è','i'),'é','y'),'ê','k'),'ë','l'),'ì','m'),'í','n'),'î','o'),'ï','p'),'ð','r'),'ñ','s'),'ò','t'),'ó','u'),'ô','f'),'õ','h'),'ö','ts'),'÷','ch'),'ø','sh'),'ù','sch'),'ú','y'),'û','yi'),'ü',''),'ý','e'),'þ','yu'),'ÿ','ya'),'³','i');