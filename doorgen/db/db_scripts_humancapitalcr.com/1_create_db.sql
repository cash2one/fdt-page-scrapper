ALTER DATABASE humancapitalcr CHARACTER SET utf8 COLLATE utf8_unicode_ci;

DROP TABLE IF EXISTS `upd_rules`;
DROP TABLE IF EXISTS `content_detail`;
DROP TABLE IF EXISTS `page_content`;
DROP TABLE IF EXISTS `snippets`;
DROP TABLE IF EXISTS `pages`;
DROP TABLE IF EXISTS `door_keys`;

CREATE TABLE IF NOT EXISTS `door_keys` (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	key_value VARCHAR(200),
	key_value_latin VARCHAR(200),
	upd_dt TIMESTAMP,
	CONSTRAINT uc_key_value UNIQUE (key_value,key_value_latin)
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS `pages` (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	key_id INT NOT NULL UNIQUE,
	title VARCHAR(1024) NOT NULL,
	meta_keywords VARCHAR(1024) NOT NULL,
	meta_description VARCHAR(2048) NOT NULL,
	upd_dt TIMESTAMP,
	upd_rule_id INT,
	upd_rule_type VARCHAR(10),
	CONSTRAINT FOREIGN KEY (`key_id`) REFERENCES `door_keys` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS `snippets` (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	key_id INT NOT NULL,
	title VARCHAR(1024),
	description VARCHAR(1024),
	image_large VARCHAR(1024),
	image_small VARCHAR(1024),
	upd_dt TIMESTAMP,
	CONSTRAINT FOREIGN KEY (`key_id`) REFERENCES `door_keys` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS `page_content` (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	page_id INT NOT NULL,
	post_dt TIMESTAMP,
	upd_flg TINYINT(1) DEFAULT 0,
	upd_dt TIMESTAMP,
	CONSTRAINT FOREIGN KEY (`page_id`) REFERENCES `pages` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS `content_detail` (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	page_content_id INT NOT NULL,
	snippet_id INT NOT NULL,
	snippets_index INT NOT NULL,
	main_flg TINYINT(1) DEFAULT 0,
	upd_dt TIMESTAMP,
	CONSTRAINT FOREIGN KEY (`page_content_id`) REFERENCES `page_content` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS `upd_rules` (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	upd_dt TIMESTAMP
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

DROP FUNCTION IF EXISTS `encodestring`;
CREATE FUNCTION `encodestring` (input_string VARCHAR(200))
RETURNS VARCHAR(255) DETERMINISTIC
RETURN REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(input_string),'а','a'),'б','b'),'в','v'),'г','g'),'д','d'),'е','e'),'ё','eo'),'ж','j'),'з','z'),'и','i'),'й','y'),'к','k'),'л','l'),'м','m'),'н','n'),'о','o'),'п','p'),'р','r'),'с','s'),'т','t'),'у','u'),'ф','f'),'х','h'),'ц','ts'),'ч','ch'),'ш','sh'),'щ','sch'),'ъ','y'),'ы','yi'),'ь',''),'э','e'),'ю','yu'),'я','ya'),'і','i');