package com.fdt.scrapper.util;

public class ResultParserFilter
{
    private long minDomainCount = Long.MIN_VALUE;
    private long maxAlexaRank = Long.MAX_VALUE;
    private long minAllIndex = Long.MIN_VALUE;
    private long minWeekIndex = Long.MIN_VALUE;

    public ResultParserFilter() {
	super();
	minDomainCount = Long.MIN_VALUE;
	maxAlexaRank = Long.MAX_VALUE;
	minAllIndex = Long.MIN_VALUE;
	minWeekIndex = Long.MIN_VALUE;
    }

    public ResultParserFilter(long minDomainCount, long maxAlexaRank, long minAllIndex, long minWeekIndex) {
	super();
	this.minDomainCount = minDomainCount;
	this.maxAlexaRank = maxAlexaRank;
	this.minAllIndex = minAllIndex;
	this.minWeekIndex = minWeekIndex;
    }

    public long getMinDomainCount() {
	return minDomainCount;
    }

    public void setMinDomainCount(int minDomainCount) {
	this.minDomainCount = minDomainCount;
    }

    public long getMaxAlexaRank() {
	return maxAlexaRank;
    }

    public void setMaxAlexaRank(int maxAlexaRank) {
	this.maxAlexaRank = maxAlexaRank;
    }

    public long getMinAllIndex() {
	return minAllIndex;
    }

    public void setMinAllIndex(int minAllIndex) {
	this.minAllIndex = minAllIndex;
    }

    public long getMinWeekIndex() {
	return minWeekIndex;
    }

    public void setMinWeekIndex(int minWeekIndex) {
	this.minWeekIndex = minWeekIndex;
    }

    public boolean filterExist() {
	if(minDomainCount == Long.MIN_VALUE &&
		maxAlexaRank == Long.MAX_VALUE &&
		minAllIndex == Long.MIN_VALUE &&
		minWeekIndex == Long.MIN_VALUE){
	    return false;
	}
	return true;
    }
}
