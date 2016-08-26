package com.fdt.scrapper.task;

public class Snippet {
	private String title = "";
	private String content = "";
	private String link = "";

	public Snippet(){
		super();
	}
	
	public Snippet(String title, String content)
	{
		super();
		this.title = title.replaceAll("((https|http)?:\\/\\/)?(www\\.)?([\\w\\.]+)(\\.[a-zA-Z]{2,6})(\\/[\\w\\.]*)*\\/?", "");;
		this.content = content.replaceAll("((https|http)?:\\/\\/)?(www\\.)?([\\w\\.]+)(\\.[a-zA-Z]{2,6})(\\/[\\w\\.]*)*\\/?", "");;
	}

	public Snippet(String title, String content, String link)
	{
		super();
		this.title = title.replaceAll("((https|http)?:\\/\\/)?(www\\.)?([\\w\\.]+)(\\.[a-zA-Z]{2,6})(\\/[\\w\\.]*)*\\/?", "");;
		this.content = content.replaceAll("((https|http)?:\\/\\/)?(www\\.)?([\\w\\.]+)(\\.[a-zA-Z]{2,6})(\\/[\\w\\.]*)*\\/?", "");;
		this.link = link;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;//.replaceAll("((https|http)?:\\/\\/)?(www\\.)?([\\w\\.]+)(\\.[a-zA-Z]{2,6})(\\/[\\w\\.]*)*\\/?", "");
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;//.replaceAll("((https|http)?:\\/\\/)?(www\\.)?([\\w\\.]+)(\\.[a-zA-Z]{2,6})(\\/[\\w\\.]*)*\\/?", "");
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		Snippet other = (Snippet) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
}
