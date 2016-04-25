package com.fdt.postbit.scrapper.task;

public class Snippet extends com.fdt.scrapper.task.Snippet{
	private String title = "";
	private String content = "";

	public Snippet(String title, String content)
	{
		super(title, content);
	}

	@Override
	public String toString(){
		StringBuilder result = new StringBuilder();
		result.append("<p>").append("<h3>").append(title).append("</h3>").append(content).append("</p>");
		return result.toString();
	}
}
