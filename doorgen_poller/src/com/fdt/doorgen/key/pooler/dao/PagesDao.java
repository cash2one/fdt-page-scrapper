package com.fdt.doorgen.key.pooler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.fdt.doorgen.key.pooler.util.DoorUtils;
import com.fdt.scrapper.task.SnippetTask;

public class PagesDao extends DaoCommon{

	private static final Logger log = Logger.getLogger(PagesDao.class);
	
	public PagesDao(Connection connection) {
		super(connection);
	}
	
	public int insertPage(SnippetTask task, String hostName, String globalTitle){
		PreparedStatement prStmt = null;
		Random rnd = new Random();
		ResultSet rs = null;
		int pId = -1;
		try {
			StringBuffer title = new StringBuffer();
			title.append(task.getKeyWordsOrig().substring(0, 1).toUpperCase())
			.append(task.getKeyWordsOrig().substring(1).toLowerCase())
			//TODO Exclude title from sources
			.append(" | ").append(globalTitle).append(" | ")
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
			log.error(e);
			e.printStackTrace();
		}
		finally{
			if(rs != null){
				try {
					rs.close();
				} catch (SQLException e) {
					log.error(e);
					e.printStackTrace();
				}
			}
			if(prStmt != null){
				try {
					prStmt.close();
				} catch (SQLException e) {
					log.error(e);
					e.printStackTrace();
				}
			}
		}

		return pId;
	}
	
	public List<List<String>> getPages4Post(String slcQuery) throws SQLException{
		return getPagesBySelect(slcQuery, new String[]{"key_value","id"});
	}
	
	public List<List<String>> getPages4Post(String slcQuery, List<InputParam> inParams) throws SQLException{
		return getPagesBySelect(slcQuery, inParams, new String[]{"key_value","id"});
	}
	
	public List<List<String>> getPages4UpdateReplaceCntnt(int updDateDiff) throws SQLException{
		String slcQuery = 	" SELECT DISTINCT k.id, k.key_value FROM page_content pc, pages p, door_keys k " + 
							" WHERE pc.page_id = p.id AND p.key_id = k.id AND k.key_value <> '/' AND pc.upd_flg=0 AND (DATEDIFF((now()),pc.post_dt) >  " + updDateDiff +") ";
		return getPagesBySelect(slcQuery, new String[]{"key_value"});
	}
	
	public List<List<String>> getPages4UpdateAppendCntnt(int updDateDiff) throws SQLException{
		String slcQuery = 	" SELECT k.id, k.key_value, pc.id, pc.post_dt <= now(), pc.post_dt > now(), MAX(cd.snippets_index), DATEDIFF((now()),pc.post_dt) " + 
							" FROM door_keys k LEFT JOIN pages p ON p.key_id = k.id LEFT JOIN page_content pc ON pc.page_id = p.id LEFT JOIN content_detail cd ON cd.page_content_id = pc.id " + 
							" WHERE k.key_value <> '/' AND DATEDIFF(now(),pc.post_dt) >= " + updDateDiff +" AND k.id NOT IN  " + 
							" (SELECT DISTINCT k.id " + 
							" 	FROM page_content pc, pages p, door_keys k, content_detail cd  " + 
							" 	WHERE pc.page_id = p.id AND p.key_id = k.id AND cd.page_content_id = pc.id AND k.key_value <> '/'  " + 
							" 	AND DATEDIFF(now(),pc.post_dt) < " + updDateDiff +" AND pc.post_dt < now() + INTERVAL 1 DAY) " + 
							" GROUP BY k.id, k.key_value, pc.id HAVING MAX(cd.snippets_index) < 9 ORDER BY k.id; ";
		return getPagesBySelect(slcQuery, new String[]{"key_value"});
	}
	
	public int getPostedCnt4Day(String slcQuery) throws SQLException{
		ArrayList<InputParam> inParams = new ArrayList<InputParam>();
		return getPostedCnt4Day(slcQuery, inParams);
	}
	
	public int getPostedCnt4Day(String slcQuery, List<InputParam> inParams) throws SQLException
	{
		int count = 0;
		
		List<List<String>> postCnt = getPagesBySelect(slcQuery, inParams, new String[]{"posted_count"});
		
		if(postCnt.size() > 0 && postCnt.get(0).size() > 0 && postCnt.get(0).get(0) != null && !"".equals(postCnt.get(0).get(0)))
		{
			count = Integer.valueOf(postCnt.get(0).get(0));
		}

		return count;
	}
}
