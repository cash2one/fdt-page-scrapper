package com.fdt.doorgen.key.pooler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.fdt.doorgen.key.pooler.content.ContentStrategy;
import com.fdt.doorgen.key.pooler.util.DoorUtils;

public class PageContentDao extends DaoCommon {
	private static final Logger log = Logger.getLogger(PageContentDao.class);

	private SnippetsDao snipDao;

	public PageContentDao(Connection connection, SnippetsDao snipDao) {
		super(connection);
		this.snipDao = snipDao;
	}


	public int insertPageContent(String key){
		return insertPageContent(key, System.currentTimeMillis() + 10 * DoorUtils.YEAR_MIL_SEC_CNT);
	}

	public int insertPageContent(String key, long postTime){
		PreparedStatement prStmt = null;
		ResultSet rs = null;
		int pcId = -1;
		try {
			prStmt = connection.prepareStatement(" INSERT INTO page_content (page_id, post_dt, upd_flg, upd_dt) " +
					" SELECT p.id, ?, 0, now()" + 
					" FROM door_keys k, pages p " + 
					" WHERE p.key_id = k.id AND k.key_value = ? ",
					Statement.RETURN_GENERATED_KEYS);

			prStmt.setTimestamp(1, new Timestamp(postTime));
			prStmt.setString(2, key);

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
	 * Randomly populate page content
	 * @param key
	 * @return
	 */
	//TODO Add proceudre for population content randomly & adding content to existed content
	public int[] populateContent(String key, int pcIdNew, int pcIdPrev, ContentStrategy strategy){

		ArrayList<Integer> snpIds = new ArrayList<Integer>();
		//idx for already existed content

		if(strategy.isAppendContent()){
			snpIds = snipDao.getNotUsedSnpId(key);
		}else{
			snpIds = snipDao.getAllSnpId4Key(key);
		}

		int[] result = null;

		int snpCnt = snpIds.size();
		PreparedStatement batchStatement = null;
		PreparedStatement copyStatement = null;

		ArrayList<Integer> rndSeq = DoorUtils.getRandomSequense(snpCnt);

		if(pcIdNew < 0){
			return null;
		}

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
					copyStatement.executeUpdate(); // Execute every 1000 items.
				}
			}

			//Insert snippets & page_content tables.
			batchStatement = connection.prepareStatement("INSERT INTO content_detail (page_content_id, snippet_id, snippets_index, main_flg, upd_dt) " +
					" SELECT ?, ?, ?, ?, now() ");


			List<List<Integer>> newCntntDtl = strategy.getSrtgPoller().prepareCntntDtlTable(convertSrtList2IntList(getContentDetailStructure(pcIdNew)));

			for(List<Integer> row : newCntntDtl)
			{

				batchStatement.setInt(1, pcIdNew);
				batchStatement.setInt(2, snpIds.get(rndSeq.remove(0)));
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

	/**
	 * Получаем количество новостей, которые будут запощены в течении суток со дня запуска
	 * @return
	 */
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

	/**
	 * Получаем список новостей, которые надо будет обновить в течении суток
	 * @return
	 */
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


	public int postPage(String key, long postTime)
	{
		PreparedStatement prStmt = null;
		int count = 0;
		try {
			prStmt = connection.prepareStatement(
					" UPDATE page_content pc SET pc.post_dt=FROM_UNIXTIME(?/1000) " +
					" WHERE pc.page_id = (SELECT p.id FROM door_keys k, pages p WHERE p.key_id=k.id AND k.key_value = ?) ");

			prStmt.setLong(1, postTime);
			prStmt.setString(2, key);

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

	public void updPagesAsUpdated(String key)
	{
		PreparedStatement prStmt = null;
		try {
			prStmt = connection.prepareStatement(
					" UPDATE page_content pc SET pc.upd_flg=1, pc.post_dt=pc.post_dt " +
					" WHERE pc.page_id = (SELECT p.id FROM door_keys k, pages p WHERE p.key_id=k.id AND k.key_value = ?) ");

			prStmt.setString(1, key);

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
					" DELETE FROM page_content WHERE post_dt < now() AND id IN ( " + 
							" 	SELECT DISTINCT t2.id FROM   " + 
							" 	(SELECT t1.page_id, t1.post_dt, t1.id FROM page_content t1 WHERE t1.post_dt < now()) AS t2,  " + 
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

	/**
	 * Получаем последний id для page_content
	 * @return
	 */
	public int getLastPageContentId(String key)
	{
		PreparedStatement prpStmt = null;
		ResultSet rs = null;
		int pcId = -1;

		try {
			//TODO Insert snippets & page_content tables.
			prpStmt = connection.prepareStatement( " " +
					" SELECT DISTINCT pc.id FROM page_content pc, pages p, door_keys k " +
					" WHERE pc.page_id = p.id AND p.key_id = k.id AND k.key_value=? ORDER BY pc.upd_dt DESC ");

			prpStmt.setString(1, key);

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

	/**
	 * Получаем максимальный индеск для content_detail
	 * @return
	 */
	public int getSnipIdx4PageCntnt(int pcId)
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

	public List<List<String>> getContentDetailStructure(int pcId){
		String slcQuery = 	" SELECT cd.snippets_index, cd.main_flg " +
				" FROM content_detail cd " +
				" WHERE cd.page_content_id = 2 ORDER BY cd.id ";
		List<List<String>> result = getPagesBySelect(slcQuery, new String[]{"max_snip_idx"});
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
	}
}
