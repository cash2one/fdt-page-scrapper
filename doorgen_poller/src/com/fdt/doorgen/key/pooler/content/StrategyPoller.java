package com.fdt.doorgen.key.pooler.content;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.fdt.doorgen.key.pooler.dao.KeysDao;

public abstract class StrategyPoller 
{
	public abstract List<List<Integer>> prepareCntntDtlTable(List<List<Integer>> currentDtlTable);
	
	/**
	 * Should be overrided in child classes
	 * 
	 * sql must return "posted_count" column
	 * @return
	 */
	public String getSql4CountPostedNews(){
		return " SELECT DISTINCT COUNT(k.id) posted_count" + 
				" FROM page_content pc, pages p, door_keys k " + 
				" WHERE p.id = pc.page_id AND k.id = p.key_id AND k.key_value <> '/' AND pc.upd_flg=0 AND pc.post_dt > now() AND (pc.post_dt < now() + INTERVAL 1 DAY )";
	}
	
	public String getSqlGetKeys4Post(){
		return " SELECT DISTINCT k.id, k.key_value, pc.id " + 
				" FROM content_detail cd, page_content pc, pages p, door_keys k " + 
				" WHERE cd.page_content_id = pc.id AND pc.page_id = p.id AND p.key_id = k.id AND k.key_value <> '/' AND pc.upd_flg=0 AND (pc.post_dt > now() + INTERVAL 1 DAY) ORDER BY k.id ";
	}
	
	public String getSqlGetKeys4Update(){
		return null;
	}
	
	/**
	 * 0 is Default value for sites without regions
	 * @param keysDao
	 * @return
	 */
	public List<Integer> getRegionList(KeysDao keysDao)  throws SQLException{
		List<Integer> regions = new ArrayList<Integer>();
		regions.add(0);
		return regions;
	}
}
