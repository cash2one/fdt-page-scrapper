package com.fdt.doorgen.key.pooler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.fdt.doorgen.key.pooler.util.DoorUtils;
import com.fdt.scrapper.task.Snippet;
import com.fdt.scrapper.task.SnippetTask;

public class SnippetsDao extends DaoCommon {
	private static final Logger log = Logger.getLogger(SnippetsDao.class);
	
	public SnippetsDao(Connection connection) {
		super(connection);
	}

	public int[] insertSnippets(SnippetTask task, int id){
		PreparedStatement batchStatement = null;
		int[] result = null;
		try {
			//TODO Insert snippets & page_content tables.
			batchStatement = connection.prepareStatement("INSERT INTO snippets (key_id,title,description,upd_dt) " +
					"SELECT k.id, ?, ?, now() " + 
					"FROM door_keys k " + 
					"WHERE k.id = ?");
			for(Snippet snippet : task.getSnipResult()){
				batchStatement.setString(1, DoorUtils.getFirstSmblUpper(DoorUtils.cleanString(snippet.getTitle())));
				batchStatement.setString(2, DoorUtils.cleanString(snippet.getContent()));
				batchStatement.setInt(3, id);
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
	
	/**
	 * Getting all snippets (already used and not used) for appropriated key.
	 * @param keyId
	 * @return
	 */
	public ArrayList<Integer> getAllSnpId4Key(int keyId){
		ArrayList<Integer> result = new ArrayList<Integer>();
		PreparedStatement prpStmt = null;
		ResultSet rs = null;
		try {
			//TODO Insert snippets & page_content tables.
			prpStmt = connection.prepareStatement(" SELECT s.id FROM snippets s, door_keys k " +
					" WHERE k.id = ? AND k.id = s.key_id ");
			prpStmt.setInt(1, keyId);

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
	
	public ArrayList<Integer> getNotUsedSnpId(int keyId){
		ArrayList<Integer> result = new ArrayList<Integer>();
		PreparedStatement prpStmt = null;
		ResultSet rs = null;
		try {
			//TODO Insert snippets & page_content tables.
			prpStmt = connection.prepareStatement(" SELECT s.id FROM snippets s, door_keys k " +
					" WHERE k.id = ? AND k.id = s.key_id AND s.id NOT IN (" + 
					" SELECT DISTINCT snp.id " + 
					" FROM page_content pc LEFT JOIN content_detail cd ON pc.id = cd.page_content_id, snippets snp , pages p, door_keys k " + 
					" WHERE k.id = p.key_id AND snp.id = cd.snippet_id AND pc.page_id = p.id AND k.id = ? ORDER BY snp.id ASC" + ")");
			prpStmt.setInt(1, keyId);
			prpStmt.setInt(2, keyId);

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
