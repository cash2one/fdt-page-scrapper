DROP TABLE IF EXISTS `snippets`;
DROP TABLE IF EXISTS `cached_page`;
DROP TABLE IF EXISTS `page`;

CREATE TABLE IF NOT EXISTS `page` (
	page_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	key_value VARCHAR(200),
	key_value_latin VARCHAR(200),
	posted_time TIMESTAMP
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