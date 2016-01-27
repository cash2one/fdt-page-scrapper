package com.fdt.doorgen.key.pooler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

	public ArrayList<String> getKeyList4Update(HashMap<String, Integer> keyMap, Integer minSnpCnt4PostPage) throws ClassNotFoundException, SQLException{
		ArrayList<String> keyList = new ArrayList<String>();

		//TODO Fill pages if snippets for keys are exist
		
		
		//Select key for witch snippet count less than 4-6 or page does not exist for current key
		PreparedStatement prStmt = connection.prepareStatement(
				" SELECT DISTINCT t.* FROM (SELECT k.key_value, 0 FROM door_keys k LEFT JOIN pages p ON k.id=p.key_id WHERE p.key_id IS NULL AND k.key_value <> '/' " +
						" union  " +
						" SELECT k.key_value, COUNT(k.key_value)    " +
						" FROM door_keys k LEFT JOIN snippets s  " +
						" ON k.id=s.key_id   " +
						" WHERE k.id NOT IN (SELECT k.id FROM door_keys k LEFT JOIN pages p ON k.id=p.key_id WHERE p.key_id IS NULL) AND k.key_value <> '/'   " +
						" GROUP BY k.key_value HAVING COUNT(k.key_value) < " + minSnpCnt4PostPage +") AS t ");
		ResultSet rs = prStmt.executeQuery();

		if(rs == null){
			return keyList;
		}

		while(rs.next()){
			if(!"/".equals(rs.getString(1))){
				keyList.add(rs.getString(1));
				//saving count of snippets
				keyMap.put(rs.getString(1), rs.getInt(2));
			}
		}

		Collections.shuffle(keyList);

		return keyList;
	}
	
	public List<List<String>> getKeysWithSnippets4FillPages(){
		String slcQuery = 	" SELECT DISTINCT k.key_value " +
							" FROM door_keys k LEFT JOIN snippets snp ON k.id = snp.key_id LEFT JOIN pages p ON k.id=p.key_id " + 
							" WHERE p.key_id IS NULL AND snp.key_id IS NOT NULL AND k.key_value <> '/' ";
		return getPagesBySelect(slcQuery, new String[]{"key_value"});
	}
}