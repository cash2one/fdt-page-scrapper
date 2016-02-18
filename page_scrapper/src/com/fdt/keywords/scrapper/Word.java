package com.fdt.keywords.scrapper;

import java.util.Comparator;

public class Word implements Comparable<Word>, Comparator<Word>, IEncoder
{
	private String value;
	private int count = 1;
	
	private IEncoder encoder;
	
	public Word(String value) {
		super();
		this.encoder = this;
		this.value = value.toLowerCase();
	}
	
	public Word(String value, IEncoder encoder) {
		super();
		this.encoder = encoder;
		this.value = value.toLowerCase();
	}

	public void incCount(){
		count++;
	}

	public String getValue() {
		return value;
	}

	public int getCount() {
		return count;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + count;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		Word other = (Word) obj;
		
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equalsIgnoreCase(other.value))
			return false;
		
		return true;
	}

	@Override
	public int compareTo(Word arg0) {
		return arg0.count - this.count;
	}

	@Override
	public int compare(Word arg0, Word arg1) {
		return arg0.compareTo(arg1);
	}

	@Override
	public String toString() {
		return "Word [value=" + value + ", count=" + count + ", code=" + encoder.encode(value) +"]";
	}
	
	public Object[] toObjArray(){
		return new Object[]{false, value, count, encoder.encode(value)};
	}
	
	public IEncoder getEncoder() {
		return encoder;
	}

	public void setEncoder(IEncoder encoder) {
		this.encoder = encoder;
	}

	@Override
	public String encode(String value) {
		return value;
	}

	@Override
	public String decode(String value) {
		return value;
	}
}
