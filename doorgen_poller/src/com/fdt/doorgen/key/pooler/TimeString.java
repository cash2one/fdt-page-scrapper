package com.fdt.doorgen.key.pooler;

import java.util.Random;

public class TimeString {
	private static final Random rnd = new Random();

	public static final TimeString DEFAUL_TM_STR = new TimeString(10,20);
	
	private int min = 7;
	private int max = 15;

	public TimeString(int min, int max) {
		super();
		if(min <= max){
			this.min = min;
			this.max = max;
		}else{
			this.min = max;
			this.max = min;
		}
	}

	public TimeString(int min) {
		super();
		this.min = min;
		this.max = min;
	}

	public static TimeString parseTmSrt(String str) {
		String values[] = str.split("-");
		if(values.length == 1){
			return new TimeString(Integer.parseInt(values[0]));
		}else if(values.length == 2){
			return new TimeString(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
		}else{
			throw new NumberFormatException("Input string does not satisfied required format <number>[-<number>]. Input string: " + str);
		}
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + max;
		result = prime * result + min;
		return result;
	}

	public boolean isCountSatisfied(int count){
		return count >= min && count <=max;
	}

	public int getRndCnt(){
		return min + rnd.nextInt(1+max-min);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimeString other = (TimeString) obj;
		if (max != other.max)
			return false;
		if (min != other.min)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return min + "-" + max;
	}
	
	
}
