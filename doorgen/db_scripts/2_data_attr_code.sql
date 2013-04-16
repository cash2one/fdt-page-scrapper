INSERT INTO `doorgen_banks`.`ref_attr`(`attr_name`,`attr_name_desc`) values 
('case','Падежная форма города/области/республики'),
('location_type','Тип локации: город или республика');

INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'1','Кто, Что' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'2','Кого, Чего' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'3','Кому, Чему' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'4','Кого, Что' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'5','Кем, Чем' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'6','О ком, О чём, В ком, В чём' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'7','Где' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'8','Откуда' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'9','Куда' from `doorgen_banks`.`ref_attr` where attr_name = 'case';

INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'1','Город' from `doorgen_banks`.`ref_attr` where attr_name = 'location_type';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'2','Регион' from `doorgen_banks`.`ref_attr` where attr_name = 'location_type';