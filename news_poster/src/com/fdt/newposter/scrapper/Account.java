package com.fdt.newposter.scrapper;

public class Account {
    private String login = "";
    private String pass = "";
    private String groupId = "";
    private String cookie = "";
    
    public Account(String login, String pass, String groupId) {
	super();
	this.login = login;
	this.pass = pass;
	this.groupId = groupId;
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

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    }

    public String getCookie()
    {
        return cookie;
    }

    public void setCookie(String cookie)
    {
        this.cookie = cookie;
    }
}
