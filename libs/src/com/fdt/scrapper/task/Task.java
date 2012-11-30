package com.fdt.scrapper.task;

public abstract class Task{
	private String url = "";
	private String xPath = "";
	private boolean xmlParce = false;
	protected String result = "";

	public String getResult() {
		return result;
	}

	//Parse result there
	public abstract void setResult(String result);

	public Task(String xPath, boolean xmlParce){
		super();
		this.xPath = xPath;
		this.xmlParce = xmlParce;
	}

	public Task(String xPath){
		super();
		this.xPath = xPath;
		this.xmlParce = false;
	}

	public String getUrlToScrap(){
		return url;
	}

	protected void setUrlToScrap(String url){
		this.url = url;
	}

	public String getxPath(){
		return xPath;
	}

	public void setxPath(String xPath){
		this.xPath = xPath;
	}

	public boolean isXmlParce()
	{
		return xmlParce;
	}

	public void setXmlParce(boolean xmlParce) {
		this.xmlParce = xmlParce;
	}

	public  void setResultAsIs(String result){
		if(result != null){
			this.result = result;
		}
		else{
			this.result = "-1";
		}
	}

	public boolean isResultEmpty(){
		if(result == null || "".equals(result)){
			return true;
		}
		return false;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("URL: ").append(url);
		sb.append("; XPATH: ").append(xPath);
		sb.append("; Source is XML: ").append(xmlParce);
		return sb.toString();
	}
}
