package com.fdt.doorgen.key.pooler.content;

import com.fdt.doorgen.key.pooler.content.impl.*;

public enum ContentStrategy {
	
	VTOPAX_RU("VTOPAX_RU",true,false, new VtopaxStrategyPoller()),
	HUMAN_CAPITAL_CR_COM("HUMAN_CAPITAL_CR_COM",false,true,new HumanCapitalcrStrategyPoller()),
	VTOPAXMIRA_RU("VTOPAXMIRA_RU",true,true,new VtopaxMiraRuStrategyPoller()),
	USALOANS("USALOANS",true,true,new UsaLoansVtopaxStrategyPoller());
	
	//String strategy name
	private String srtgName = "DEFAULT";
	
	//should we use randomly keys mix
	private boolean mixKeys = true;
	
	//true - append to exist content, false - replace with new
	private boolean appendContent = false;
	
	private StrategyPoller srtgPoller;
	
	private ContentStrategy(StrategyPoller srtgPoller) 
	{
		this.srtgPoller = srtgPoller;
	}
	
	private ContentStrategy(String srtgName, boolean mixKeys,
			boolean appendContent, StrategyPoller srtgPoller) {
		this.srtgName = srtgName;
		this.mixKeys = mixKeys;
		this.appendContent = appendContent;
		this.srtgPoller = srtgPoller;
	}

	public static ContentStrategy getByName(String strgName){
		for(ContentStrategy strg : ContentStrategy.values()){
			if(strg.getSrtgName().equals(strgName)){
				return strg;
			}
		}
		
		return null;
	}

	private ContentStrategy() {
	}

	public String getSrtgName() {
		return srtgName;
	}

	public boolean isMixKeys() {
		return mixKeys;
	}

	public boolean isAppendContent() {
		return appendContent;
	}

	public StrategyPoller getSrtgPoller() {
		return srtgPoller;
	}
}
