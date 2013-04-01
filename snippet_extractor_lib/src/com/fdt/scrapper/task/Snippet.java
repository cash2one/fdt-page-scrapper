package com.fdt.scrapper.task;

public class Snippet {
    private String title = "";
    private String content = "";
    private String link = "";
    
    public Snippet(String title, String content)
    {
	super();
	this.title = title;
	this.content = content;
    }
    
    public Snippet(String title, String content, String link)
    {
	super();
	this.title = title;
	this.content = content;
	this.link = link;
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
    
    public String getLink()
    {
        return link;
    }

    public void setLink(String link)
    {
        this.link = link;
    }

    @Override
    public String toString(){
	StringBuilder result = new StringBuilder();
	result.append("<h3>").append(title).append("</h3>\r\n").append(content);
	return result.toString();
    }
}
