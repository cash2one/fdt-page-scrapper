package com.fdt.scrapper;

import java.util.ArrayList;
import java.util.HashMap;

public class Domain {
	private String name = "";
	private int count = 0;
	//key - subdomen, value - index in subDomainsList
	private HashMap<String, Integer> subDomainsIndexList = null;
	private ArrayList<Domain> subDomainsList = null;

	public HashMap<String, Integer> getSubDomainsIndexList()
	{
		return subDomainsIndexList;
	}

	public Domain(String url, boolean isChild){
		this.name = url;
		count++;
		if(!isChild){
			subDomainsIndexList = new HashMap<String, Integer>();
			subDomainsList = new ArrayList<Domain>();
		}
	}

	public void addSubDomain(String subDomain){
		if(subDomainsIndexList.containsKey(subDomain)){
			subDomainsList.get(subDomainsIndexList.get(subDomain)).incCount();
		}else{
			subDomainsList.add(new Domain(subDomain, true));
			subDomainsIndexList.put(subDomain,subDomainsList.size()-1);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void incCount() {
		++count;
	}

	public void decCount() {
		--count;
	}

	public ArrayList<Domain> getSubDomainsList()
	{
		return subDomainsList;
	}

	public int getSubDomainCount(String subDomain) {
		if(subDomainsList.contains(subDomain)){
			return subDomainsList.get(subDomainsIndexList.get(subDomain)).getCount();
		}
		return 0;
	}

	public static String extractSecondaryDomain(String url){
		StringBuilder domain = new StringBuilder();
		if(url.lastIndexOf(".") > 0){
			domain.append(url.substring(url.lastIndexOf(".")));
			url = url.substring(0,url.lastIndexOf("."));
			if(url.lastIndexOf(".") > 0){
				domain.insert(0,url.substring(url.lastIndexOf(".")+1));
			}
			else{
				domain.insert(0, url);
			}
		}
		return domain.toString();
	}

	/*public int getUrlLevel(){
		if(subDomain == null){
			return 1;
		}else{
			return 1+subDomain.getUrlLevel();
		}
	}
	
	public String toString(){
		if(subDomain == null){
			return name;
		}else{
			return subDomain.toString() + DOT_VALUE + name;
		}
	}
	
	public String getUrlByDomainLevel(int level) throws ScrapperException{
		if(level <= this.getUrlLevel()){
			level--;
			if(level == 0){
				return name;
			}else{
				return subDomain.getUrlByDomainLevel(level) + DOT_VALUE + name;
			}
		}else{
			throw new ScrapperException("The requested ("+level+") Domain level less than the actual ("+getUrlLevel()+")");
		}
	}*/
}
