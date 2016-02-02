package com.fdt.doorgen.key.pooler.content.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fdt.doorgen.key.pooler.content.StrategyPoller;
import com.fdt.doorgen.key.pooler.dao.KeysDao;

public class VtopaxMiraRuStrategyPoller extends StrategyPoller {

	public static void main(String... args){
		List<List<Integer>> test = new ArrayList<List<Integer>>();
		ArrayList<Integer> row = new ArrayList<Integer>();
		row.add(7);
		row.add(1);
		test.add(row);
		row = new ArrayList<Integer>();
		row.add(7);
		row.add(0);
		test.add(row);
		test.add(row);
		row = new ArrayList<Integer>();
		row.add(5);
		row.add(1);
		test.add(row);
		VtopaxMiraRuStrategyPoller poller = new VtopaxMiraRuStrategyPoller();
		List<List<Integer>> resutl = poller.prepareCntntDtlTable(test);
		
		for(List<Integer> snglRow : resutl){
			System.out.println(snglRow.get(0) + ":" + snglRow.get(1));
		}
	}
	
	@Override
	public String getSql4CountPostedNews(){
		return " SELECT DISTINCT COUNT(k.id) posted_count" + 
				" FROM page_content pc, pages p, door_keys k, city c, region r " + 
				" WHERE p.id = pc.page_id AND k.id = p.key_id AND k.city_id = c.city_id AND c.region_id = r.region_id AND r.region_id = ? AND k.key_value <> '/' AND pc.upd_flg=0 AND pc.post_dt > now() AND (pc.post_dt < now() + INTERVAL 1 DAY )";
	}
	
	@Override
	public String getSqlGetKeys4Post(){
		return " SELECT DISTINCT k.id, k.key_value, pc.id " + 
				" FROM content_detail cd, page_content pc, pages p, door_keys k, city c, region r " + 
				" WHERE cd.page_content_id = pc.id AND pc.page_id = p.id AND p.key_id = k.id AND k.city_id = c.city_id AND c.region_id = r.region_id AND r.region_id = ? AND k.key_value <> '/' AND pc.upd_flg=0 AND (pc.post_dt > now() + INTERVAL 1 DAY) ORDER BY k.id ";
	}
	
	@Override
	public List<Integer> getRegionList(KeysDao keysDao) throws SQLException{
		return keysDao.getRegionList();
	}
	
	@Override
	public List<List<Integer>> prepareCntntDtlTable( List<List<Integer>> currentDtlTable ) 
	{
		Random rnd = new Random();
		rnd.nextInt();
		List<List<Integer>> dtlTbl = new ArrayList<List<Integer>>();
		
		Map<Integer, Integer> blckDtl = getBlckDtl(currentDtlTable);
		int inCopmBlckNmbr = getIncompleteBlckNmbr(blckDtl);
		
		List<Integer> row = new ArrayList<Integer>();
		if(inCopmBlckNmbr > 0){
			row.add(inCopmBlckNmbr);
			row.add(0);
			dtlTbl.add(row);
		}else{
			//TODO Getting random next block
			List<Integer> emptyBlock = getEmptyBlockList(blckDtl);
			rnd.nextInt();
			row.add(emptyBlock.get(rnd.nextInt(emptyBlock.size())));
			row.add(1);
			dtlTbl.add(row);
		}

		return dtlTbl;
	}
	
	private Map<Integer, Integer> getBlckDtl(List<List<Integer>> currentDtlTable){
		Map<Integer, Integer> blckDtl = new HashMap<Integer, Integer>();
		
		for(List<Integer> list : currentDtlTable){
			if(blckDtl.get(list.get(0)) == null){
				blckDtl.put(list.get(0), 1);
			}else{
				blckDtl.put(list.get(0), blckDtl.get(list.get(0)) + 1);
			}
		}
		
		return blckDtl;
	}
	
	private int getIncompleteBlckNmbr(Map<Integer, Integer> blckDtl)
	{
		for(Integer key : blckDtl.keySet())
		{
			if(blckDtl.get(key) < 3)
			{
				return key;
			}
		}
		
		return -1;
	}
	
	private ArrayList<Integer> getEmptyBlockList(Map<Integer, Integer> blckDtl)
	{
		
		ArrayList<Integer> result = new ArrayList<Integer>();
		for(int i = 1; i <=9; i++){
			result.add(i);
		}
		
		for(Integer key : blckDtl.keySet())
		{
			if(blckDtl.get(key) == 3)
			{
				result.remove(key);
			}
		}
		
		return result;
	}

}
