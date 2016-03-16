package com.ssa.custsites;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.fdt.utils.Utils;
import com.ssa.rent.util.EmailSender;

public class TutByNewsChecker4Update {

	private static final Logger log = Logger.getLogger(TutByNewsChecker4Update.class);

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME =60000L;

	public TutByNewsChecker4Update(String cfgFilePath){

	}

	public static void main(String[] args) {
		try{
			TutByNewsChecker4Update taskRunner = new TutByNewsChecker4Update("config.ini");
			DOMConfigurator.configure("./log4j.xml");
			
			EmailSender mailSender = new EmailSender();
			
			while(true){
				taskRunner.getFlats(mailSender);
				Thread.sleep(RUNNER_QUEUE_EMPTY_WAIT_TIME);
			}
			
			
			//System.out.print("Program execution finished");
			//System.exit(0);
		}catch(Throwable e){
			e.printStackTrace();
			log.error("Error during main stream",e);
			System.out.print("Program execution finished with errors");
		}
	}
	private boolean getFlats(EmailSender mailSender) throws IOException{

		String url = "http://afisha.tut.by/news/anews/488401.html";

		log.info("Fetching %s..." + url);

		Document doc = Jsoup.connect(url)
				.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0")
				.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
				.timeout(30000).get();
		Elements tabels = doc.select("div[id=utm_article_block");
		
		String lastNews = Utils.loadFileAsString(new File("tut.by.news.txt")).replaceAll("\r\n", "");
		
		String newsNew = tabels.text();
		
		if(!newsNew.equalsIgnoreCase(lastNews)){
			List<String> aList = Arrays.asList(lastNews.split(" "));
			List<String> bList = Arrays.asList(newsNew.split(" "));
			
			List<String> union = new ArrayList<String>(aList);
			union.addAll(bList);

			List<String> intersection = new ArrayList<String>(aList);
			intersection.retainAll(bList);

			List<String> symmetricDifference = new ArrayList<String>(union);
			symmetricDifference.removeAll(intersection);
			
			StringBuffer strBuf = new StringBuffer();
			for(String str : symmetricDifference){
				strBuf.append(str).append(" ");
			}
			
			File output = new File("tut.by.news.txt");
			output.delete();
			Utils.appendStringToFile(tabels.text(), new File("tut.by.news.txt"));
			mailSender.sendEmail("sidorenko.s.a@gmail.com", "","http://afisha.tut.by/news/anews/488401.html\r\n" + strBuf.toString());
			return true;
		}
		
		return false;

	}
}
