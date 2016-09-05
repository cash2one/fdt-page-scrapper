DROP TABLE IF EXISTS article_content;
DROP TABLE IF EXISTS article_tmpl;

CREATE TABLE IF NOT EXISTS article_tmpl (
	tmpl_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	titleOrig VARCHAR(255) NOT NULL,
	title VARCHAR(255) NOT NULL,
	description VARCHAR(4095),
	keywords VARCHAR(4095),
	url VARCHAR(255) NOT NULL,
	text VARCHAR(65535) NOT NULL,
	ratingCount FLOAT,
	reviewCount INT,
	upd_dt TIMESTAMP,
	CONSTRAINT uc_url_title UNIQUE (url, title)
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS article_content (
	artcl_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	tmpl_id INT,
	text VARCHAR(65535) NOT NULL,
	upd_flg TINYINT(1) DEFAULT 0,	
	post_dt TIMESTAMP,
	upd_dt TIMESTAMP,
	CONSTRAINT FOREIGN KEY (tmpl_id) REFERENCES article_tmpl (tmpl_id) ON DELETE CASCADE ON UPDATE CASCADE
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

/*
INSERT INTO article_tmpl (title, url, text, upd_dt) values
("title 1", "title_1", "text {compile|generate} test",  now()),
("title 2", "title_2", "text 2 {compile|generate} test",  now()),
("title 3", "title_3", "text 3 {compile|generate} test",  now()),
("title 4", "title_4", "text 4 {compile|generate} test",  now());

INSERT INTO article_content (tmpl_id, text, upd_flg, post_dt, upd_dt) values
(1, "text compile test SHOW NOUPDATE", 0, now() - INTERVAL 7 DAY, now()),
(1, "text compile test NOT", 1, now() + INTERVAL 7 DAY, now()),
(2, "text 21 generate test NOT",  0, now() - INTERVAL 10 DAY, now()),
(2, "text 22 generate test SHOW UPDATE",  1, now() - INTERVAL 5 DAY, now()),
(3, "text 33 generate test NOT",  0, now() + INTERVAL 5 DAY, now()),
(4, "text 44 generate test SHOW NOUPDATE",  0, now() - INTERVAL 5 DAY, now());
*/