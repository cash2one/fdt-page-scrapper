ALTER DATABASE vtopax CHARACTER SET utf8 COLLATE utf8_unicode_ci;

DROP TABLE IF EXISTS `door_keys`;
DROP TABLE IF EXISTS `pages`;
DROP TABLE IF EXISTS `snippets`;
DROP TABLE IF EXISTS `page_content`;
DROP TABLE IF EXISTS `upd_rules`;

CREATE TABLE IF NOT EXISTS `door_keys` (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	key_value VARCHAR(200),
	key_value_latin VARCHAR(200),
	upd_dt TIMESTAMP
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS `pages` (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	key_id INT NOT NULL,
	title VARCHAR(1024) NOT NULL,
	meta_keywords VARCHAR(1024) NOT NULL,
	meta_description VARCHAR(2048) NOT NULL,
	upd_dt TIMESTAMP,
	upd_flg TINYINT(1) DEFAULT 0,
	upd_rule_id INT,
	upd_rule_type VARCHAR(10),
	post_dt TIMESTAMP
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS `snippets` (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	key_id INT NOT NULL,
	title VARCHAR(1024),
	description VARCHAR(1024),
	image_large VARCHAR(1024),
	image_small VARCHAR(1024),
	upd_dt TIMESTAMP
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;


CREATE TABLE IF NOT EXISTS `page_content` (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	page_id INT NOT NULL,
	snippet_id INT NOT NULL,
	snippets_index INT NOT NULL,
	main_flg TINYINT(1) DEFAULT 0,
	upd_dt TIMESTAMP
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS `upd_rule` (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	upd_dt TIMESTAMP
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

DROP FUNCTION IF EXISTS `encodestring`;
CREATE FUNCTION `encodestring` (input_string VARCHAR(200))
RETURNS VARCHAR(255) DETERMINISTIC
RETURN REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(input_string),'а','a'),'б','b'),'в','v'),'г','g'),'д','d'),'е','e'),'ё','e'),'ж','j'),'з','z'),'и','i'),'й','y'),'к','k'),'л','l'),'м','m'),'н','n'),'о','o'),'п','p'),'р','r'),'с','s'),'т','t'),'у','u'),'ф','f'),'х','h'),'ц','ts'),'ч','ch'),'ш','sh'),'щ','sch'),'ъ','y'),'ы','yi'),'ь',''),'э','e'),'ю','yu'),'я','ya'),'і','i');