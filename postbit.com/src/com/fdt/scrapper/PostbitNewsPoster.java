/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fdt.scrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fdt.scrapper.task.Constants;
import com.fdt.scrapper.task.NewsTask;
import com.fdt.scrapper.task.Snippet;

/**
 *
 * @author Administrator
 */
public class PostbitNewsPoster {
    private static final Logger logSaveLinks = Logger.getLogger(PostbitNewsPoster.class);
    private static final Logger logExternal = Logger.getLogger(PosterTaskRunner.class);

    private int MIN_SNIPPET_COUNT=3;
    private int MAX_SNIPPET_COUNT=5;

    private int MIN_LINK_COUNT=3;
    private int MAX_LINK_COUNT=5;

    private int MIN_WORDS_COUNT=2;
    private int MAX_WORDS_COUNT=3;

    private static final String MIN_SNIPPET_COUNT_LABEL = "MIN_SNIPPET_COUNT";
    private static final String MAX_SNIPPET_COUNT_LABEL = "MAX_SNIPPET_COUNT";

    private static final String MIN_LINK_COUNT_LABEL = "MIN_LINK_COUNT";
    private static final String MAX_LINK_COUNT_LABEL = "MAX_LINK_COUNT";

    public final static String MAIN_URL_LABEL = "main_url";
    public final static String MAIN_LINKS_URL_LABEL = "main_links_url";
    public final static String POST_NEWS_URL_LABEL = "post_news_url";
    private final static String LOGIN_URL_LABEL = "login_url";

    Random rnd = new Random();

    private NewsTask task = null;
    private Proxy proxy = null;
    private PostbitTaskFactory taskFactory = null;

    private ArrayList<String> cookiesArray = new ArrayList<String>();

    private String userPage = "";
    private String userID = "";
    private String newsUrl = "";
    private String newsID = "";

    private String workingKeyWord = "";
    
    private ArrayList<String> linkList = null;

    private int MAX_KEY_LENGHT = 30;

    private static String[] USER_AGENTS = new String[]{"Opera/9.25 (Windows NT 5.1; U; ru",
	"Opera/9.26 (Windows NT 5.1; U; ru)",
	"Opera/9.20 (Windows NT 5.1; U; ru)",
	"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; MathPlayer 2.10b; .NET CLR 2.0.50727; .NET CLR 1.1.4322)",
	"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)",
	"Opera/9.52 (Windows NT 5.1; U; ru)",
	"Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.8.0.7) Gecko/20060909 Firefox/1.5.0.7",
	"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)",
	"Mozilla/5.0 (X11; U; FreeBSD i386; en-US; rv:1.8.0.9) Gecko/20070204 Firefox/1.5.0.9",
	"Mozilla/4.0 (compatible; MSIE 5.5; Windows 98; Win 9x 4.90) ",
	"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; InfoPath.2) ",
	"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.6) Gecko/20040206 Firefox/0.8 Mnenhy/0.6.0.103 ",
	"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; ru) Opera 8.50 ",
	"Opera/9.0 (Windows NT 5.1; U; en) ",
	"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1) ; .NET CLR 1.1.4322) ",
	"Mozilla/1.22 (compatible; MSIE 2.0d; Windows NT) ",
	"Opera/9.52 (Windows NT 6.0; U; ru) ",
	"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.1) Gecko/2008070208 Firefox/3.0.1 ",
	"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.0.04506) ",
	"Mozilla/5.0 (X11; I; Linux 2.6.22-gentoo-r8 x86_64) Gecko/20071115 Firefox/2.0.0.10 ",
	"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; MRA 5.0 (build 02094)) ",
	"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1) ",
	"Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.9.0.1) Gecko/2008070208 Firefox/3.0.1 ",
	"Mozilla/5.0 (compatible; Refer.Ru; +http://www.refer.ru) ",
	"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; FREE; .NET CLR 1.1.4322) ",
	"Opera/8.54 (Windows NT 5.1; U; en) ",
	"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30) ",
	"Opera/9.02 (Windows NT 5.1; U; ru) ",
	"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; Q312461) ",
	"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727) ",
    "Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.8.1.4) Gecko/20070515 Firefox/2.0.0.4"};

    private static String userAgent = USER_AGENTS[0];

    public PostbitNewsPoster(NewsTask task, Proxy proxy, PostbitTaskFactory taskFactory, ArrayList<String> linkList) {
	this.task = task;
	this.proxy = proxy;
	this.taskFactory = taskFactory;
	this.linkList = linkList;

	MIN_SNIPPET_COUNT = Integer.valueOf(Constants.getInstance().getProperty(MIN_SNIPPET_COUNT_LABEL));
	MAX_SNIPPET_COUNT = Integer.valueOf(Constants.getInstance().getProperty(MAX_SNIPPET_COUNT_LABEL));
	MIN_LINK_COUNT = Integer.valueOf(Constants.getInstance().getProperty(MIN_LINK_COUNT_LABEL));
	MAX_LINK_COUNT = Integer.valueOf(Constants.getInstance().getProperty(MAX_LINK_COUNT_LABEL));
    }

    private String cookiesToStr(ArrayList<String> cookies){
	StringBuilder strBld = new StringBuilder();
	for(String cookie:cookies){
	    strBld.append(cookie).append("; ");
	}
	if(strBld.length() > 0){
	    strBld.setLength(strBld.length()-2);
	}
	return strBld.toString();
    }

    public String executePostNews() throws Exception {
	//get snippets
	ArrayList<Snippet> snippets = parseHtml(task.getKeyWords());
	if(snippets == null || snippets.size() == 0){
	    throw new Exception("Snippets size is 0. Will try to use another proxy server");
	}
	//post news
	return postNews(snippets);
	/*ArrayList<Snippet> test = new ArrayList<Snippet>();
	test.add(new Snippet("1", "1"));
	test.add(new Snippet("2", "2"));
	return postNews(test);*/
    }

    private String preprocessingKeyWord(String key){
	key = key.replaceAll(" ", "");
	if(key.length() > MAX_KEY_LENGHT){
	    key = key.substring(0,MAX_KEY_LENGHT);
	    key = key + (System.currentTimeMillis() % 1000);
	}

	return key;
    }

    private String postNews(ArrayList<Snippet> snippets) throws Exception{
	cookiesArray.add("__utma=; __utmb=; __utmc=; __utmz=; pb_cap=pb");
	userAgent = USER_AGENTS[rnd.nextInt(USER_AGENTS.length)];
	workingKeyWord = preprocessingKeyWord(task.getKeyWords());

	//getCookie();
	registration();
	post(snippets);
	return userPage;
    }

    private void getCookie(){
	//getting cookie for each account
	try {
	    String postUrl = Constants.getInstance().getProperty(MAIN_URL_LABEL);
	    URL url = new URL(postUrl);
	    HttpURLConnection.setFollowRedirects(false);
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
	    conn.setReadTimeout(60000);
	    conn.setConnectTimeout(60000);
	    conn.setRequestMethod("GET");
	    conn.setDoInput(true);
	    conn.setDoOutput(true);

	    conn.addRequestProperty("Host","postbit.com");
	    conn.addRequestProperty("User-Agent",userAgent);
	    conn.addRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	    conn.addRequestProperty("Accept-Language","ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
	    conn.addRequestProperty("Connection","keep-alive");
	    conn.addRequestProperty("Content-Type","application/x-www-form-urlencoded");

	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

	    OutputStream os = conn.getOutputStream();
	    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
	    writer.write(getQuery(nameValuePairs));
	    writer.flush();
	    writer.close();
	    os.close();

	    conn.disconnect();
	} catch (ClientProtocolException e) {
	    logExternal.error("Error during filling account from list and getting cookies for account",e);
	} catch (IOException e) {
	    logExternal.error("Error during filling account from list and getting cookies for account",e);
	}
	/*catch (XPatherException e) {
			log.error("Error during filling account from list and getting cookies for account",e);
		}*/
    }

    private void registration() throws Exception{
	String postUrl = Constants.getInstance().getProperty(MAIN_URL_LABEL) + Constants.getInstance().getProperty(LOGIN_URL_LABEL);;

	//String postUrl = Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + task.getKeyWords() + "delete/";

	try {
	    //registration
	    URL url = new URL(postUrl);
	    HttpURLConnection.setFollowRedirects(true);
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
	    conn.setReadTimeout(60000);
	    conn.setConnectTimeout(60000);
	    conn.setRequestMethod("POST");
	    conn.setDoInput(true);
	    conn.setDoOutput(true);

	    conn.addRequestProperty("Host", "postbit.com");
	    conn.addRequestProperty("Accept", "*/*");	
	    conn.addRequestProperty("Referer", "http://postbit.com");
	    conn.addRequestProperty("X-Requested-With", "XMLHttpRequest");
	    conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
	    conn.addRequestProperty("Connection", "keep-alive");
	    conn.addRequestProperty("User-Agent", userAgent); 
	    conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
	    conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	    conn.setRequestProperty("Pragma", "no-cache");
	    conn.setRequestProperty("Cache-Control", "no-cache");
	    conn.setRequestProperty("Cookie",cookiesToStr(cookiesArray));

	    //httppost.setHeader("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
	    //httppost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"); 
	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	    nameValuePairs.add(new BasicNameValuePair("action","register"));
	    nameValuePairs.add(new BasicNameValuePair("user_username",workingKeyWord));
	    nameValuePairs.add(new BasicNameValuePair("user_email",workingKeyWord+"@gmail.com"));
	    nameValuePairs.add(new BasicNameValuePair("user_password",workingKeyWord));
	    nameValuePairs.add(new BasicNameValuePair("cpx","xKr4"));
	    //Insert news content here
	    /*String[] snippetsContent = getSnippetsContent(snippets);
			task.getNewsContent().put("SNIPPETS_1", snippetsContent[0]);
			task.getNewsContent().put("SNIPPETS_2", snippetsContent[1]);
			task.getNewsContent().put("KEY_WORDS", task.getKeyWords());*/

	    String query = getQuery(nameValuePairs);
	    conn.setRequestProperty("Content-Length",String.valueOf(query.length()));
	    OutputStream os = conn.getOutputStream();
	    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
	    writer.write(query);
	    writer.flush();
	    writer.close();
	    os.close();

	    InputStream is = conn.getInputStream();

	    /*org.jsoup.nodes.Document page = Jsoup.parse(conn.getInputStream(), "UTF-8", "");
		System.out.println(page);*/

	    userPage = "";
	    userID = "";
	    String respContent = convertResponceToString(is);
	    String[] jsonValues = respContent.split(",");

	    boolean tryToLogin = false;
	    if(jsonValues.length >= 2){
		userPage = jsonValues[1].split(":",2)[1];
		userPage = userPage.substring(8, userPage.length()-2);
		userID = jsonValues[2].split(":")[1];
		userID = userID.substring(1, userID.length()-2);
	    }else{
		if(respContent.contains("Unfortunately this username is already taken.")){
		    tryToLogin = true;
		    login();
		}else{
		    throw new Exception("Error occured during registration. Responce: " + respContent);
		}
	    }

	    //read cookies
	    Map<String,List<String>> cookies = conn.getHeaderFields();

	    if(!tryToLogin){
		if(!tryToLogin && cookies.get("Set-cookie") != null){
		    for(String cookieOne: cookies.get("Set-cookie"))
		    {
			cookiesArray.add(cookieOne);
		    }
		}else{
		    throw new Exception("Registration failed for user " + respContent);
		}
	    }
	    
	    if(is != null){
		is.close();
	    }
	    conn.disconnect();

	} catch (ClientProtocolException e) {
	    logExternal.error("Error occured during posting news",e);
	} catch (IOException e) {
	    logExternal.error("Error occured during posting news",e);
	}
    }

    private void login() throws Exception{
	String postUrl = Constants.getInstance().getProperty(MAIN_URL_LABEL) + Constants.getInstance().getProperty(LOGIN_URL_LABEL);;

	//String postUrl = Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + task.getKeyWords() + "delete/";

	try {
	    //registration
	    URL url = new URL(postUrl);
	    HttpURLConnection.setFollowRedirects(false);
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
	    conn.setReadTimeout(60000);
	    conn.setConnectTimeout(60000);
	    conn.setRequestMethod("POST");
	    conn.setDoInput(true);
	    conn.setDoOutput(true);

	    conn.addRequestProperty("Host", "postbit.com");
	    conn.addRequestProperty("Accept", "*/*");	
	    conn.addRequestProperty("Referer", "http://postbit.com");
	    conn.addRequestProperty("X-Requested-With", "XMLHttpRequest");
	    conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
	    conn.addRequestProperty("Connection", "keep-alive");
	    conn.addRequestProperty("User-Agent", userAgent); 
	    conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
	    conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	    conn.setRequestProperty("Pragma", "no-cache");
	    conn.setRequestProperty("Cache-Control", "no-cache");
	    conn.setRequestProperty("Cookie",cookiesToStr(cookiesArray));

	    //httppost.setHeader("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
	    //httppost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"); 
	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	    nameValuePairs.add(new BasicNameValuePair("login_referer","http://postbit.com/"));
	    nameValuePairs.add(new BasicNameValuePair("acao","login"));
	    nameValuePairs.add(new BasicNameValuePair("username_or_email",workingKeyWord+"@gmail.com"));
	    nameValuePairs.add(new BasicNameValuePair("user_password",workingKeyWord));

	    String query = getQuery(nameValuePairs);
	    conn.setRequestProperty("Content-Length",String.valueOf(query.length()));
	    OutputStream os = conn.getOutputStream();
	    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
	    writer.write(query);
	    writer.flush();
	    writer.close();
	    os.close();

	    InputStream is = conn.getInputStream();

	    //read cookies
	    Map<String,List<String>> cookies = conn.getHeaderFields();
	    if(cookies.get("Set-cookie") != null){
		for(String cookieOne: cookies.get("Set-cookie"))
		{
		    cookiesArray.add(cookieOne);
		}
	    }else{
		throw new Exception("Login failed for user " + workingKeyWord);
	    }
	    if(is != null){
		is.close();
	    }
	    conn.disconnect();

	} catch (ClientProtocolException e) {
	    logExternal.error("Error occured during posting news",e);
	} catch (IOException e) {
	    logExternal.error("Error occured during posting news",e);
	}
    }

    private void post(ArrayList<Snippet> snippets) throws Exception{
	userPage = "http://"+workingKeyWord + ".postbit.com";
	
	String postUrl = "http://"+workingKeyWord + ".postbit.com/" + Constants.getInstance().getProperty(POST_NEWS_URL_LABEL);

	try {
	    //posting news
	    URL url = new URL(postUrl);
	    HttpURLConnection.setFollowRedirects(true);
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
	    conn.setReadTimeout(60000);
	    conn.setConnectTimeout(60000);
	    conn.setRequestMethod("POST");
	    conn.setDoInput(true);
	    conn.setDoOutput(true);

	    conn.addRequestProperty("Host", userPage);
	    conn.addRequestProperty("User-Agent", userAgent); 
	    conn.addRequestProperty("Accept", "*/*");	
	    conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
	    conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
	    conn.addRequestProperty("X-Requested-With", "XMLHttpRequest");
	    conn.addRequestProperty("Connection", "keep-alive");
	    conn.setRequestProperty("Pragma", "no-cache");
	    conn.setRequestProperty("Cache-Control", "no-cache");
	    conn.setRequestProperty("Cookie",cookiesToStr(cookiesArray));

	    String[] snippetsContent = getSnippetsContent(snippets);
	    task.getNewsContent().put("SNIPPETS_1", snippetsContent[0]);
	    task.getNewsContent().put("SNIPPETS_2", snippetsContent[1]);
	    task.getNewsContent().put("KEY_WORDS", task.getKeyWords());

	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	    nameValuePairs.add(new BasicNameValuePair("acao","post_save_ok"));
	    nameValuePairs.add(new BasicNameValuePair("cap_pos","xk"));
	    nameValuePairs.add(new BasicNameValuePair("post_id",""));
	    nameValuePairs.add(new BasicNameValuePair("post_title",task.getKeyWords()));
	    nameValuePairs.add(new BasicNameValuePair("post_text",mergeTemplate(task)));
	    nameValuePairs.add(new BasicNameValuePair("post_category",task.getKeyWords().replaceAll(" ", ",")));
	    nameValuePairs.add(new BasicNameValuePair("post_date_year",""));
	    nameValuePairs.add(new BasicNameValuePair("post_date_month",""));
	    nameValuePairs.add(new BasicNameValuePair("post_date_day",""));
	    nameValuePairs.add(new BasicNameValuePair("post_date_hour",""));
	    nameValuePairs.add(new BasicNameValuePair("post_date_min",""));
	    //Insert news content here

	    String query = getQuery(nameValuePairs);
	    conn.setRequestProperty("Content-Length",String.valueOf(query.length()));
	    OutputStream os = conn.getOutputStream();
	    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
	    writer.write(query);
	    writer.flush();
	    writer.close();
	    os.close();

	    InputStream is = conn.getInputStream();

	    /*org.jsoup.nodes.Document page = Jsoup.parse(conn.getInputStream(), "UTF-8", "");
			System.out.println(page);*/

	    newsUrl = "";
	    newsID = "";
	    String respContent = convertResponceToString(is);
	    String[] jsonValues = respContent.split(",");
	    if(jsonValues.length >= 2){
		newsUrl = jsonValues[1].split(":",2)[1];
		newsUrl = newsUrl.substring(1, newsUrl.length()-1);
		newsID = jsonValues[2].split(":")[1];
		newsID = newsID.substring(1, newsID.length()-2);
		logSaveLinks.error(userPage);
	    }else{
		throw new Exception("Error occured during registration. Responce: " + respContent);
	    }

	    if(is != null){
		is.close();
	    }
	    conn.disconnect();

	} catch (ClientProtocolException e) {
	    logExternal.error("Error occured during posting news",e);
	} catch (IOException e) {
	    logExternal.error("Error occured during posting news",e);
	}
    }

    private String convertResponceToString(InputStream is){
	try {
	    BufferedReader br = new BufferedReader( new InputStreamReader(is));

	    StringBuilder sb = new StringBuilder();

	    String line;

	    while ((line = br.readLine()) != null) {
		sb.append(line);
	    }
	    return sb.toString();
	} catch (IOException e) {
	    logExternal.error("Error occured during posting news",e);
	    return "";
	} 
    }

    private String mergeTemplate(NewsTask task){
	//subject
	StringWriter writer = new StringWriter();
	taskFactory.getBottomTemplate().merge(task.getNewsContent(), writer);
	return writer.toString();
    }

    //random links
    private String[] getSnippetsContent(ArrayList<Snippet> snippets) {
	//calculate snippets count
	String[] result = new String[]{"",""};

	int snipCount = 0;
	int linkCount = 0;
	if(snippets.size() <= MIN_SNIPPET_COUNT){
	    snipCount = snippets.size();
	}else{
	    int randomValue = getRandomValue(MIN_SNIPPET_COUNT, MAX_SNIPPET_COUNT);
	    if(randomValue <= snippets.size()){
		snipCount = randomValue;
	    }else{
		snipCount = snippets.size();
	    }
	}
	logExternal.debug("Keywords: task.getKeyWords(). Snippet count: " + snipCount);
	StringBuilder snippetsContent = new StringBuilder();

	//get links count
	int randomValue = getRandomValue(MIN_LINK_COUNT, MAX_LINK_COUNT);
	if(randomValue > taskFactory.getSuccessQueue().size()){
	    linkCount = taskFactory.getSuccessQueue().size();
	}else{
	    linkCount = randomValue;
	}

	//linkCount = 0;

	int snippetLinked = 0;
	int indexShift = getRandomValue(0,snippets.size()-snipCount);

	int resCounter = 0;
	for(int i = indexShift; i < (snipCount+indexShift); i++){
	    //add link to snipper
	    if(snippetLinked < linkCount){
		//add snippet link
		int randomSuccessLink = getRandomValue(1,linkList.size()-1);
		addLinkToSnippetContent(snippets.get(i), linkList.get(randomSuccessLink));
		addLinkToSnippetContent(snippets.get(i), Constants.getInstance().getProperty("") + taskFactory.getSuccessQueue().get(randomSuccessLink-1).getResult());
		snippetLinked++;
	    }
	    snippetsContent.append(snippets.get(i).toString());
	    resCounter++;
	    if(resCounter == 2){
		result[0] = snippetsContent.toString();
		snippetsContent = new StringBuilder();
	    }
	}

	result[1] = snippetsContent.toString();

	return result;
    }

    //titles as links
    /*private String getSnippetsContent(ArrayList<Snippet> snippets) {
		//calculate snippets count
		int snipCount = 0;
		int linkCount = 0;
		if(snippets.size() <= MIN_SNIPPET_COUNT){
			snipCount = snippets.size();
		}else{
			int randomValue = getRandomValue(MIN_SNIPPET_COUNT, MAX_SNIPPET_COUNT);
			if(randomValue <= snippets.size()){
				snipCount = randomValue;
			}else{
				snipCount = snippets.size();
			}
		}
		logExtarnal.debug("Keywords: task.getKeyWords(). Snippet count: " + snipCount);
		StringBuilder snippetsContent = new StringBuilder();

		//get links count
		int randomValue = getRandomValue(MIN_LINK_COUNT, MAX_LINK_COUNT);
		if(randomValue > taskFactory.getSuccessQueue().size()){
			linkCount = taskFactory.getSuccessQueue().size();
		}else{
			linkCount = randomValue;
		}
		int snippetLinked = 0;
		for(int i = 0; i < snipCount; i++){
			//add link to snipper
			if(snippetLinked < linkCount){
				//add snippet link
				int randomSuccessLink = getRandomValue(1,taskFactory.getSuccessQueue().size());
				String titleLink = "<a href=\""+Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + taskFactory.getSuccessQueue().get(randomSuccessLink-1).getResult()+"\">" + snippets.get(i).getTitle()+"</a>";
				snippets.get(i).setTitle(titleLink);
				//addLinkToSnippetContent(snippets.get(i), Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + taskFactory.getSuccessQueue().get(randomSuccessLink-1).getResult());
				snippetsContent.append(snippets.get(i).toString()).append("\r\n");
				snippetLinked++;
			}else{
				snippetsContent.append(snippets.get(i).toString()).append("\r\n");
			}
		}

		return snippetsContent.toString();
	}*/

    private org.jsoup.nodes.Document getUrlContent(String keyWords) throws MalformedURLException, IOException {
	HttpURLConnection conn = null;
	InputStream is = null;
	try{

	    String strUrl = "http://search.tut.by/?rs=1&page="+rnd.nextInt(20)+"&query="+keyWords.replace(" ", "+")+"&how=rlv&ru=1&tc=0&ust="+keyWords.replace(" ", "+")+"&sh=&cg=20&cdig=1";
	    URL url = new URL(strUrl);
	    //using proxy
	    conn = (HttpURLConnection)url.openConnection(proxy);
	    //don't using proxy
	    //conn = (HttpURLConnection)url.openConnection();
	    //conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
	    //conn.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"); 
	    is = conn.getInputStream();
	    org.jsoup.nodes.Document page = Jsoup.parse(conn.getInputStream(), "UTF-8", strUrl);
	    is.close();
	    is = null;
	    conn.disconnect();
	    conn = null;
	    return page;
	}finally{
	    if(conn != null){
		try{conn.disconnect();}catch(Throwable e){}
	    }
	    if(is != null){
		try{is.close();}catch(Throwable e){}
	    }
	}
    }

    private void addLinkToSnippetContent(Snippet snippet, String link){
	StringBuilder newContent = new StringBuilder(snippet.getContent());
	//find random
	String[] words = snippet.getContent().split(" ");
	//all snippet will be as link
	if(words.length == 1 || words.length == 2){
	    //insert link here
	    newContent = new StringBuilder();
	    newContent.append("<a href=\""+link+"\">");
	    for(int i = 0; i < words.length; i++){
		newContent.append(words[i]).append(" ");
	    }
	    newContent.setLength(newContent.length()-1);
	    newContent.append("</a>");
	}else if(words.length > 2){
	    int randomValue = getRandomValue(MIN_WORDS_COUNT, MAX_WORDS_COUNT);
	    int startStringIndex = getRandomValue(0, words.length-randomValue);
	    newContent = new StringBuilder();
	    for(int i = 0; i < words.length; i++){
		if(startStringIndex == i){
		    newContent.append("<a href=\""+link+"\">").append(words[i]).append(" ");
		    continue;
		}else if((startStringIndex + randomValue-1) == i){
		    newContent.append(words[i]).append("</a>").append(" ");
		    continue;
		}
		newContent.append(words[i]).append(" ");
	    }
	    if(newContent.length() > 0){
		newContent.setLength(newContent.length()-1);
	    }
	}
	snippet.setContent(newContent.toString());
    }

    private Integer getRandomValue(Integer minValue, Integer maxValue){
	return  minValue + rnd.nextInt(maxValue - minValue+1);
    }

    private ArrayList<Snippet> parseHtml(String keyWords) throws MalformedURLException, IOException{
	ArrayList<Snippet> snippets = new ArrayList<Snippet>();
	org.jsoup.nodes.Document page = getUrlContent(keyWords);

	Elements elements = page.select("li[class=b-results__li]");
	if(!elements.isEmpty()){
	    for(Element element : elements){
		String h3Value = element.select("h3").text();
		String pValue = element.select("p").text();
		if(h3Value != null && !"".equals(h3Value.trim()) && pValue != null && !"".equals(pValue.trim())){
		    snippets.add(new Snippet(h3Value.trim(), pValue.trim()));
		}
	    }
	}
	return snippets;
    }

    public static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
	StringBuilder result = new StringBuilder();
	boolean first = true;

	for (NameValuePair pair : params)
	{
	    if (first)
		first = false;
	    else
		result.append("&");

	    result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
	    result.append("=");
	    result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
	}

	return result.toString();
    }
}
