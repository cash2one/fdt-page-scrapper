package com.fdt.blogssapo.scrapper.task;

public class Snippet extends com.fdt.scrapper.task.Snippet {

	public Snippet(String title, String content)
	{
		super(title, content);
	}

	@Override
	public String toString(){
		StringBuilder result = new StringBuilder();
		result.append("<p>").append("<h3>").append(this.getTitle()).append("</h3>").append(this.getContent()).append("</p>");
		return result.toString();
	}
}
