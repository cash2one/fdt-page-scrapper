package com.fdt.scrapper.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;

import javax.xml.xpath.XPathExpressionException;

public class ProxyConnector
{
    String adress;
    int port;
    
    public ProxyConnector(String adress, int port){
	super();
	this.adress = adress;
	this.port = port;
    }
   
    public Proxy getConnect() throws IOException, XPathExpressionException{
        SocketAddress addr = new InetSocketAddress(adress, port);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
        return proxy;
    }
    
    public String getProxyKey(){
	return adress + ":" + port;
    }

    public String getAdress()
    {
        return adress;
    }

    public int getPort()
    {
        return port;
    }
}
