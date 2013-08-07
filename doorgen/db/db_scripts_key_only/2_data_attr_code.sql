INSERT INTO `ref_attr`(`attr_name`,`attr_name_desc`) values 
('case','Падежная форма города/области/республики'),
('location_type','Тип локации: город или республика');

INSERT INTO `ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'1','Кто, Что' from `ref_attr` where attr_name = 'case';
INSERT INTO `ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'2','Кого, Чего' from `ref_attr` where attr_name = 'case';
INSERT INTO `ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'3','Кому, Чему' from `ref_attr` where attr_name = 'case';
INSERT INTO `ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'4','Кого, Что' from `ref_attr` where attr_name = 'case';
INSERT INTO `ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'5','Кем, Чем' from `ref_attr` where attr_name = 'case';
INSERT INTO `ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'6','О ком, О чём, В ком, В чём' from `ref_attr` where attr_name = 'case';
INSERT INTO `ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'7','Где' from `ref_attr` where attr_name = 'case';
INSERT INTO `ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'8','Откуда' from `ref_attr` where attr_name = 'case';
INSERT INTO `ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'9','Куда' from `ref_attr` where attr_name = 'case';

INSERT INTO `ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'1','Город' from `ref_attr` where attr_name = 'location_type';
INSERT INTO `ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'2','Регион' from `ref_attr` where attr_name = 'location_type';