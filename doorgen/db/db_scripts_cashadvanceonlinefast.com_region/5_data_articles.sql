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