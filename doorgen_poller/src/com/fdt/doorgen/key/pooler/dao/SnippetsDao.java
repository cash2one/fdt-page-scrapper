package com.fdt.doorgen.key.pooler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.fdt.doorgen.key.pooler.util.DoorUtils;
import com.fdt.scrapper.task.Snippet;
import com.fdt.scrapper.task.SnippetTask;

public class SnippetsDao {
	
	private Connection connection;
	
	public SnippetsDao(Connection connection) {
		super();
		this.connection = connection;
	}

	public int[] insertSnippets(SnippetTask task){
		PreparedStatement batchStatement = null;
		int[] result = null;
		try {
			//TODO Insert snippets & page_content tables.
			batchStatement = connection.prepareStatement("INSERT INTO snippets (key_id,title,description,upd_dt) " +
					"SELECT k.id, ?, ?, now() " + 
					"FROM door_keys k " + 
					"WHERE k.key_value = ?");
			for(Snippet snippet : task.getSnipResult()){
				batchStatement.setString(1, DoorUtils.getFirstSmblUpper(DoorUtils.cleanString(snippet.getTitle())));
				batchStatement.setString(2, DoorUtils.cleanString(snippet.getContent()));
				batchStatement.setString(3, task.getKeyWordsOrig());
				batchStatement.addBatch();
			}

			if(batchStatement != null){
				result = batchStatement.executeBatch(); // Execute every 1000 items.
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(batchStatement != null){
				try {
					batchStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return result;
	}
	
	public ArrayList<Integer> getInsertedSnpId(String key){
		ArrayList<Integer> result = new ArrayList<Integer>();
		PreparedStatement prpStmt = null;
		ResultSet rs = null;
		try {
			//TODO Insert snippets & page_content tables.
			prpStmt = connection.prepareStatement(" SELECT s.id FROM snippets s, door_keys k " +
					" WHERE k.key_value = ? AND k.id = s.key_id ");
			prpStmt.setString(1, key);

			rs = prpStmt.executeQuery();

			if(rs != null){
				while(rs.next()){
					result.add(rs.getInt(1));
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(rs != null){
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if(prpStmt != null){
				try {
					prpStmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return result;
	}
}
