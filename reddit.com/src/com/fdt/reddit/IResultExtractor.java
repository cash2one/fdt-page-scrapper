package com.fdt.reddit;

public interface IResultExtractor {
	public String getResult();
	public void init(String responseStr);
}
