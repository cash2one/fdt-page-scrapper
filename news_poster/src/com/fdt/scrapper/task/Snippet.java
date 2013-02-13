package com.fdt.scrapper.task;

public class Snippet {
    private String title = "";
    private String content = "";
    
    public Snippet(String title, String content)
    {
	super();
	this.title = title;
	this.content = content;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }
    
    @Override
    public String toString(){
	StringBuilder result = new StringBuilder();
	result.append("<p>").append("<h3>").append(title).append("</h3>").append(content).append("</p>");
	return result.toString();
    }
}
