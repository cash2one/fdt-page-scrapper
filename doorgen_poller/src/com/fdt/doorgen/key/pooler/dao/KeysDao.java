package com.fdt.doorgen.key.pooler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

public class KeysDao extends DaoCommon{
	
	private static final Logger log = Logger.getLogger(KeysDao.class);
	
	public KeysDao(Connection connection) {
		super(connection);
	}

	public ArrayList<String> getKeyList4Polling(HashMap<Integer, Integer> keyMap, Integer minSnpCnt4Key) throws ClassNotFoundException, SQLException{
		ArrayList<String> keyList = new ArrayList<String>();

		//TODO Fill pages if snippets for keys are exist
		
		
		//Select key for witch snippet count less than 4-6 or page does not exist for current key
		PreparedStatement prStmt = connection.prepareStatement(
				" SELECT DISTINCT t.* FROM (SELECT k.key_value, 0, k.id FROM door_keys k LEFT JOIN snippets snp ON k.id=snp.key_id WHERE snp.key_id IS NULL AND k.key_value <> '/' " +
						" union  " +
						" SELECT k.key_value, COUNT(1), k.id " +
						" FROM door_keys k LEFT JOIN snippets s  " +
						" ON k.id=s.key_id   " +
						" WHERE k.id NOT IN (SELECT k.id FROM door_keys k LEFT JOIN snippets snp ON k.id=snp.key_id WHERE snp.key_id IS NULL AND k.key_value <> '/' " +
						" GROUP BY k.key_value HAVING COUNT(1) < " + minSnpCnt4Key +") AS t ");
		ResultSet rs = prStmt.executeQuery();

		if(rs == null){
			return keyList;
		}

		while(rs.next()){
			if(!"/".equals(rs.getString(1)))
			{
				String key = rs.getString(1);
				
				if(!keyList.contains(key))
				{
					keyList.add(key);
				}
				//saving count of snippets
				keyMap.put(rs.getInt(3), rs.getInt(2));
			}
		}

		Collections.shuffle(keyList);

		return keyList;
	}
	
	public List<List<String>> getKeysWithSnippets4FillPages() throws SQLException{
		String slcQuery = 	" SELECT DISTINCT k.key_value " +
							" FROM door_keys k LEFT JOIN snippets snp ON k.id = snp.key_id LEFT JOIN pages p ON k.id=p.key_id " + 
							" WHERE p.key_id IS NULL AND snp.key_id IS NOT NULL AND k.key_value <> '/' ";
		return getPagesBySelect(slcQuery, new String[]{"key_value"});
	}
	
	public List<List<String>> getKeysIdByKeyValue(String keyValue) throws SQLException{
		ArrayList<InputParam> inParams = new ArrayList<InputParam>();
		inParams.add(new InputParam(keyValue, Types.VARCHAR));
		String slcQuery = 	" SELECT DISTINCT k.id " +
							" FROM door_keys " + 
							" WHERE k.key_value = ? ";
		return getPagesBySelect(slcQuery, inParams, new String[]{"id"});
	}
	
	public List<Integer> getRegionList() throws SQLException{
		String slcQuery = 	" SELECT region_id id" +
							" FROM region r ";
		List<List<String>> ids = getPagesBySelect(slcQuery, new String[]{"id"});
		
		List<Integer> resultList = new ArrayList<Integer>();
		for(List<String> params : ids){
			resultList.add(Integer.valueOf(params.get(0)));
		}
		
		return resultList;
	}
	
	public List<List<String>> getKeysWithoutPagesAndPageContent() throws SQLException{
		String slcQuery = 	" SELECT DISTINCT k.id, k.key_value, (SELECT snp.description FROM snippets snp WHERE snp.key_id = k.id ORDER BY snp.upd_dt LIMIT 1) description " +
							" FROM door_keys k LEFT JOIN pages p ON k.id = p.key_id LEFT JOIN page_content pc ON p.id = pc.page_id " +
							" WHERE (p.id IS NULL OR pc.id IS NULL) AND k.key_value <> '/' AND k.id IN " +
							" ( " +
							" 	SELECT k.id " +
							"     FROM door_keys k LEFT JOIN snippets snp ON k.id=snp.key_id  " +
							"     WHERE snp.key_id IS NOT NULL AND k.key_value <> '/' " +
							" 	GROUP BY k.key_value HAVING COUNT(1) > 27 " +
							" ) ";
		return getPagesBySelect(slcQuery, new String[]{"id", "key_value"});
	}
}