DELETE FROM door_keys;

DELETE FROM door_keys WHERE key_value = '/';
INSERT INTO door_keys (ex_key_id, city_id, key_value,key_value_latin,upd_dt) select 1,1,"/","/",now();
INSERT INTO pages (key_id,title,meta_keywords,meta_description,upd_dt) SELECT LAST_INSERT_ID(),'Абсолютный лидер в сфере кредитования | vtopaxmira.ru','Абсолютный лидер в сфере кредитования | vtopaxmira.ru','Абсолютный лидер в сфере кредитования | vtopaxmira.ru', now();
INSERT INTO page_content (page_id,post_dt, upd_dt) SELECT LAST_INSERT_ID(), now(), now();

SET @@innodb_lock_wait_timeout:=1;
call fill_region_pages;
SELECT @out;

SET @@innodb_lock_wait_timeout:=1;
call fill_city_news;
SELECT @out;