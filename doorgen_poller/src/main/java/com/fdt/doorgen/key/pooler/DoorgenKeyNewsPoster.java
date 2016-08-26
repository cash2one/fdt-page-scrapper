package com.fdt.doorgen.key.pooler;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.scrapper.task.ConfigManager;

/**
 *
 * @author Administrator
 */
public class DoorgenKeyNewsPoster{

	private static final Logger log = Logger.getLogger(DoorgenKeyNewsPoster.class);

	private static final String CONNECTION_STRING_LABEL = "connection_string";
	private static final String HOST_NAME_LABEL = "host_name";
	private static final String POST_TIMETABLE_LABEL = "time_table";

	private String connectionString = null;
	private String hostName = null;
	private File timeTableFile = null;

	Random rnd = new Random();

	private Connection connection;

	/**
	 * args[0] - path to config file
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception{
		System.out.println("Working Directory = " +  System.getProperty("user.dir")); 
		System.out.println(args[0]);

		DOMConfigurator.configure("log4j.xml");

		DoorgenKeyNewsPoster taskRunner = null;
		try {
			taskRunner = new DoorgenKeyNewsPoster();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		taskRunner.executeWrapper();
	}

	private void executeWrapper() throws Exception{
		execute();
	}

	public DoorgenKeyNewsPoster() throws Exception{

		this.hostName = ConfigManager.getInstance().getProperty(HOST_NAME_LABEL);
		this.connectionString = ConfigManager.getInstance().getProperty(CONNECTION_STRING_LABEL);

		connection = getConnection();
	}

	public void execute(){
		try{
			//TODO Получаем количество уже запощенных записей и проверяем по таблице-рассписанию, надо ли нам постить ещё

			//TODO Если надо постить, то постим новые записи

		}finally{
			if(connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					log.error(e);
				}
			}
		}
	}

	private Connection getConnection() throws SQLException, ClassNotFoundException{
		Class.forName("com.mysql.jdbc.Driver");
		//TODO Setup the connection with the DB
		Connection connection = (Connection) DriverManager.getConnection(this.connectionString);

		return connection;
	}

	/**
	 * Получаем количество записей, которые будут запощены(т.е. у которых дата уже изменена) в течении суток.
	 * @param key
	 * @return
	 */
	private int getDayPostedPageCount(){
		PreparedStatement prpStmt = null;
		ResultSet rs = null;
		int result = -1;
		try {
			//TODO Insert snippets & page_content tables.
			prpStmt = connection.prepareStatement(
					" SELECT count(p.id) row_count " +
							" FROM (SELECT DISTINCT p.id FROM door_keys k, pages p LEFT JOIN page_content pc ON p.id=pc.page_id " +
					" WHERE k.id = p.key_id AND p.post_dt > now() AND p.post_dt < (now() + INTERVAL 1 DAY) AND pc.page_id IS NOT NULL) as t");

			rs = prpStmt.executeQuery();

			if(rs != null){
				result = rs.getInt(1);
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
	 * Постим новости, обновляя поле post_dt в таблице pages
	 * @param key
	 * @return
	 */
	private int postNews(int postCnt){
		PreparedStatement prpStmt = null;
		ResultSet rs = null;
		int result = -1;
		try {
			//TODO Insert snippets & page_content tables.
			prpStmt = connection.prepareStatement(
					" SELECT DISTINCT p.id FROM door_keys k, pages p LEFT JOIN page_content pc ON p.id=pc.page_id " +
					" WHERE k.id = p.key_id AND p.post_dt > (now() + INTERVAL 1 DAY) AND pc.page_id IS NOT NULL ");

			rs = prpStmt.executeQuery();

			if(rs != null){
				while(rs.next()){
					int id = rs.getInt(1);
					updatePostDt(id);
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
	 * Постим новости, обновляя поле post_dt в таблице pages
	 * @param key
	 * @return
	 */
	private int updatePostDt(int pageId){
		PreparedStatement prpStmt = null;
		int result = -1;
		try {
			//TODO Insert snippets & page_content tables.
			prpStmt = connection.prepareStatement(
					" UPDATE pages SET post_dt=? WHERE id = ? ");

			result = prpStmt.executeUpdate();
			if(result < 1){
				log.error("Date for news was not updated: page id: " + pageId);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
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
