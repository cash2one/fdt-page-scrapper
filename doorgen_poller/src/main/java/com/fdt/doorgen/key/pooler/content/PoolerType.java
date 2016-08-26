package com.fdt.doorgen.key.pooler.content;

import com.fdt.doorgen.key.pooler.runner.DoorgenPoolerFilesRunner;
import com.fdt.doorgen.key.pooler.runner.DoorgenPoolerSnippetsRunner;

public enum PoolerType {
	
	SNIPPETS("SNIPPETS", new DoorgenPoolerSnippetsRunner()),
	//TODO Implement file pooler
	FILES("FILES",new DoorgenPoolerFilesRunner());
	
	//String strategy name
	private String poolerTypeName = "DEFAULT";
	
	private Pooler poller;
	
	private PoolerType(String poolerTypeName, Pooler poller) {
		this.poolerTypeName = poolerTypeName;
		this.poller = poller;
	}

	public static PoolerType getByName(String strgName){
		for(PoolerType strg : PoolerType.values()){
			if(strg.getPoolerTypeName().equals(strgName)){
				return strg;
			}
		}
		
		return null;
	}

	public Pooler getPoller() {
		return poller;
	}

	public String getPoolerTypeName() {
		return poolerTypeName;
	}
}
