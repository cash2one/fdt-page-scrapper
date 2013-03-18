package com.fdt.scrapper;

public class Account {
    private String login = "";
    private String pass = "";
    private String blogName = "";
    
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
}
