package com.fdt.scrapper;

import com.fdt.scrapper.proxy.ProxyConnector;

public class Account {
    private String login = "";
    private String pass = "";
    private String cookie = "";
    private String formName = "";
    private ProxyConnector proxyConnector = null;
    
    public Account(String login, String pass, ProxyConnector proxyConnector) {
	super();
	this.login = login;
	this.pass = pass;
	this.proxyConnector = proxyConnector;
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

    public String getCookie()
    {
        return cookie;
    }

    public void setCookie(String cookie)
    {
        this.cookie = cookie;
    }

    public ProxyConnector getProxyConnector()
    {
        return proxyConnector;
    }

    public void setProxyConnector(ProxyConnector proxyConnector)
    {
        this.proxyConnector = proxyConnector;
    }

    public String getFormName()
    {
        return formName;
    }

    public void setFormName(String formName)
    {
        this.formName = formName;
    }
}
