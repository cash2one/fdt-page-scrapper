package com.fdt.filemapping;

public class RowScope 
{
	private int first = 1;
	private int last = 1;

	public RowScope(int first, int last) 
	{
		super();
		this.first = first;
		this.last = last;
	}

	public RowScope(int first) {
		super();
		this.first = first;
		this.last = first;
	}

	public static RowScope parseTmSrt(String str) {
		String values[] = str.split("-");
		if(values.length == 1){
			return new RowScope(Integer.parseInt(values[0]));
		}else if(values.length == 2){
			return new RowScope(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
		}else{
			throw new NumberFormatException("Input string does not satisfied required format <number>[-<number>]. Input string: " + str);
		}
	}

	public int getFirst() {
		return first;
	}

	public void setFirst(int first) {
		this.first = first;
	}

	public int getLast() {
		return last;
	}

	public void setLast(int last) {
		this.last = last;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + first;
		result = prime * result + last;
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
		RowScope other = (RowScope) obj;
		if (first != other.first)
			return false;
		if (last != other.last)
			return false;
		return true;
	}
}
