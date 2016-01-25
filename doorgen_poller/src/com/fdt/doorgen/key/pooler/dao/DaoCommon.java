package com.fdt.doorgen.key.pooler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class DaoCommon {
	private static final Logger log = Logger.getLogger(DaoCommon.class);

	protected Connection connection;

	public DaoCommon(Connection connection) {
		super();
		this.connection = connection;
	}
	
	protected List<List<String>> getPagesBySelect(String slcQuery, String[] extrParamNmArr)
	{
		List<List<String>> result = new ArrayList<List<String>>();
		PreparedStatement prpStmt = null;
		ResultSet rs = null;
		try {
			//TODO Insert snippets & page_content tables.
			prpStmt = connection.prepareStatement(slcQuery);

			rs = prpStmt.executeQuery();

			if(rs != null){
				while(rs.next()){
					ArrayList<String> row = new ArrayList<String>();
					for(String extrParamNm : extrParamNmArr){
						row.add(rs.getString(extrParamNm));
					}
					result.add(row);
				}
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

			if(prpStmt != null){
				try {
					prpStmt.close();
				} catch (SQLException e) {
					log.error(e);
					e.printStackTrace();
				}
			}
		}

		return result;
	}
}
