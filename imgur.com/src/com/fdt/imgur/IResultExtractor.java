package com.fdt.imgur;

public interface IResultExtractor {
	public String getResult();
	public void init(String responseStr);
}
