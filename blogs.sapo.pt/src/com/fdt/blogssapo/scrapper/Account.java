package com.fdt.blogssapo.scrapper;

public class Account {
    private String login = "";
    private String pass = "";
    private String blogName = "";
    private int banCount = 0;
    private static final int MAX_BAN_COUNT = 10;
    
    public Account(String login, String pass, String blogName) {
	super();
	this.login = login;
	this.pass = pass;
	this.blogName = blogName;
    }

    public String getLogin()
    {
        return login;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    public String getPass()
    {
        return pass;
    }

    public void setPass(String pass)
    {
        this.pass = pass;
    }

    public String getBlogName()
    {
        return blogName;
    }

    public void setBlogName(String blogName)
    {
        this.blogName = blogName;
    }
    
    public void incBan(){
    	banCount++;
    }
    
    public void resetBan(){
    	banCount = 0;
    }
    
    public boolean isTotallyBaned(){
    	return banCount > MAX_BAN_COUNT;
    }
}
