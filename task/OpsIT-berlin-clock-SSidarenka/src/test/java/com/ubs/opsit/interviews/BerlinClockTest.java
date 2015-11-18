package com.ubs.opsit.interviews;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.ubs.opsit.interviews.exception.InvalidTimeFormatException;

public class BerlinClockTest {
	
	private BerlinClock berlinClock;
	
	@Before
	public void setUp(){
		berlinClock = new BerlinClock();
	}
	
	//test null input
	@Test(expected=NullPointerException.class)
	public void testNullInput(){
		berlinClock.convertTime(null);
	}
	
	//test invalid input format
	@Test(expected=InvalidTimeFormatException.class)
	public void testInvalidTimeFormat4AnyString(){
		berlinClock.convertTime("time_11:25:57");
	}
	
	//test for invalid hours value
	@Test(expected=InvalidTimeFormatException.class)
	public void testInvalidTimeFormat4MistakenFormatHours(){
		berlinClock.convertTime("29:12:34");
	}
	
	//test for invalid minutes value
	@Test(expected=InvalidTimeFormatException.class)
	public void testInvalidTimeFormat4MistakenFormatMinutes(){
		berlinClock.convertTime("13:92:34");
	}
	
	//test for invalid seconds value
	@Test(expected=InvalidTimeFormatException.class)
	public void testInvalidTimeFormat4MistakenFormatSeconds(){
		berlinClock.convertTime("09:22:94");
	}
	
	//test for Midnight
	@Test
	public void testMidnightTime(){
		String berlinFormat = berlinClock.convertTime("00:00:00");
		assertEquals("Y\r\nOOOO\r\nOOOO\r\nOOOOOOOOOOO\r\nOOOO", berlinFormat);
	}
	
	//test for 1 second before midnight
	@Test
	public void test1SecBeforeMidnightTime(){
		String berlinFormat = berlinClock.convertTime("23:59:59");
		assertEquals("O\r\nRRRR\r\nRRRO\r\nYYRYYRYYRYY\r\nYYYY", berlinFormat);
	}
	
	//test for another representation of midnight
	@Test
	public void testMidnightTime24h(){
		String berlinFormat = berlinClock.convertTime("24:00:00");
		assertEquals("Y\r\nRRRR\r\nRRRR\r\nOOOOOOOOOOO\r\nOOOO", berlinFormat);
	}
	
	@Test
	public void testMiddleOfTheAfternoon(){
		String berlinFormat = berlinClock.convertTime("13:17:01");
		assertEquals("O\r\nRROO\r\nRRRO\r\nYYROOOOOOOO\r\nYYOO", berlinFormat);
	}
	
	@Test
	public void testEarlyMorning(){
		String berlinFormat = berlinClock.convertTime("05:43:01");
		assertEquals("O\r\nROOO\r\nOOOO\r\nYYRYYRYYOOO\r\nYYYO", berlinFormat);
	}
	
	@Test
	public void testAfternoon(){
		String berlinFormat = berlinClock.convertTime("12:00:00");
		assertEquals("Y\r\nRROO\r\nRROO\r\nOOOOOOOOOOO\r\nOOOO", berlinFormat);
	}
}
