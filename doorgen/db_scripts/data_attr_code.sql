INSERT INTO `doorgen_banks`.`ref_attr`(`attr_name`,`attr_name_desc`) values 
('case','�������� ����� ������/�������/����������'),
('location_type','��� �������: ����� ��� ����������');

INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'1','���, ���' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'2','����, ����' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'3','����, ����' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'4','����, ���' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'5','���, ���' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'6','� ���, � ���, � ���, � ���' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'7','���' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'8','������' from `doorgen_banks`.`ref_attr` where attr_name = 'case';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'9','����' from `doorgen_banks`.`ref_attr` where attr_name = 'case';

INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'1','�����' from `doorgen_banks`.`ref_attr` where attr_name = 'location_type';
INSERT INTO `doorgen_banks`.`ref_code`(`ref_attr_id`,`code_value`,`code_desc`) select ref_attr_id,'2','������' from `doorgen_banks`.`ref_attr` where attr_name = 'location_type';