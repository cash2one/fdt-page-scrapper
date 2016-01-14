package com.fdt.doorgen.key.pooler.content;

public enum ContentStrategy {
	
	DEFAULT(),
	RANDOM_REPLACEMENT_3_3_3_3_TRUE_FALSE("RANDOM_REPLACEMENT_3_3_3_3_TRUE_FALSE",3,3,3,3,true,false),
	RANDOM_APPEND_3_3_1_1_FALSE_TRUE("RANDOM_APPEND_3_3_1_1_FALSE_TRUE",3,3,1,1,false,true);
	
	//String strategy name
	private String srtgName = "DEFAULT";
	//content block count
	private int mnBlockCnt = 3;
	//max block size per main block. getting random value from 1 to blockSize
	private int blockSize = 3;
	//max block count that will be populated per post
	private int blockCntPerPost = 3;
	//max description count in block 
	private int maxDescCnt = 3;
	
	//should we use randomly keys mix
	private boolean mixKeys = true;
	
	//true - append to exis content, false - replace with new
	private boolean appendContent = false;
	
	private ContentStrategy(String srtgName, int mnBlockCnt, int blockSize,
			int blockSCntPerPost, int maxDescCnt, boolean mixKeys,
			boolean appendContent) {
		this.srtgName = srtgName;
		this.mnBlockCnt = mnBlockCnt;
		this.blockSize = blockSize;
		this.blockCntPerPost = blockSCntPerPost;
		this.maxDescCnt = maxDescCnt;
		this.mixKeys = mixKeys;
		this.appendContent = appendContent;
	}



	public static ContentStrategy getByName(String strgName){
		for(ContentStrategy strg : ContentStrategy.values()){
			if(strg.getSrtgName().equals(strgName)){
				return strg;
			}
		}
		
		return ContentStrategy.DEFAULT;
	}

	private ContentStrategy() {
	}

	public String getSrtgName() {
		return srtgName;
	}

	public int getMnBlockCnt() {
		return mnBlockCnt;
	}

	public int getBlockSize() {
		return blockSize;
	}

	public int getBlockCntPerPost() {
		return blockCntPerPost;
	}

	public boolean isMixKeys() {
		return mixKeys;
	}

	public int getMaxDescCnt() {
		return maxDescCnt;
	}

	public boolean isAppendContent() {
		return appendContent;
	}
	
	
}
