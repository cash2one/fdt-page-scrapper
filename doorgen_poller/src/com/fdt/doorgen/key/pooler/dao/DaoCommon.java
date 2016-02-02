package com.fdt.doorgen.key.pooler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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
	
	public Connection getConnection(){
		return connection;
	}
	
	public List<List<String>> getPagesBySelect(String slcQuery, String[] extrParamNmArr) throws SQLException{
		ArrayList<InputParam> inParams = new ArrayList<InputParam>();
		return getPagesBySelect(slcQuery,inParams, extrParamNmArr);
	}
	
	public List<List<String>> getPagesBySelect(String slcQuery, List<InputParam> inParams, String[] extrParamNmArr) throws SQLException
	{
		List<List<String>> result = new ArrayList<List<String>>();
		PreparedStatement prpStmt = null;
		ResultSet rs = null;
		try {
			//TODO Insert snippets & page_content tables.
			prpStmt = connection.prepareStatement(slcQuery);
			
			prpStmt = prepateInputs(prpStmt, inParams);

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
	
	private PreparedStatement prepateInputs(PreparedStatement prpStmt, List<InputParam> inParams) throws SQLException{
		int index = 1;
		for(InputParam param : inParams){
			if(param.getType() == Types.INTEGER){
				prpStmt.setInt(index++, (Integer)param.getValue());
			}else if(param.getType() == Types.VARCHAR){
				prpStmt.setString(index++, (String)param.getValue());
			}else{
				prpStmt.setString(index++, (String)param.getValue());
			}
		}
		
		return prpStmt;
	}
}
