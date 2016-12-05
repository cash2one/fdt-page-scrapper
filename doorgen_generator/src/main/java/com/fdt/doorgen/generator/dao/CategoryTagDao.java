package com.fdt.doorgen.generator.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import com.fdt.doorgen.generator.categories.Tag;
import com.fdt.doorgen.key.pooler.dao.DaoCommon;

public class CategoryTagDao extends DaoCommon{

	private static final Logger log = Logger.getLogger(CategoryTagDao.class);

	public CategoryTagDao(Connection connection) {
		super(connection);
	}

	public int deleteAllTags(){
		PreparedStatement prprStatement = null;
		int result = -1;
		try {
			//TODO Insert snippets & page_content tables.
			prprStatement = connection.prepareStatement(""
					+ " DELETE FROM tag");

			result = prprStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(prprStatement != null){
				try {
					prprStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return result;
	}

	public int[] insertTags(List<Tag> tagList, Integer parentIdIfNotSpecified){
		PreparedStatement prprStatement = null;
		int[] result = null;
		try {
			prprStatement = connection.prepareStatement(""
					+ " INSERT INTO tag (parent_id, parent_type, tag_value, tag_value_tmpl, upd_dt) "
					+ " SELECT ?, ?, ?,  ?,  now() "
					+ " ON DUPLICATE KEY UPDATE upd_dt=now()", Statement.RETURN_GENERATED_KEYS);

			for (Tag tag : tagList) {
					/*
					parent_id INT NOT NULL,
					parent_type VARCHAR(50) NOT NULL,
					tag_value VARCHAR(200),
					tag_value_tmpl
					*/
					
					prprStatement.setInt(1, tag.getParentId() == -1? parentIdIfNotSpecified : tag.getParentId());
					prprStatement.setString(2, tag.getParentType().name());
					prprStatement.setString(3, tag.getValue());
					prprStatement.setString(4, tag.getValueTmpl());
					
					prprStatement.addBatch();
			}
			
			result = prprStatement.executeBatch();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(prprStatement != null){
				try {
					prprStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return result;
	}

	/*public ArrayList<String> getKeyList4Polling(HashMap<Integer, Integer> keyMap, Integer minSnpCnt4Key, ContentStrategy strategy) throws ClassNotFoundException, SQLException{
		ArrayList<String> keyList = new ArrayList<String>();

		//Select key for witch snippet count less than 4-6 or page does not exist for current key
		PreparedStatement prStmt = connection.prepareStatement(
						" SELECT DISTINCT k.key_value, 0, k.id FROM door_keys k LEFT JOIN snippets snp ON k.id=snp.key_id WHERE snp.key_id IS NULL AND k.key_value <> '/' " +
						" union  " +
						" SELECT k.key_value, COUNT(1), k.id FROM door_keys k LEFT JOIN snippets snp ON k.id=snp.key_id " +
						" WHERE snp.key_id IS NOT NULL AND k.key_value <> '/' GROUP BY k.key_value, k.id HAVING COUNT(1) < " + minSnpCnt4Key );
		ResultSet rs = prStmt.executeQuery();

		if(rs == null){
			return keyList;
		}

		while(rs.next()){
			if(!"/".equals(rs.getString(1)))
			{
				String key = rs.getString(1);

				if(!keyList.contains(key))
				{
					keyList.add(key);
				}
				//saving count of snippets
				keyMap.put(rs.getInt(3), rs.getInt(2));
			}
		}

		if(strategy.isMixKeys()){
			Collections.shuffle(keyList);
		}

		return keyList;
	}

	public List<List<String>> getKeysWithSnippets4FillPages() throws SQLException{
		String slcQuery = 	" SELECT DISTINCT k.key_value " +
							" FROM door_keys k LEFT JOIN snippets snp ON k.id = snp.key_id LEFT JOIN pages p ON k.id=p.key_id " + 
							" WHERE p.key_id IS NULL AND snp.key_id IS NOT NULL AND k.key_value <> '/' ";
		return getPagesBySelect(slcQuery, new String[]{"key_value"});
	}

	public List<List<String>> getKeysIdByKeyValue(String keyValue) throws SQLException{
		ArrayList<InputParam> inParams = new ArrayList<InputParam>();
		inParams.add(new InputParam(keyValue, Types.VARCHAR));
		String slcQuery = 	" SELECT DISTINCT k.id " +
							" FROM door_keys k " + 
							" WHERE k.key_value = ? ";
		return getPagesBySelect(slcQuery, inParams, new String[]{"id"});
	}

	public List<Integer> getRegionList() throws SQLException{
		String slcQuery = 	" SELECT region_id id" +
							" FROM region r ";
		List<List<String>> ids = getPagesBySelect(slcQuery, new String[]{"id"});

		List<Integer> resultList = new ArrayList<Integer>();
		for(List<String> params : ids){
			resultList.add(Integer.valueOf(params.get(0)));
		}

		return resultList;
	}

	public List<List<String>> getKeysWithoutPagesAndPageContent(int minSnpCnt) throws SQLException{
		String slcQuery = 	" SELECT DISTINCT t.id, t.key_value FROM " +
							" (SELECT k.id, k.key_value, COUNT(1) " +
							" FROM door_keys k LEFT JOIN snippets snp ON k.id=snp.key_id " +
							" WHERE snp.key_id IS NOT NULL AND k.key_value <> '/' GROUP BY k.key_value, k.id HAVING COUNT(1) >= " + minSnpCnt + ") t " +
							" LEFT JOIN pages p ON t.id = p.key_id LEFT JOIN page_content pc ON p.id = pc.page_id " +
							" WHERE (p.id IS NULL OR pc.id IS NULL) AND t.key_value <> '/' ";
		return getPagesBySelect(slcQuery, new String[]{"id", "key_value"});
	}

	public int insertKey(String key){
		PreparedStatement prprStatement = null;
		int result = -1;
		try {
			//TODO Insert snippets & page_content tables.
			prprStatement = connection.prepareStatement(""
					+ " INSERT INTO door_keys (key_value,key_value_latin,upd_dt) "
					//+ " SELECT ?, REPLACE(encodestring(?) COLLATE utf8_unicode_ci ,' ','-'), now()");
					+ " SELECT ?, REPLACE(?,' ','-'), now()"
					+ " ON DUPLICATE KEY UPDATE upd_dt=now()", Statement.RETURN_GENERATED_KEYS);
			prprStatement.setString(1, Utils.getFirstSmblUpper(key));
			prprStatement.setString(2, key.trim());

			prprStatement.executeUpdate();

			if(prprStatement != null){
				ResultSet rs = prprStatement.getGeneratedKeys();
				if (rs != null && rs.next()) {
					result = rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(prprStatement != null){
				try {
					prprStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return result;
	}*/
}