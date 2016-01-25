
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.Callable;


public class Test {

	private static final String TIME_STAMP_FORMAT = "HH:mm:ss.SSS";
	private static SimpleDateFormat sdf = new SimpleDateFormat(TIME_STAMP_FORMAT);

	private static final String outputFilename = "myVideo.mov";

	private volatile ArrayList<String> str = new  ArrayList<String>();
	static int i;

	public static void main(String[] args) throws IOException {

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

		for(TestEnum strg : TestEnum.values()){
			System.out.println(strg.name());
			System.out.println(strg.getName());
		}

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

