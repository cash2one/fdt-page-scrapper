package com.fdt.doorgen.key.pooler.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

public class DoorUtils {
	
	public static final long DAY_MIL_SEC_CNT = 24*60*60*1000;
	public static final long YEAR_MIL_SEC_CNT = 24*60*60*365*1000;
	private static Random rnd = new Random();
	
	public static String cleanString(String input)
	{
		StringBuffer output = new StringBuffer(input);

		return output.toString().replaceAll("[^0-9a-zA-Zà-ÿÀ-ß\\s\\%\\$\\-]+", "").replaceAll("\\s+", " ");
	}
	
	public static String getFirstSmblUpper(String input)
	{
		StringBuffer output = new StringBuffer(input.substring(1).toLowerCase());
		output.insert(0, input.substring(0, 1).toUpperCase());

		return output.toString();
	}
	
	public static ArrayList<Integer> getRandomSequense(int seqSize){
		ArrayList<Integer> rndSeq = new ArrayList<Integer>();
		for(int i = 0; i < seqSize; i++){
			rndSeq.add(i);
		}

		Collections.shuffle(rndSeq);

		return rndSeq;
	}

	public static int[] getRndBlocksSize(int blockCnt, int blockSize){
		Random rnd = new Random();
		int[] result = new int[blockCnt];

		for(int i = 0; i < blockCnt; i++){
			result[i] = 1 + rnd.nextInt(blockSize);
		}

		return result;
	}

	public static int arraySum(int[] array){
		int sum = 0;
		for(Integer elem : array){
			sum += elem;
		}

		return sum;
	}
	
	public static long getRndNormalDistTime(){
		
		double gaus = rnd.nextGaussian();
		while(Math.abs(gaus) > 2){
			gaus = rnd.nextGaussian();
		}
		
		long time = (long)Math.round(DAY_MIL_SEC_CNT*(2+gaus)/4);
		
		return time;
	}
	
	public static long getStartOfDay(long time) {
		Date date = new Date(time);
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    calendar.set(Calendar.HOUR_OF_DAY, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MILLISECOND, 0);
	    return calendar.getTime().getTime();
	}
	
/*	//TODO Processing time zone here
	public static long getStartOfDay(long time, TimeZone timeZone) {
		Date date = new Date(time);
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    calendar.set(Calendar.HOUR_OF_DAY, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MILLISECOND, 0);
	    return calendar.getTime().getTime();
	}*/
	
	public static long calibratePostDate(long postTime, long curTime){
		if(postTime < curTime){
			return postTime + DAY_MIL_SEC_CNT;
		}else{
			return postTime;
		}
	}
}
