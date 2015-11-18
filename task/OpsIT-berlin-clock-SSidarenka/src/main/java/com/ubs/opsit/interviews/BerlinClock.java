package com.ubs.opsit.interviews;

import com.ubs.opsit.interviews.exception.InvalidTimeFormatException;

/**
 * After little investigation of Berlin Clock schema I did conclusion:
 * 
 * All lamps in row lighted up sequentially. 
 * So, we can't have situation, for example, when in the same row lamp#1,lamp#3 
 * are LIGHTED UP, but lamp#2 is LIGHTED OFF. Therefore, we can just calculate 
 * LIGHTED UP lamps count and just LIGHT OFF (print to output symbol 'O') remain lamps.
 * For implementation of algorithm we will use prepared String constants 
 * (This constants represent situation when all lamps in row are LIGHRED UP) and enabled lamps count.
 * 
 * This logic is implemented in the method 
 * 
 * @author SSidarenka
 *
 */
public class BerlinClock implements TimeConverter{
	
	/**
	 * RegEx to validate input time string format HH:MM:SS. Separately it checks value 24:00:00
	 */
	private static final String TIME_PATTERN = "(^(([01][0-9])|([2][0-3])):([0-5][0-9]):([0-5][0-9])$)|(^24:00:00$)";
	
	//new line representation
	private static final String NEW_LINE = "\r\n";
	
	/** Even second's lamp representation */
	private static final String SECOND_EVEN = "Y";
	
	/** Odd second's lamp representation */
	private static final String SECOND_ODD = "O";
	
	/** Representation of first hour's row in the case when all lamps of row light */
	private static final String HOUR_FIRST_ROW = "RRRR";
	
	/** Representation of second hour's row in the case when all lamps of row light */
	private static final String HOUR_SECOND_ROW = "RRRR";
	
	/** Representation of first hour's row in the case when all lamps of row light */
	private static final String MINUTE_FIRST_ROW = "YYRYYRYYRYY";

	/** Representation of second hour's row in the case when all lamps of row light */
	private static final String MINUTE_SECOND_ROW = "YYYY";
	
	
	public String convertTime(String aTime) {
		byte hours, minutes, seconds = 0;
		
		//Validation
		validateTimeFormat(aTime);
		
		StringBuffer berlinFormat = new StringBuffer();
		
		//split input time string into  hours, minutes and seconds values and parse them
		String[] timeValues = aTime.split(":");
		hours = Byte.valueOf(timeValues[0]);
		minutes = Byte.valueOf(timeValues[1]);
		seconds = Byte.valueOf(timeValues[2]);
		
		berlinFormat.append(getSecondsRpr(seconds)).append(NEW_LINE);
		berlinFormat.append(getHoursRpr(hours)).append(NEW_LINE);
		berlinFormat.append(getMinutesRpr(minutes));
		
		return berlinFormat.toString();
	}
	
	/**
	 * Returns SECONDS ROW representation:
	 * @return Y - if second's count is EVEN; O - if second's count is ODD
	 */
	private String getSecondsRpr(byte seconds){
		return seconds % 2 == 0? SECOND_EVEN : SECOND_ODD;
	}
	
	/**
	 * Returns MINUTE'S ROWS representation.
	 * 
	 * @return 
	 */
	private String getMinutesRpr(byte minutes){
		StringBuffer minRpr = new StringBuffer();
		
		minRpr.append(getRprByLmpCount(MINUTE_FIRST_ROW, minutes / 5)).append(NEW_LINE);
		minRpr.append(getRprByLmpCount(MINUTE_SECOND_ROW, minutes % 5));
		
		return minRpr.toString();
	}
	
	/**
	 * Returns HOUR'S ROWS representation.
	 * 
	 * @return 
	 */
	private String getHoursRpr (byte hours){
		StringBuffer hourRpr = new StringBuffer();
		
		hourRpr.append(getRprByLmpCount(HOUR_FIRST_ROW, hours / 5)).append(NEW_LINE);
		hourRpr.append(getRprByLmpCount(HOUR_SECOND_ROW, hours % 5));
		
		return hourRpr.toString();
	}

	/**
	 * Return one time row representation
	 * 
	 * @param allLmpsEnblRpr - Representation of row where all lamps are lighted up
	 * @param enblLmpsCount - Count of lighted up lamps
	 * @return
	 */
	private String getRprByLmpCount(String allLmpsEnblRpr, int enblLmpsCount){
		StringBuffer rowRpr = new StringBuffer(allLmpsEnblRpr);
		
		for(int i = enblLmpsCount; i < allLmpsEnblRpr.length(); i++){
			rowRpr.setCharAt(i, 'O');
		}
		
		return rowRpr.toString();
	}
	
	/**
	 * Validate input value for the required time format 'HH:MM:SS'
	 * @param time
	 * @return input value - if value pass validation; throw InvalidTimeFormatException - in other case
	 */
	private String validateTimeFormat(String time){
		if(time == null){
			throw new NullPointerException("Input value have not to be null");
		}
		if(!time.matches(TIME_PATTERN)){
			throw new InvalidTimeFormatException(
					String.format("Invalid input time value '%s'. You have to use format 'HH:MM:SS'", time)
			);
		}
		return time;
	}
	
	
}
