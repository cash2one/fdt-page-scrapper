package com.fdt.doorgen.key.pooler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Random;

import org.apache.log4j.Logger;

import com.fdt.doorgen.key.pooler.content.ContentStrategy;
import com.fdt.doorgen.key.pooler.util.DoorUtils;
import com.fdt.scrapper.task.SnippetTask;

public class PageContentDao extends DaoCommon {
	private static final Logger log = Logger.getLogger(PageContentDao.class);
	
	private SnippetsDao snipDao;
	
	public PageContentDao(Connection connection, SnippetsDao snipDao) {
		super(connection);
		this.snipDao = snipDao;
	}
	
	
	public int insertPageContent(String key){
		return insertPageContent(key, System.currentTimeMillis() + DoorUtils.YEAR_MIL_SEC_CNT);
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
	public int[] populateContent(String key, int pcId, ContentStrategy strategy){
		Random rnd = new Random();
		
		ArrayList<Integer> snpIds = new ArrayList<Integer>();
		//idx for already existed content
		int lastMaxSnpIdx = 0;
		
		if(strategy.isAppendContent()){
			snpIds = snipDao.getNotUsedSnpId(key);
		}else{
			snpIds = snipDao.getAllSnpId4Key(key);
		}
		
		int[] result = null;

		int snpCnt = snpIds.size();
		PreparedStatement batchStatement = null;

		int rndBatchSnpCnt[] = DoorUtils.getRndBlocksSize(strategy.getMnBlockCnt(), strategy.getBlockSize());
		//если количество сниппетов не достаточно, то контент не будет сгенерирован
		if(DoorUtils.arraySum(rndBatchSnpCnt) > snpCnt){
			return new int[]{};
		}

		ArrayList<Integer> rndSeq = DoorUtils.getRandomSequense(snpCnt);
		
		if(pcId < 0){
			return null;
		}

		try {
			//TODO Insert snippets & page_content tables.
			batchStatement = connection.prepareStatement("INSERT INTO content_detail (page_content_id, snippet_id, snippets_index, main_flg, upd_dt) " +
					" SELECT ?, ?, ?, ?, now() " + 
					" FROM pages p, door_keys k" + 
					" WHERE p.key_id = k.id AND k.key_value = ? ");


			//TODO get last max snippet index
			if(strategy.isAppendContent()){
				lastMaxSnpIdx = getSnipIdx4PageCntnt(pcId) % strategy.getBlockSize();
			}else{
				lastMaxSnpIdx = 0;
			}
			
			for(int i = 0; i < strategy.getMnBlockCnt(); i++)
			{
				for(int j = 1; j <= rndBatchSnpCnt[i]; j++)
				{
					//get discription count
					int descCnt = 1+rnd.nextInt(strategy.getMaxDescCnt());
					boolean ifMainNotInserted = true;

					for(int k = 0; k < descCnt; k++)
					{
						batchStatement.setInt(1, pcId);
						batchStatement.setInt(2, snpIds.get(rndSeq.remove(0)));
						batchStatement.setInt(3, i*strategy.getMnBlockCnt() + j + lastMaxSnpIdx);
						batchStatement.setBoolean(4, ifMainNotInserted || false);
						batchStatement.setString(5, key);
						ifMainNotInserted = false;
						batchStatement.addBatch();
					}
				}
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
	 * ѕолучаем количество новостей, которые будут запощены в течении суток со дн€ запуска
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
	 * ѕолучаем список новостей, которые надо будет обновить в течении суток
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
							" DELETE FROM page_content WHERE id IN " +
							" (SELECT t2.id FROM  " +
							" (SELECT t1.* FROM page_content t1) AS t2, " +
							" (SELECT pc.page_id, MIN(pc.post_dt) min_post_dt FROM  page_content pc WHERE pc.post_dt < now() GROUP BY pc.page_id HAVING count(pc.page_id) > 1) AS t3 " +
							" WHERE t2.page_id = t3.page_id AND t2.post_dt = t3.min_post_dt) "	
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
	 * ѕолучаем последний id дл€ page_content
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
				while(rs.next()){
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
	 * ѕолучаем максимальный индеск дл€ content_detail
	 * @return
	 */
	public int getSnipIdx4PageCntnt(int pcId)
	{
			String slcQuery = 	" SELECT MAX(cd.snippets_index) max_snip_idx, pc.id " +
								" FROM page_content pc, content_detail cd " + 
								" WHERE cd.page_content_id = pc.id AND pc.id = "+ pcId + " " +
								" GROUP BY pc.id";
			return Integer.valueOf(getPagesBySelect(slcQuery, "max_snip_idx").get(0));
	}
}
