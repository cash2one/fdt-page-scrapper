package com.fdt.doorgen.generator.categories.utils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;

import com.fdt.doorgen.generator.DoorgenGeneratorRunner;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.utils.Utils;

@Configuration
//@EnableAutoConfiguration
@ComponentScan(basePackages = "com.fdt", excludeFilters={
		@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value = com.fdt.scrapper.proxy.ProxyFactory.class),
		@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value = com.fdt.doorgen.key.pooler.articles.ArticlesPosterUpdaterRunner.class)
})
@PropertySource(value="file:${config.file}",ignoreResourceNotFound = true)
public class CategoriesUtils {
	
	private static final Random rnd = new Random();

	public static void main(String[] args){
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		ApplicationContext ctx = SpringApplication.run(DoorgenGeneratorRunner.class, args);

		System.out.println("Let's inspect the beans provided by Spring Boot:");

		String[] beanNames = ctx.getBeanDefinitionNames();
		Arrays.sort(beanNames);
		for (String beanName : beanNames) {
			System.out.println(beanName);
		}

		DOMConfigurator.configure("log4j.xml");
		CategoriesUtils taskRunner = ctx.getBean(CategoriesUtils.class);

		try {
			taskRunner.generateCategoriesStructure(new File("_gen_categories"));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public CategoriesUtils(){
		super();
	}

	public void generateCategoriesStructure(File rootFolder) throws ClassNotFoundException, SQLException, IOException{

		Connection connection = null;
		ResultSet rs = null;
		ResultSet rsTags = null;
		PreparedStatement prStmt = null;
		PreparedStatement prStmtTags = null;
		
/*		for(File dir : rootFolder.listFiles()){
			if(dir.isDirectory()){
				boolean delResult = dir.delete();
				delResult = delResult;
			}
		}*/
		
		try{
			connection = getConnection();
			ArrayList<String> keyList = new ArrayList<String>();

			//Select key for witch snippet count less than 4-6 or page does not exist for current key
			prStmt = connection.prepareStatement(
					" SELECT r.region_name, r.region_name_latin, r.abbr FROM region r WHERE 1 ");
			rs = prStmt.executeQuery();

			while(rs.next()){
				String regionName = rs.getString("region_name");
				String regionNameLatin = rs.getString("region_name_latin");
				String regionAbbr= rs.getString("abbr");
				File categoryFldr = new File(rootFolder,regionName + "~" + regionAbbr);
				categoryFldr.mkdir();

				new File(categoryFldr,"main.txt").createNewFile();
				
				File tagsFile = new File(categoryFldr,"tags.txt");
				tagsFile.createNewFile();

				//TODO Load tags
				
				//item_name, item_name_latin, category_id, geo_placename, geo_position, geo_category, ICBM, lat, lng, zip_code, country, tmpl_text, generated_text, ratingCount, private static final Random rnd = new Random();, voteCount
				silentCLose(null, rsTags, prStmtTags);
				prStmtTags = connection.prepareStatement(
						" SELECT i.region_id, i.city_name city_name, i.city_name_latin, i.geo_placename, i.geo_position, i.geo_region, i.ICBM, i.lat, i.lng, i.zip_code, i.country FROM city i, region r WHERE i.region_id = r.region_id AND r.region_name= ? ");
				prStmtTags.setString(1, regionName);
				rsTags = prStmtTags.executeQuery();
				while(rsTags.next()){
					
					//category_id, item_name, item_name_latin, geo_placename, geo_position, geo_category, ICBM, lat, lng, zip_code, country, tmpl_text, generated_text, ratingCount, reviewCount, voteCount
					String tagStr = String.format("%s~%s~%s~%s~%s~%s~%s~%s~%s~%s~%s~%s~%s~%s~%s~%s",
							rsTags.getString("city_name"),
							rsTags.getString("city_name_latin"),
							"",
							rsTags.getString("geo_placename"),
							rsTags.getString("geo_position"),
							rsTags.getString("geo_region"),
							rsTags.getString("ICBM"),
							rsTags.getBigDecimal("lat"),
							rsTags.getBigDecimal("lng"),
							rsTags.getString("zip_code"),
							rsTags.getString("country"),
							"",
							"",
							String.valueOf(((float)4.11 + ((float)rnd.nextInt(40)/100))),
							String.valueOf(1000 + rnd.nextInt(2000)),
							String.valueOf(2000 + rnd.nextInt(3000))
							
					);
					Utils.appendStringToFile(tagStr, tagsFile);
				}
			}
		}finally{
			silentCLose(connection, rs, prStmt);
		}
	}
	
	private void silentCLose(Connection connection, ResultSet rs, PreparedStatement prStmt) throws SQLException{
		if(rs != null){
			rs.close();
		}
		if(prStmt != null){
			prStmt.close();
		}
		if(connection != null){
			connection.close();
		}
	}

	private Connection getConnection() throws SQLException, ClassNotFoundException
	{
		Class.forName("com.mysql.jdbc.Driver");
		//Setup the connection with the DB
		//Connection connection = (Connection) DriverManager.getConnection(ConfigManager.getInstance().getProperty(DoorgenGeneratorRunner.CONNECTION_STRING_LABEL));
		Connection connection = (Connection) DriverManager.getConnection("jdbc:mysql://198.255.2.70:3306/usaonlinepaydayloansnet?user=usaonlinepayday&password=lol200");

		return connection;
	}
}
