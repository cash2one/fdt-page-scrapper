package com.fdt.smsreg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.fdt")
@PropertySource(value="file:${config.file}",ignoreResourceNotFound = true)
public class SmsRegRunner {

	private static final Logger log = Logger.getLogger(SmsRegRunner.class);

	private ISmsReg smsRegImpl = new SmsRegImpl();

	private final String API_KEY = "b7vb67q8r241cni2n1r9rsqvumzyzlne";

	//http://api.sms-reg.com/METHOD_NAME.php?PARAMETERS&apikey=YOUR_APIKEY
	private String postUrlTmpl = "http://api.sms-reg.com/?.php\\??&apikey=?";

	/**
	 * args[0] - service code
	 * args[1] - country code
	 * args[2] - 
	 */
	public static void main(String[] args)
	{
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		ApplicationContext ctx = SpringApplication.run(SmsRegRunner.class, args);

		System.out.println("Let's inspect the beans provided by Spring Boot:");

		String[] beanNames = ctx.getBeanDefinitionNames();
		Arrays.sort(beanNames);
		for (String beanName : beanNames) {
			System.out.println(beanName);
		}

		DOMConfigurator.configure("log4j.xml");
		SmsRegRunner taskRunner = ctx.getBean(SmsRegRunner.class);

		taskRunner.execute();
	}

	/**
	 * getNum - 
	 */

	public SmsRegRunner() {
		super();
		// TODO Auto-generated constructor stub
	}

	@PostConstruct
	private void init() throws Exception{

	}

	public void execute(){
		//
	}

	private String executeRequest(String methodName, Map<String,Object> params) throws Exception{
		HttpsURLConnection conn = null;
		String uploadUrl = ""; 
		try{
			//post news
			//TODO Add developer's key
			URL url = new URL(String.format(postUrlTmpl, methodName, params2Str(params), API_KEY));
			HttpsURLConnection.setFollowRedirects(false);
			conn = (HttpsURLConnection) url.openConnection();
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("GET");
			conn.setDoOutput(true);


			StringBuilder responseStr = getResponseAsString(conn);

			log.debug(responseStr.toString());

			conn.disconnect();

			JSONObject jsonObj = new JSONObject(responseStr.substring(1, responseStr.length()-1));
			//uploadUrl = jsonObj.getJSONObject("result").getString("upload_url");

			log.info("Upload URL: " + uploadUrl);
		}finally{
			if(conn != null){
				conn.disconnect();
			}
		}

		return uploadUrl;
	}

	private String params2Str(Map<String,Object> params){
		StringBuffer paramsStr = new StringBuffer();

		for(String key : params.keySet()){
			paramsStr.append(key).append("=").append(params.get(key)).append("&");
		}

		if(paramsStr.length() > 0){
			paramsStr.setLength( paramsStr.length() - 1 );
		}

		return paramsStr.toString();
	}

	private StringBuilder getResponseAsString(HttpURLConnection conn)
			throws IOException {
		InputStream is = conn.getInputStream();

		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String line;
		StringBuilder responseStr = new StringBuilder();
		while ((line = br.readLine()) != null) {
			responseStr.append(line).append("\r\n");
		}
		is.close();
		return responseStr;
	}

}
