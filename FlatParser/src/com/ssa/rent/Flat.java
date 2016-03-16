package com.ssa.rent;
import java.io.Serializable;

public class Flat implements Serializable
{
    private static final long serialVersionUID = 2298579030135000022L;
    private String price = "";
    private String title = "";
    private String adress = "";
    private String detailInfo = "";
    private boolean isAgent = false;
    private String phone = "";
    private String postDate = "";
    private byte roomCount = 1;

    public Flat(String title, String adress, String price, String detailInfo){
	super();
	this.price = price;
	this.title = title;
	this.adress = adress;
	this.detailInfo = detailInfo;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDetailInfo()
    {
        return detailInfo;
    }

    public void setDetailInfo(String detailInfo)
    {
        this.detailInfo = detailInfo;
    }

    public boolean isAgent()
    {
        return isAgent;
    }

    public void setAgent(boolean isAgent)
    {
        this.isAgent = isAgent;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public void setPrice(String price)
    {
        this.price = price;
    }

    public void setAdress(String adress)
    {
        this.adress = adress;
    }

    public String getPrice() {
	return price;
    }

    public String getAdress() {
	return adress;
    }

    public String getPostDate()
    {
        return postDate;
    }

    public void setPostDate(String postDate)
    {
        this.postDate = postDate;
    }

    public byte getRoomCount()
    {
        return roomCount;
    }

    public void setRoomCount(byte roomCount)
    {
        this.roomCount = roomCount;
    }

    @Override
    public String toString(){
	StringBuilder res = new StringBuilder();
	res.append("<b>Заголовок: </b>");
	res.append(title);
	res.append("; <br><b>Цена:</b> ");
	res.append(price);
	res.append("; <br><b>Адресс:</b> ");
	res.append(adress);
	res.append("; <br><b>Объявление:</b> ");
	res.append(detailInfo);
	res.append("; <br><b>Телефон:</b> ");
	res.append(phone);
	res.append("; <br><b>Дата подачи:</b> ");
	res.append(postDate);
	res.append("<HR size=2 color=#000000><br>");
	return res.toString();
    }

    @Override
    public boolean equals(Object obj){
	boolean compare = false;
	if(obj instanceof Flat){
	    Flat entry = (Flat)obj;
	    if( 	this.getAdress().equalsIgnoreCase(entry.getAdress()) && 
		    this.getPrice().equalsIgnoreCase(entry.getPrice())&&
		    this.getDetailInfo().equalsIgnoreCase(entry.getDetailInfo())&&
		    this.getTitle().equalsIgnoreCase(entry.getTitle())&&
		    this.getRoomCount() == entry.getRoomCount()&&
		    (this.isAgent() ==  entry.isAgent())&&
		    this.getPostDate().equalsIgnoreCase(entry.getPostDate()) &&
		    this.getPhone().equalsIgnoreCase(entry.getPhone()))
	    {
		return true;
	    }
	}
	return compare;
    }
}
