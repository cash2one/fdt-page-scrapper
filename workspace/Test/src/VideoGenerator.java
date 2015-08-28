
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;

public class VideoGenerator {

	private static final String TIME_STAMP_FORMAT = "HH:mm:ss.SSS";
	private static SimpleDateFormat sdf = new SimpleDateFormat(TIME_STAMP_FORMAT);

    private static final String outputFilename = "myVideo.mp4";

    /*public static void main(String[] args) throws IOException {

        final IMediaWriter writer = ToolFactory.makeWriter(outputFilename);

        writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, 1080, 720);

        File folder = new File("d:\\Work\\Scrapper_door_GIT_new\\dailymotion.com\\images");
        File[] listOfFiles = folder.listFiles();

        //for (int index = 0; index < SECONDS_TO_RUN_FOR * FRAME_RATE; index++) {
        BufferedImage screen = ImageIO.read(new File("d:\\Work\\Scrapper_door_GIT_new\\dailymotion.com\\images\\article_1.jpg"));
        BufferedImage bgrScreen = convertToType(screen, BufferedImage.TYPE_3BYTE_BGR);
        
        BufferedImage screen2 = ImageIO.read(new File("d:\\Work\\Scrapper_door_GIT_new\\dailymotion.com\\images\\article_2.jpg"));
        BufferedImage bgrScreen2 = convertToType(screen2, BufferedImage.TYPE_3BYTE_BGR);
        for(int i = 0; i <3599; i++){
        	writer.encodeVideo(0, bgrScreen, i, TimeUnit.SECONDS);
        }
        //writer.encodeVideo(0, bgrScreen2, 3599, TimeUnit.SECONDS);
        writer.encodeVideo(0, bgrScreen2, 3599, TimeUnit.SECONDS);
        writer.encodeVideo(0, bgrScreen2, 3600, TimeUnit.SECONDS);
        // tell the writer to close and write the trailer if needed
        writer.flush();
        writer.close();
        System.out.println("Video Created");

    }*/
    
    public static void main(String[] args) throws IOException {
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
}

