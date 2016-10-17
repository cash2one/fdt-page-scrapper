package com.fdt.doorgen.key.pooler.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import com.fdt.utils.Utils;

public class SiteMapGenerator 
{

	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"+
			"<urlset xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" xmlns:mobile=\"http://www.google.com/schemas/sitemap-mobile/1.0\" xmlns:image=\"http://www.google.com/schemas/sitemap-image/1.1\" xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">\r\n";

	private static final String XML_TAIL = "</urlset>";

	private static final String XML_BOTY_TMPL=	"<url>\r\n"+
			"<loc>%s</loc>\r\n"+
			"<lastmod>%s</lastmod>\r\n"+
			"<changefreq>daily</changefreq>\r\n"+
			"<priority>1</priority>\r\n"+
			"</url>\r\n";

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

	/**
	 * args[0] - url_files
	 * args[1] - min urls count
	 * args[2] - max urls count
	 * @param args
	 */
	public static void main(String... args){
		try{
			Random rnd = new Random();
			rnd.nextInt();

			File utlsFile = new File("txt/urls.txt");
			File outputFile = new File("sitemap_gen.xml");
			File articleFile = null;
			
			int minUrlsCnt = 900;
			int maxUrlsCnt = 1000;

			switch(args.length) {
			case 5:{
				articleFile = new File(args[4]);
			}
			case 4:{
				outputFile = new File(args[3]);
			}
			case 3:{
				maxUrlsCnt = Integer.valueOf(args[2]);
			}
			case 2:{
				minUrlsCnt = Integer.valueOf(args[1]);
			}
			case 1:{
				utlsFile = new File(args[0]);
				break;
			}
			default:break;
			}

			List<String> urls = Utils.loadFileAsStrList(utlsFile);
			HashSet<String> urlsSet = new HashSet<String>(urls);
			urls = new ArrayList<String>(urlsSet);
			
			List<String> articleUrls = new ArrayList<String>();
			
			if(articleFile != null && articleFile.exists())
			{
				articleUrls = Utils.loadFileAsStrList(articleFile);
			}
			
			Collections.shuffle(urls);

			//get sitemap
			StringBuffer strBuf = new StringBuffer();
			int rndCount = minUrlsCnt + rnd.nextInt(maxUrlsCnt - minUrlsCnt + 1);

			String lastModDate = sdf.format(new Date(System.currentTimeMillis()));

			strBuf.append(XML_HEADER);

			//Load all article files
			for(int i = 0; i < articleUrls.size(); i++){
				strBuf.append(
						String.format(
								XML_BOTY_TMPL, 
								articleUrls.get(i),
								lastModDate
								)
						);
			}
			
			for(int i = 0; i < rndCount && urls.size() > i ; i++){
				strBuf.append(
						String.format(
								XML_BOTY_TMPL, 
								urls.get(i),
								lastModDate
								)
						);
			}

			strBuf.append(XML_TAIL);

			outputFile.delete();

			Utils.appendStringToFile(strBuf.toString(), outputFile);
		}catch(Throwable e){
			e.printStackTrace();
		}
	}
	
	
}
