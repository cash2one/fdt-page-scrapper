package com.fdt.registration.account;

public class Account {
    private String login = "";
    private String pass = "";
    private String email = "";
    
    public Account() {
		super();
	}
    
    public Account(String login, String pass,
			String email) {
		super();
		this.login = login;
		this.pass = pass;
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	@Override
	public String toString() {
		return "Account [login=" + login + ", pass=" + pass + ", email="
				+ email + "]";
	}
    
	public String toResultString() {
		return login + ":" + pass + ":" +email;
	}
}

