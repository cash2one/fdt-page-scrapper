package com.fdt.doorgen.key.pooler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class KeysDao {
	
	public ArrayList<String> getKeyList4Update(Connection connection, HashMap<String, Integer> keyMap, Integer minSnpCnt4PostPage) throws ClassNotFoundException, SQLException{
		ArrayList<String> keyList = new ArrayList<String>();


		//Select key for witch snippet count less than 4-6 or page does not exist for current key
		PreparedStatement prStmt = connection.prepareStatement(
				" SELECT k.key_value, 0 FROM door_keys k LEFT JOIN pages p ON k.id=p.key_id WHERE p.key_id IS NULL " +
						" union " +
						" SELECT k.key_value, COUNT(k.key_value) " +  
						" FROM door_keys k LEFT JOIN snippets s " + 
						" ON k.id=s.key_id " + 
						" WHERE k.id NOT IN (SELECT k.key_value FROM door_keys k LEFT JOIN pages p ON k.id=p.key_id WHERE p.key_id IS NULL) " + 
						" GROUP BY k.key_value HAVING COUNT(k.key_value) < " + minSnpCnt4PostPage);
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
}