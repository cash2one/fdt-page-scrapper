
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.fdt.utils.Utils;


public class Test {

	private static final String TIME_STAMP_FORMAT = "HH:mm:ss.SSS";
	private static SimpleDateFormat sdf = new SimpleDateFormat(TIME_STAMP_FORMAT);

	private static final String outputFilename = "myVideo.mov";

	private volatile ArrayList<String> str = new  ArrayList<String>();
	static int i;

	private static Random rnd = new Random();

	public static void main(String[] args) {

		/*final IMediaWriter writer = ToolFactory.makeWriter(outputFilename);

        writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, 1080, 720);

        File folder = new File("d:\\Work\\Scrapper_door_GIT_new\\dailymotion.com\\images");
        File[] listOfFiles = folder.listFiles();

        //for (int index = 0; index < SECONDS_TO_RUN_FOR * FRAME_RATE; index++) {
        BufferedImage screen = ImageIO.read(new File("d:\\Work\\Scrapper_door_GIT_new\\dailymotion.com\\images\\article_1.jpg"));
        BufferedImage bgrScreen = convertToType(screen, BufferedImage.TYPE_3BYTE_BGR);

        BufferedImage screen2 = ImageIO.read(new File("d:\\Work\\Scrapper_door_GIT_new\\dailymotion.com\\images\\preview_article_2.jpg"));
        BufferedImage bgrScreen2 = convertToType(screen2, BufferedImage.TYPE_3BYTE_BGR);
        for(int i = 0; i <36; i++){
        	writer.encodeVideo(0, bgrScreen, i, TimeUnit.SECONDS);
        }
        //writer.encodeVideo(0, bgrScreen2, 3599, TimeUnit.SECONDS);
        writer.encodeVideo(0, bgrScreen2, 36, TimeUnit.SECONDS);
        writer.encodeVideo(0, bgrScreen2, 37, TimeUnit.SECONDS);
        // tell the writer to close and write the trailer if needed
        writer.flush();
        writer.close();
        System.out.println("Video Created");*/

		/*String test = "Seon in Korea Taego Bou Jinul Seongcheol Zen in the USA D. T. Suzuki Hakuun Yasutani Taizan Maezumi Shunryū Suzuki Seungsahn Category: Zen Buddhists …";
    	System.out.println(test.replaceAll("(\\.){2,}", ".").replaceAll("…", ".").trim());*/

		/*ExecutorService extService = Executors.newFixedThreadPool(5);
    	Future runFut = extService.submit(new RunnableTest());
    	Future<String> callFut = extService.submit(new CallableTest());

    	try {
			Object obj = runFut.get();
			obj = obj;
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

    	Future<String> result = extService.submit(new Callable<String>(){

			public String call() throws Exception {
				throw new Exception("Callable exception");
				//return "Callable";
			}

    	});

    	Future result2 = extService.submit(new Runnable(){
			public void run() {
			}
    	});

    	try {
			System.out.println(result.get());
			System.out.println(result2.get());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	long a = 1;
    	long b = 3;
    	long fib = 1;
    	System.out.println(fib);
    	for(int i = 0; i < 100; i++){
    		fib = a + b;
    		a = b;
    		b = fib;
    		System.out.println(fib);
    	}

    	System.out.println(i);
    	int[][][] testArray = new int[2][][];
    	String $ = "";
    	$ += "";

    	System.out.println("------------------");
    	for(int i = 0; i < 3; i++){
			for(int j = 1; j <= 3; j++){
				System.out.println(i*3+j);
			}
		}

    	System.out.println(getFirstSmblUpper("fKKKKKKKSFD"));
    	System.out.println(cleanString("dfa af_*$_*@#_%#@ :!\"№;%:?*()_LK:AFы а фыва         фыва фыва фы афы а 75% 15$"));*/

		/*FileWriter fw = null;
    	BufferedWriter bw = null;
    	PrintWriter pw = null;

    	String reportFileName = "test_rep.name";
		try {
			File tempFile = File.createTempFile(reportFileName, ".CSV", new File("./"));
			//tempFile.deleteOnExit();
			fw = new FileWriter(tempFile);
			bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			byte[] bom = new byte[] { (byte)0xEF, (byte)0xBB, (byte)0xBF };
			pw.print(new String(bom));
			pw.print("TEST BIG STRINGSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS");
			pw.close();
			bw.close();
			fw.close();
		} catch (Exception e) {
			//
		}*/

		/*for(int i = 0; i < 60; i++){
			int val1 = ((i - (i % 5)) / 5);
			int val2 = i/5;
			if(val1 != val2){
				System.out.println("Error for i: " + i);
			}
			System.out.println(String.format("%d: %d - %d", i, val1, val2));
		}*/

		/*for(int i = 18900; i <= 19500; i++){
			System.out.println((double)23037276/i + " " + i);
		}*/

		/*Random rnd = new Random();

    	for(int i = 0; i < 100; i++)
    	{
    		System.out.println(rnd.nextGaussian());
    	}

    	for(int i = 0; i < 100; i++)
    	{
    		long time = getRndNormalDistTime();
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		System.out.println(time + " " + sdf.format(new Date(time)));
    	}
		 */

		/*String testStr = ";;;123;123;;;1";
    	System.out.println(testStr.split(";") + " " + testStr.split(";").length);
    	for(String str : testStr.split(";",0)){
    		System.out.println("--" + str);
    	}*/

		/*ArrayList<String> test = new ArrayList<String>();
		test.add("1");
		System.out.println("TEST: " + test.get(1));*/

		/*for(TestEnum strg : TestEnum.values()){
			System.out.println(strg.name());
			System.out.println(strg.getName());
		}*/

		/*String testSrt = "";
		System.out.println("   	".isEmpty());
		System.out.println("".isEmpty());

		String str = "Adak, Aleutians West county, AK 99546, USA";
		System.out.println(str.replaceAll("[^0-9]*", ""));
		System.out.println(str.split(",")[1].trim());
		System.out.println(toUpperFirstLetters("yeElow park"));

		for(int i = 1; i < 500; i += 5 ){
			for(int j = 1; j < 125; j += 5){
				System.out.print(i+","+j+";");
			}
		}

		String keyWord = "Payday Loans's'   \"online: -connected   ";
		String[] list = keyWord.split("[\\s\\:\\-\\'\"\\%\\$\\-]+");
		list = keyWord.split("[\\s]+");
		keyWord = "Payday 'Loans' online: -connected";

		String forReplace = "Payday Our services are designed to help you find a reputable payday lender to work with. PayDayLoansforUSA.org has a large number "
				+ "of lenders in our network, and we are confident that we can assist you in obtaining the best PAyday loan to meet your needs.";

		System.out.println(forReplace);
		System.out.println(forReplace.replaceAll("(?i)(([\\s\\:\\-\"\\%\\$\\-\\.,]+)|(^))" + "payday" + "(([\\s\\:\\-\"\\%\\$\\-\\.,]+)|($))", "$1__REPLACED__$4"));*/


		/*String str4Replace = "same day payday {advance} {is|is usually|is definitely|is certainly|is normally|is going to be} a fast and {convenient|easy|practical|effortless|comfortable|simple} solution for your {short|brief} term cash {needs|requirements|demands|wants|desires|necessities}";
		Pattern p =  Pattern.compile("\\{(.+?)\\}");
        Matcher m = p.matcher(str4Replace);  

        int idx = 1;
       	while(m.find()){
       		System.out.println(m.groupCount());
       		int rndGrpCnt = m.groupCount();
       		System.out.println(m.group(1));
        }*/

		/*Random rnd = new Random();
		rnd.nextInt();

		System.out.println("-----------------------------");

		String params = "app1=123&app2=321";

		for (StringTokenizer tok = new StringTokenizer(params, "&"); tok.hasMoreTokens();)
		{
			System.out.println(tok.nextToken());
		}

		String title="[Book]\r\n";
		String newTitle = title.replaceAll("\\[Book\\]", "New Book[]");
		System.out.println("newTitle: " + newTitle);

		Random rand = new Random();
		System.out.println(rand.nextInt(1));

		String str1 = "Новый 123 132123 123 1 13 1 Новый вопрос напишите ответ 1314123 113";
		String str2 = "Новый 123 132123 123 1 13 1 1314123 113";

		List<String> aList = Arrays.asList(str1.split(" "));
		List<String> bList = Arrays.asList(str2.split(" "));

		List<String> union = new ArrayList(aList);
		union.addAll(bList);

		List<Integer> intersection = new ArrayList(aList);
		intersection.retainAll(bList);

		List<Integer> symmetricDifference = new ArrayList(union);
		symmetricDifference.removeAll(intersection);

		System.out.println(symmetricDifference);*/
		/*AtomicBoolean ab = new AtomicBoolean(false);
		System.out.println(ab.compareAndSet(false, true));
		ab = new AtomicBoolean(true);
		System.out.println(ab.compareAndSet(false, true));*/

		/*System.out.println(extractOrderNumber("ebook15.jpg"));
		System.out.println(extractOrderNumber("ebook0.jpg"));
		System.out.println(extractOrderNumber("ebook324.jpg"));
		System.out.println(extractOrderNumber("ebook_3.png"));
		System.out.println(extractOrderNumber("ebook_.jpg"));

		System.out.println(Object.class.getClassLoader());*/

		/*try {
			// open websocket
			WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI("wss://ws.blockchain.info/inv"));

			// add listener
			clientEndPoint.addMessageHandler(new MessageHandler() {
				public void handleMessage(String message) {
					System.out.println(message);
				}
			});

			// send message to websocket
			clientEndPoint.sendMessage("{\"op\":\"unconfirmed_sub\"}");

			// wait 5 seconds for messages from websocket
			Thread.sleep(5000000);

		} catch (InterruptedException ex) {
			System.err.println("InterruptedException exception: " + ex.getMessage());
		} catch (URISyntaxException ex) {
			System.err.println("URISyntaxException exception: " + ex.getMessage());
		}*/

		//String testString = "<article><div><b>From the Publisher</b></div>";
		/*String testString = "<h2>Editorial     Reviews</h2><article><div><b>From       the Publisher    </b></div>";
		System.out.println(cleanDataFile(testString));*/

		/*Snippet snp = new Snippet();
		snp.setContent("About the experience of violence, abuse, love, loss, and femininity.");
		Utils.addLinkToSnippetContent(snp, "http://test.com", 2, 3);
		System.out.println(snp.getContent());*/
		/*StringBuffer sb = new StringBuffer("123~123~~~~");
		System.out.println(trimLastChars(sb, '~'));
		sb = new StringBuffer("123~123FDSAFAS~SDFASDF");
		System.out.println(trimLastChars(sb, '~'));
		sb = new StringBuffer("123~123FDSAFAS~SDFASDF~");
		System.out.println(trimLastChars(sb, '~'));
		sb = new StringBuffer("");
		System.out.println(trimLastChars(sb, '~'));*/

		/*ArrayList<String> cleanList = new ArrayList<String>(Arrays.asList(new String[]{"cbc","cg"}));
		ArrayList<String> args4Clean = new ArrayList<String>(Arrays.asList(new String[]{"cbc1","cbc2","cbc3","cc","cg1"}));
		//Collections.sort(Arrays.asList(argTest));

		Collections.sort(cleanList, new Comparator<String>(){

			@Override
			public int compare(String o1, String o2) {
				return o2.compareTo(o1);
			}

		});

		Collections.sort(args4Clean, new Comparator<String>(){

			@Override
			public int compare(String o1, String o2) {
				return o2.compareTo(o1);
			}

		});

		for(String str : cleanList){
			System.out.println(str);
		}

		System.out.println("");

		for(String str : args4Clean){
			System.out.println(str);
		}

		int idx4Clean = 0;

		for(int i = 0; i < cleanList.size(); i++){

			String cleanWord = cleanList.get(i);

			while(args4Clean.get(idx4Clean).compareTo(cleanWord) >= 0)
			{
				if(args4Clean.get(idx4Clean).startsWith(cleanWord))
				{
					args4Clean.remove(idx4Clean);
					if(args4Clean.size() == idx4Clean){
						break;
					}
				}else{
					idx4Clean++;
				}
			}
		}

		System.out.println("\r\nResult:");
		System.out.println("cc".compareTo("cg"));
		System.out.println("ca".compareTo("cas"));
		System.out.println("\r\nCleaned:");
		for(String str : args4Clean){
			System.out.println(str);
		}*/

		/*BufferedImage image;
		try {
			image = ImageIO.read(new URL("https://upload.wikimedia.org/wikipedia/en/2/24/Lenna.png"));
			Graphics g = image.getGraphics();

			Font myFont = new Font("Serif", Font.BOLD, 12);
		    Font newFont = myFont.deriveFont(50F);

		    g.setFont(newFont);
		    g.drawString("Hello World! Veeeeeeeeeeeeee", 100, 100);
		    g.drawString("eeeeeeeeeeeeeeery long text", 100, 150);
		    g.dispose();

		    ImageIO.write(image, "png", new File("test.png"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	 */

		/*int[] postInterval = new int[]{5,3};

		int idxCnt = 0;

		Random rnd = new Random();

		for(int i = 0; i<10; i++){
			int rndDayCnt = postInterval[0] * (idxCnt/postInterval[1]) + 1 + rnd.nextInt(postInterval[0]);
			idxCnt++;
			System.out.println(String.format("i=%d; idx=%d; rnd day=%d",i, idxCnt,rndDayCnt));
		}*/
		
		//System.out.println(synonymizeText(Utils.loadFileAsString(new File("synonym.txt"))));
		//System.out.println(String.format("%03d", 14));
		
		//System.out.println("test   space 33".trim().replace(" ", "-"));
		
		//System.out.println(",,,123,,123,".split(",").length);
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		String strValue = (String) map.get("123");
		strValue = (String) null;
		System.out.println(strValue);
	}

	private static String synonymizeText(String text){
		Pattern ptrn = Pattern.compile("(\\{([^\\{\\}]+)\\})");
		Matcher mtch = ptrn.matcher(text);

		while(mtch.find()){
			String[] array = mtch.group(2).split("\\|");
			text = text.replace(mtch.group(1), array[rnd.nextInt(array.length)]);
			mtch = ptrn.matcher(text);
		}

		return text;
	}

	public static StringBuffer trimLastChars(String input, char char4Trim)
	{
		StringBuffer sb = new StringBuffer(input);

		while(sb.length() > 0 && sb.charAt(sb.length()-1) == char4Trim )
		{
			sb = sb.deleteCharAt(sb.length() - 1);
		}

		return sb;
	}


	private static String cleanDataFile(String input){

		input = input.replaceAll("<dt>(.*?)</dt>", " ")
				.replaceAll("<dd>(.*?)</dd>", "$1")
				.replaceAll("<h2>Product Details</h2>", " ")
				.replaceAll("<h2>Overview</h2>", " ")
				.replaceAll("<[^>]*>(.*?)</[^>]*>", "$1")
				.replaceAll("<[^>]*?>", " ")
				.replaceAll("</[^>]*?>", " ")
				.replaceAll("\\s+", " ");

		return input;
	}

	private static int extractOrderNumber(String fileName) {
		Pattern ptrn =  Pattern.compile("(.*?)(\\d+)\\.(png|jpg)");
		Matcher mtchr = ptrn.matcher(fileName);

		int result = -1;

		if(mtchr.matches()){
			result = Integer.valueOf(mtchr.group(2));
		}

		return result;
	}

	private static String toUpperFirstLetters(String input){
		StringBuffer strBuf = new StringBuffer();
		for(String word : input.split(" ")){
			strBuf.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase()).append(" ");
		}

		if(strBuf.length() > 0){
			strBuf.setLength(strBuf.length()-1);
		}

		return strBuf.toString();
	}

	enum TestEnum{
		e1("test_1"),
		e2("TEST_2");

		private String tmplName;

		private TestEnum(String tmplName) {
			this.tmplName = tmplName;
		}

		public String getName(){
			return tmplName;
		}
	}

	public static long getRndNormalDistTime(){
		Random rnd = new Random();
		double gaus = rnd.nextGaussian();
		while(Math.abs(gaus) > 2){
			gaus = rnd.nextGaussian();
		}

		long time = (long)Math.round(24*60*60*1000*(2+gaus)/4);

		return time;
	}

	public static void getRndNormalDist(int minValue, int maxValue){

		//TODO generate random tetta [1.0:2.0]
		double tetta = 1;
		double nu = (maxValue-minValue);

		ArrayList<Double> list = new ArrayList<Double>();

		for(int i = 0; i < 60*60*24; i++){
			double val1 = tetta*Math.sqrt(2.0*Math.PI);
			double val2 = Math.pow((double)i-nu, 2);
			double val3 = 2.0*Math.pow(tetta, 2);
			double val4 = Math.exp(-1*(val2)/(val3));
			double value = ( 1.0 / val1 ) * val4 * i;
			list.add(value);
		}

		Collections.sort(list);



		for(int i = 0; i < 60*60*24; i=i+10){
			if(list.get(i) > 0){
				i = i;
			}
			System.out.println(list.get(i));
			//System.out.println(sdf.format(new Date(list.get(i)));
		}
	}

	private static String getFirstSmblUpper(String input){
		StringBuffer output = new StringBuffer(input.substring(1).toLowerCase());
		output.insert(0, input.substring(0, 1).toUpperCase());

		return output.toString();
	}

	private static String cleanString(String input){
		StringBuffer output = new StringBuffer(input);

		return output.toString().replaceAll("[^0-9a-zA-Zа-яА-Я\\s\\%\\$]+", "").replaceAll("\\s+", " ");
	}

	public static void main2(String[] args) throws IOException {
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		System.out.println(getTimeString(new int[]{34,2}));
		System.out.println(getTimeString2(new int[]{34,2}));
	}


	public static BufferedImage convertToType(BufferedImage sourceImage, int targetType) {
		BufferedImage image;
		if (sourceImage.getType() == targetType) {
			image = sourceImage;
		}
		else {
			image = new BufferedImage(sourceImage.getWidth(),
					sourceImage.getHeight(), targetType);
			image.getGraphics().drawImage(sourceImage, 0, 0, null);
		}
		return image;
	}

	private static String getTimeString(int[] times){
		double milSecCnt = 0L;
		milSecCnt =  (times[0] / times[1]) * 1000 + ( (double)(times[0]%times[1])/times[1])*1000;

		String valueStr = String.format("%.0f", milSecCnt);

		return sdf.format(new Date(Long.parseLong(valueStr)-1000));
	}

	private static String getTimeString2(int[] times){
		double milSecCnt = 0L;
		milSecCnt =  (((double)times[0]/times[1])) * 1000;

		String valueStr = String.format("%.0f", milSecCnt);

		return sdf.format(new Date(Long.parseLong(valueStr)-1000));
	}

	private static class CallableTest implements Callable<String>{

		public String call() throws Exception {
			// TODO Auto-generated method stub
			return "test";
		}
	}

	public void run() {
		// TODO Auto-generated method stub
		throw new NullPointerException();
	}

	private static class RunnableTest implements Runnable{

		public void run() {

			//throw new NullPointerException();
		}
	}

	private interface ITest{
		public abstract int calculate2();

	}
}