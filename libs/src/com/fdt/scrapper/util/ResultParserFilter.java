package com.fdt.scrapper.util;

public class ResultParserFilter
{
    private int minDomainCount = Integer.MIN_VALUE;
    private int maxAlexaRank = Integer.MAX_VALUE;
    private int minAllIndex = Integer.MIN_VALUE;
    private int minWeekIndex = Integer.MIN_VALUE;

    public ResultParserFilter() {
	super();
	minDomainCount = Integer.MIN_VALUE;
	maxAlexaRank = Integer.MAX_VALUE;
	minAllIndex = Integer.MIN_VALUE;
	minWeekIndex = Integer.MIN_VALUE;
    }

    public ResultParserFilter(int minDomainCount, int maxAlexaRank, int minAllIndex, int minWeekIndex) {
	super();
	this.minDomainCount = minDomainCount;
	this.maxAlexaRank = maxAlexaRank;
	this.minAllIndex = minAllIndex;
	this.minWeekIndex = minWeekIndex;
    }

    public int getMinDomainCount() {
	return minDomainCount;
    }

    public void setMinDomainCount(int minDomainCount) {
	this.minDomainCount = minDomainCount;
    }

    public int getMaxAlexaRank() {
	return maxAlexaRank;
    }

    public void setMaxAlexaRank(int maxAlexaRank) {
	this.maxAlexaRank = maxAlexaRank;
    }

    public int getMinAllIndex() {
	return minAllIndex;
    }

    public void setMinAllIndex(int minAllIndex) {
	this.minAllIndex = minAllIndex;
    }

    public int getMinWeekIndex() {
	return minWeekIndex;
    }

    public void setMinWeekIndex(int minWeekIndex) {
	this.minWeekIndex = minWeekIndex;
    }

    public boolean filterExist() {
	if(minDomainCount == Integer.MIN_VALUE &&
		maxAlexaRank == Integer.MAX_VALUE &&
		minAllIndex == Integer.MIN_VALUE &&
		minWeekIndex == Integer.MIN_VALUE){
	    return false;
	}
	return true;
    }
}
