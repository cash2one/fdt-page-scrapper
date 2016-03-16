package com.ssa.rent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ssa.rent.dao.IFlatStorage;
import com.ssa.rent.dao.impl.FlatFileStorage;
import com.ssa.rent.util.ConfigurationParser;
import com.ssa.rent.util.EmailSender;
import com.ssa.rent.util.PageElement;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Example program to list links from a URL.
 */
public class FlatScrapper {
    private static final int WAIT_TIME = 30000;
    private EmailSender mailSender;
    private ConfigurationParser cfgParser;

    public FlatScrapper(){
	mailSender = new EmailSender();
	cfgParser = new ConfigurationParser("kvartirant.by","kvartirant.by");
    }

    public static void main(String[] args) throws IOException {
	FlatScrapper flatParser = new FlatScrapper();
	flatParser.loop();
    }

    private ArrayList<Flat> getFlats() throws IOException{
	ArrayList<Flat> flats = new ArrayList<Flat>();

	String url = "";
	print("Fetching %s...", url);

	url = "http://www.kvartirant.by/ads/flats/type/rent/?tx_uedbadsboard_pi1[search][q]=&tx_uedbadsboard_pi1[search][district]=0&tx_uedbadsboard_pi1[search][rooms][1]=1&tx_uedbadsboard_pi1[search][price][from]=150&tx_uedbadsboard_pi1[search][price][to]=500&tx_uedbadsboard_pi1[search][currency]=840&tx_uedbadsboard_pi1[search][date]=259200&tx_uedbadsboard_pi1[search][agency_id]=&tx_uedbadsboard_pi1[search][agency_fact]=on&tx_uedbadsboard_pi1[search][owner]=on";
	Document doc = Jsoup.connect(url)
	.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0")
	.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
	.timeout(30000).get();
	Elements tabels = doc.select(cfgParser.getElementLocation(PageElement.ROOT_ELEMENT).getElementXPath());

	print("\table: (%d)", tabels.size());
	for (Element link : tabels) {
	    String title = link.select(cfgParser.getElementLocation(PageElement.TITLE).getElementXPath()).text();
	    String price = link.select(cfgParser.getElementLocation(PageElement.PRICE).getElementXPath()).text();
	    String address = link.select(cfgParser.getElementLocation(PageElement.ADDRESS).getElementXPath()).text();
	    //String detailInfo = link.select("a[class=ad_button] ~ p").html();
	    String detailInfo = link.select(cfgParser.getElementLocation(PageElement.DETAIL_INFO).getElementXPath()).html();
	    String phone = link.select(cfgParser.getElementLocation(PageElement.PHONE).getElementXPath()).html();
	    String postDate = link.select(cfgParser.getElementLocation(PageElement.POST_DATE).getElementXPath()).html();
	    //replace typo3temp
	    if(phone.contains("typo3temp")){
		phone = phone.replace("typo3temp", "kvartirant.by/typo3temp");
	    }

	    if( (title != null && !"".equals(title.trim())) || 
		    (price != null && !"".equals(price.trim())) ||
		    (address != null && !"".equals(address.trim())))
	    {
		price = price.substring(0, price.length()-1).trim();
		if(Integer.valueOf(price) <= 370){
		    Flat flat = new Flat(title, address, price, detailInfo);
		    flat.setPhone(phone);
		    flat.setPostDate(postDate);
		    flats.add(flat);
		}
	    }
	}
	return flats;
    }

    private void process(ArrayList<Flat> newFlats){
	System.out.println("********************");
	IFlatStorage manager = new FlatFileStorage();
	ArrayList<Flat> oldFlats = manager.readFlatList();
	StringBuilder msg = new StringBuilder();
	for(Flat flat:newFlats){
	    if(!oldFlats.contains(flat)){
		//new flat find
		msg.append(flat.toString());
		msg.append("\r\n");
	    }
	}
	if(msg.length() > 0){
	    mailSender.sendEmail("sidorenko.s.a@googlemail.com", "8rKBfLI5",msg.toString());
	}
	manager.storeFlat(newFlats);
    }

    private synchronized void loop(){
	while(true){
	    try {
		process(getFlats());
		wait(WAIT_TIME);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    private static void print(String msg, Object... args) {
	System.out.println(String.format(msg, args));
    }

    private static String trim(String s, int width) {
	if (s.length() > width)
	    return s.substring(0, width-1) + ".";
	else
	    return s;
    }
}
