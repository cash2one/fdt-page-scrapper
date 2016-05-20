package com.fdt.doorgen.key.pooler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


public class ArticleContentDao extends DaoCommon {
	private static final Logger log = Logger.getLogger(ArticleContentDao.class);

	public ArticleContentDao(Connection connection) {
		super(connection);
	}
	
	public int postArticle(int tmplId, String text, boolean updFlg, long date){
		PreparedStatement prStmt = null;
		ResultSet rs = null;
		int pcId = -1;
		try {
			prStmt = connection.prepareStatement(
					" INSERT INTO article_content ( tmpl_id, text, upd_flg, post_dt, upd_dt) " +
					" VALUES( ?, ?, ?, FROM_UNIXTIME(?/1000), now() )",
					Statement.RETURN_GENERATED_KEYS);

			prStmt.setInt(1, tmplId);
			prStmt.setString(2, text);
			prStmt.setBoolean(3, updFlg);
			prStmt.setLong(4, date);

			prStmt.executeUpdate();

			rs = prStmt.getGeneratedKeys();
			if (rs.next()){
				pcId=rs.getInt(1);
			}else{
				pcId = -1;
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

		return pcId;
	}
	
	/**
	 * Delete deprecated page content
	 * @param key
	 * @return
	 */
	public int deleteDeprecatedPageContent(){
		PreparedStatement prStatement = null;
		int count = -1;

		try {
			prStatement = connection.prepareStatement(
							" DELETE FROM article_content WHERE post_dt < now() AND artcl_id IN ( " +
							" SELECT DISTINCT t2.artcl_id FROM " +
							" (SELECT ac.artcl_id, ac.post_dt FROM article_content ac WHERE ac.post_dt < now()) AS t2, " +
							" (SELECT ac.artcl_id, MIN(ac.post_dt) min_post_dt FROM  article_content ac WHERE ac.post_dt < now() GROUP BY ac.tmpl_id HAVING count(ac.artcl_id) > 1) AS t3 " +
							" WHERE t2.artcl_id = t3.artcl_id AND t2.post_dt = t3.min_post_dt ) "
			);
			count = prStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(prStatement != null){
				try {
					prStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return count;
	}
	
	public long getMaxPostDate() throws SQLException{
		List<InputParam> inParams = new ArrayList<InputParam>();
		String slcQuery = 	" SELECT UNIX_TIMESTAMP(MAX(ac.post_dt)) max_post_date FROM article_content ac ";
		List<List<String>> result = getPagesBySelect(slcQuery, inParams, new String[]{"max_post_date"});
		return Long.valueOf(result.get(0).get(0));
	}
	
	public List<List<String>> getArticles4Update(int updateInterval) throws SQLException
	{
		List<InputParam> inParams = new ArrayList<InputParam>();
		inParams.add(new InputParam(updateInterval, Types.INTEGER));
		String slcQuery = 	" SELECT DISTINCT at.tmpl_id tmpl_id, at.text text FROM article_tmpl at LEFT JOIN article_content ac ON at.tmpl_id = ac.tmpl_id " +
							" WHERE DATEDIFF(now(),ac.post_dt) >= ? ";
		List<List<String>> result = getPagesBySelect(slcQuery, inParams, new String[]{"tmpl_id", "text"});
		return result;
	}

	/** 
	 * Randomly populate page content
	 * @param keyId
	 * @return
	 *//*
	//TODO Add proceudre for population content randomly & adding content to existed content
	public int[] populateContent(int keyId, int pcIdNew, int pcIdPrev, ContentStrategy strategy){

		ArrayList<Integer> snpIds = new ArrayList<Integer>();
		//idx for already existed content

		if(strategy.isAppendContent()){
			snpIds = snipDao.getNotUsedSnpId(keyId);
		}else{
			snpIds = snipDao.getAllSnpId4Key(keyId);
		}

		int[] result = null;

		int snpCnt = snpIds.size();
		PreparedStatement batchStatement = null;
		PreparedStatement copyStatement = null;

		ArrayList<Integer> rndSeq = DoorUtils.getRandomSequense(snpCnt);

		if(pcIdNew < 0){
			return null;
		}

		int insertCount = -1;
		
		try {
			if(strategy.isAppendContent()){
				//TODO Copy previous snippets values to new pcId
				//Insert snippets & page_content tables.
				copyStatement = connection.prepareStatement("INSERT INTO content_detail (page_content_id, snippet_id, snippets_index, main_flg, upd_dt) " +
						" SELECT ?, cd.snippet_id, cd.snippets_index, cd.main_flg, now() " + 
						" FROM content_detail cd" + 
						" WHERE cd.page_content_id = ? ORDER BY cd.id");

				copyStatement.setInt(1, pcIdNew);
				copyStatement.setInt(2, pcIdPrev);

				if(copyStatement != null){
					insertCount = copyStatement.executeUpdate(); // Execute every 1000 items.
				}
			}

			//Insert snippets & page_content tables.
			batchStatement = connection.prepareStatement("INSERT INTO content_detail (page_content_id, snippet_id, snippets_index, main_flg, upd_dt) " +
					" SELECT ?, ?, ?, ?, now() ");


			List<List<Integer>> newCntntDtl = strategy.getSrtgPoller().prepareCntntDtlTable(convertSrtList2IntList(getContentDetailStructure(pcIdNew)));

			for(int i = 0; i < newCntntDtl.size() && i < rndSeq.size(); i++)
			{
				List<Integer> row = newCntntDtl.get(i);
				batchStatement.setInt(1, pcIdNew);
				batchStatement.setInt(2, snpIds.get(rndSeq.get(i)));
				batchStatement.setInt(3, row.get(0));
				batchStatement.setBoolean(4, row.get(1) == 1);
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

			if(copyStatement != null){
				try {
					copyStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return result;
	}

	*//**
	 * Получаем количество новостей, которые будут запощены в течении суток со дня запуска
	 * @return
	 *//*
	public ArrayList<String> getPagesCntPostedInDay()
	{
		ArrayList<String> result = new ArrayList<String>();
		PreparedStatement prpStmt = null;
		ResultSet rs = null;
		try {
			//TODO Insert snippets & page_content tables.
			prpStmt = connection.prepareStatement( " " +
					" SELECT k.id, k.key_value, pc.id " + 
					" FROM page_content pc, pages p, door_keys k " + 
					" WHERE p.id = pc.page_id AND k.id = p.key_id AND k.key_value <> '/' AND pc.upd_flg=0 ");

			rs = prpStmt.executeQuery();

			if(rs != null){
				while(rs.next()){
					result.add(rs.getString("key_value"));
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

	*//**
	 * Получаем список новостей, которые надо будет обновить в течении суток
	 * @return
	 *//*
	public ArrayList<String> getPages4Update(int dayAfterPost)
	{
		ArrayList<String> result = new ArrayList<String>();
		PreparedStatement prpStmt = null;
		ResultSet rs = null;
		try {
			//TODO Insert snippets & page_content tables.
			prpStmt = connection.prepareStatement( " " +
					" SELECT DISTINCT k.id, k.key_value FROM page_content pc, pages p, door_keys k " + 
					" WHERE pc.page_id = p.id AND p.key_id = k.id AND k.key_value <> '/' AND pc.upd_flg=0 AND (DATEDIFF((now()),pc.post_dt) > ?) ");

			prpStmt.setInt(1, dayAfterPost);

			rs = prpStmt.executeQuery();

			if(rs != null){
				while(rs.next()){
					result.add(rs.getString("key_value"));
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


	public int postPage(int key, long postTime)
	{
		PreparedStatement prStmt = null;
		int count = 0;
		try {
			prStmt = connection.prepareStatement(
					" UPDATE page_content pc SET pc.post_dt=FROM_UNIXTIME(?/1000) " +
					" WHERE pc.page_id = (SELECT p.id FROM door_keys k, pages p WHERE p.key_id=k.id AND k.id = ?) ");

			prStmt.setLong(1, postTime);
			prStmt.setInt(2, key);

			count = prStmt.executeUpdate();

		} catch (SQLException e) {
			log.error("Error for key value: " + key);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(prStmt != null){
				try {
					prStmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return count;
	}

	public void updPagesAsUpdated(int key)
	{
		PreparedStatement prStmt = null;
		try {
			prStmt = connection.prepareStatement(
					" UPDATE page_content pc SET pc.upd_flg=1, pc.post_dt=pc.post_dt " +
					" WHERE pc.page_id = (SELECT p.id FROM door_keys k, pages p WHERE p.key_id=k.id AND k.id = ?) ");

			prStmt.setInt(1, key);

			prStmt.executeUpdate();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(prStmt != null){
				try {
					prStmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	*//**
	 * Delete deprecated page content
	 * @param key
	 * @return
	 *//*
	public int deleteDeprecatedPageContent(){
		PreparedStatement prStatement = null;
		int count = -1;

		try {
			prStatement = connection.prepareStatement(
					" DELETE FROM page_content WHERE post_dt < now() AND id IN ( " + 
					" 	SELECT DISTINCT t2.id FROM   " + 
					" 	(SELECT pc.page_id, pc.post_dt, pc.id FROM page_content pc WHERE pc.post_dt < now()) AS t2,  " + 
					" 	(SELECT pc.page_id, MIN(pc.post_dt) min_post_dt FROM  page_content pc WHERE pc.post_dt < now() GROUP BY pc.page_id HAVING count(pc.page_id) > 1) AS t3  " + 
					" 	WHERE t2.page_id = t3.page_id AND t2.post_dt = t3.min_post_dt ) "	
					);
			count = prStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(prStatement != null){
				try {
					prStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return count;
	}
	
	*//**
	 * Delete page content
	 * @param key
	 * @return
	 *//*
	public int deletePageContent(int id){
		PreparedStatement prStatement = null;
		int count = -1;

		try {
			prStatement = connection.prepareStatement(" DELETE FROM page_content WHERE id = ? ");
			prStatement.setInt(1, id);
			count = prStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(prStatement != null){
				try {
					prStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return count;
	}

	*//**
	 * Получаем последний id для page_content
	 * @return
	 *//*
	public int getLastPageContentId(int keyId)
	{
		PreparedStatement prpStmt = null;
		ResultSet rs = null;
		int pcId = -1;

		try {
			//TODO Insert snippets & page_content tables.
			prpStmt = connection.prepareStatement( " " +
					" SELECT DISTINCT pc.id FROM page_content pc, pages p, door_keys k " +
					" WHERE pc.page_id = p.id AND p.key_id = k.id AND k.id=? ORDER BY pc.upd_dt DESC ");

			prpStmt.setInt(1, keyId);

			rs = prpStmt.executeQuery();

			if(rs != null){
				if(rs.next()){
					pcId = rs.getInt("id");
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

		return pcId;
	}

	*//**
	 * Получаем максимальный индеск для content_detail
	 * @return
	 * @throws SQLException 
	 *//*
	public int getSnipIdx4PageCntnt(int pcId) throws SQLException
	{
		String slcQuery = 	" SELECT MAX(cd.snippets_index) max_snip_idx, pc.id " +
				" FROM page_content pc, content_detail cd " + 
				" WHERE cd.page_content_id = pc.id AND pc.id = "+ pcId + " " +
				" GROUP BY pc.id";
		List<List<String>> arrayRes = getPagesBySelect(slcQuery, new String[]{"max_snip_idx"});

		if(arrayRes != null && arrayRes.size() > 0)
		{
			List<String> result = arrayRes.get(0);
			if(result != null && result.size() > 0)
			{
				return Integer.valueOf(result.get(0));
			}
		}

		return 0;
	}

	public List<List<String>> getContentDetailStructure(int pcId) throws SQLException{
		List<InputParam> inParams = new ArrayList<InputParam>();
		inParams.add(new InputParam(pcId, Types.INTEGER));
		String slcQuery = 	" SELECT cd.snippets_index, cd.main_flg " +
				" FROM content_detail cd " +
				" WHERE cd.page_content_id = ? ORDER BY cd.id ";
		List<List<String>> result = getPagesBySelect(slcQuery, inParams, new String[]{"snippets_index","main_flg"});
		return result;
	}

	private List<List<Integer>> convertSrtList2IntList(List<List<String>> inList)
	{
		List<List<Integer>> resList = new ArrayList<List<Integer>>();

		for(List<String> strList : inList)
		{
			List<Integer> newIntRowList = new ArrayList<Integer>();

			for(String value : strList)
			{
				newIntRowList.add(Integer.valueOf(value));
			}

			resList.add(newIntRowList);
		}
		return resList;
	}*/
}
