package com.fdt.doorgen.key.pooler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

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
	
	public int[] insertSnippets(List<String> sentenceses, int id){
		PreparedStatement batchStatement = null;
		int[] result = null;
		try {
			//TODO Insert snippets & page_content tables.
			batchStatement = connection.prepareStatement("INSERT INTO snippets (key_id,title,description,upd_dt) " +
					"SELECT k.id, ?, ?, now() " + 
					"FROM door_keys k " + 
					"WHERE k.id = ?");
			for(String sentence : sentenceses){
				batchStatement.setString(1, DoorUtils.getFirstSmblUpper(sentence));
				batchStatement.setString(2, DoorUtils.cleanString(sentence));
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
	
	public int deleteAllSnippets4Key(int id){
		PreparedStatement prpStatement = null;
		int result = -1;
		try {
			//TODO Insert snippets & page_content tables.
			prpStatement = connection.prepareStatement("DELETE FROM snippets WHERE key_id = ?");
			
			prpStatement.setInt(1, id);

			if(prpStatement != null){
				result = prpStatement.executeUpdate();
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(prpStatement != null){
				try {
					prpStatement.close();
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
	
	public List<List<String>> getSnipDescrByKeyId(int keyId) throws SQLException{
		ArrayList<InputParam> inParams = new ArrayList<InputParam>();
		inParams.add(new InputParam(keyId, Types.INTEGER));
		String slcQuery = 	" SELECT snp.description FROM snippets snp WHERE snp.key_id = ?";
		return getPagesBySelect(slcQuery, inParams, new String[]{"description"});
	}
	
	public List<List<String>> getMinSnipCount4Key(String keyValue) throws SQLException
	{
		ArrayList<InputParam> inParams = new ArrayList<InputParam>();
		inParams.add(new InputParam(keyValue, Types.VARCHAR));
		inParams.add(new InputParam(keyValue, Types.VARCHAR));
		
		String slcQuery = 	" SELECT MIN(snp_count) snp_count FROM " +
							" (SELECT k.id, k.key_value, COUNT(1) snp_count FROM door_keys k LEFT JOIN snippets snp ON snp.key_id = k.id " +
							" WHERE snp.id IS NOT NULL AND k.key_value = ? " +
							" GROUP BY k.id, k.key_value " +
							" UNION " +
							" SELECT k.id, k.key_value, 0 FROM door_keys k LEFT JOIN snippets snp ON snp.key_id = k.id " +
							" WHERE snp.id IS NULL AND k.key_value = ? " +
							" GROUP BY k.id, k.key_value) t ";
		return getPagesBySelect(slcQuery, inParams, new String[]{"snp_count"});
	}
}