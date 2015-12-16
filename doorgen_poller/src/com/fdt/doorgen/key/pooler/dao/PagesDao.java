package com.fdt.doorgen.key.pooler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;

import com.fdt.doorgen.key.pooler.util.DoorUtils;
import com.fdt.scrapper.task.SnippetTask;

public class PagesDao {
	
	private Connection connection;
	
	public PagesDao(Connection connection) {
		super();
		this.connection = connection;
	}

	public int insertPage(SnippetTask task, String hostName){
		PreparedStatement prStmt = null;
		Random rnd = new Random();
		ResultSet rs = null;
		int pId = -1;
		try {
			StringBuffer title = new StringBuffer();
			title.append(task.getKeyWordsOrig().substring(0, 1).toUpperCase())
			.append(task.getKeyWordsOrig().substring(1).toLowerCase())
			.append(" | Абсолютный лидер в сфере кредитования | ")
			.append( hostName);

			prStmt = connection.prepareStatement(" INSERT INTO pages (key_id, title, meta_keywords, meta_description, upd_dt) " +
					" SELECT k.id, ?, ?, ?, now()" + 
					" FROM door_keys k " + 
					" WHERE k.key_value = ? " +
					" ON DUPLICATE KEY UPDATE title=?, meta_keywords = ?, meta_description = ? ",
					Statement.RETURN_GENERATED_KEYS);

			prStmt.setString(1, title.toString());
			prStmt.setString(2, title.toString());
			prStmt.setString(3, DoorUtils.cleanString( task.getSnipResult().get(rnd.nextInt(task.getSnipResult().size())).getContent()) );
			prStmt.setString(4, task.getKeyWordsOrig());
			prStmt.setString(5, title.toString());
			prStmt.setString(6, title.toString());
			prStmt.setString(7, title.toString());

			prStmt.executeUpdate();
			
			rs = prStmt.getGeneratedKeys();
			if (rs.next()){
				pId=rs.getInt(1);
			}else{
				pId = -1;
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
			if(prStmt != null){
				try {
					prStmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return pId;
	}
	
	public ArrayList<String> getPages4Post(){
		String slcQuery = 	" SELECT k.id, k.key_value, pc.id " + 
							" FROM page_content pc, pages p, door_keys k " + 
							" WHERE p.id = pc.page_id AND k.id = p.key_id AND k.key_value <> '/' AND pc.upd_flg=0 AND (now() + INTERVAL 1 DAY < pc.post_dt) ";
		return getPagesBySelect(slcQuery, "key_value");
	}
	
	public ArrayList<String> getPages4Update(int updDateDiff){
		String slcQuery = 	" SELECT DISTINCT k.id, k.key_value FROM page_content pc, pages p, door_keys k " + 
							" WHERE pc.page_id = p.id AND p.key_id = k.id AND k.key_value <> '/' AND pc.upd_flg=0 AND (DATEDIFF((now()),pc.post_dt) >  " + updDateDiff +") ";
		return getPagesBySelect(slcQuery, "key_value");
	}
	
	private ArrayList<String> getPagesBySelect(String slcQuery, String extrParamNm)
	{
		ArrayList<String> result = new ArrayList<String>();
		PreparedStatement prpStmt = null;
		ResultSet rs = null;
		try {
			//TODO Insert snippets & page_content tables.
			prpStmt = connection.prepareStatement(slcQuery);

			rs = prpStmt.executeQuery();

			if(rs != null){
				while(rs.next()){
					result.add(rs.getString(extrParamNm));
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
