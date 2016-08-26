package com.fdt.doorgen.key.pooler.content.impl;

import java.util.ArrayList;
import java.util.List;

import com.fdt.doorgen.key.pooler.content.StrategyPoller;

public class HumanCapitalcrStrategyPoller extends StrategyPoller {

	public static void main(String... args){
		List<List<Integer>> test = new ArrayList<List<Integer>>();
		ArrayList<Integer> row = new ArrayList<Integer>();
		/*row.add(1);
		row.add(1);
		test.add(row);row = new ArrayList<Integer>();
		row.add(4);
		row.add(1);
		test.add(row);row = new ArrayList<Integer>();
		row.add(7);
		row.add(1);
		test.add(row);row = new ArrayList<Integer>();
		row.add(2);
		row.add(1);
		test.add(row);row = new ArrayList<Integer>();
		row.add(5);
		row.add(1);
		test.add(row);row = new ArrayList<Integer>();
		row.add(8);
		row.add(1);*/
		//test.add(row);
		HumanCapitalcrStrategyPoller poller = new HumanCapitalcrStrategyPoller();
		List<List<Integer>> resutl = poller.prepareCntntDtlTable(test);
		
		for(List<Integer> snglRow : resutl){
			System.out.println(snglRow.get(0) + ":" + snglRow.get(1));
		}
	}
	
	@Override
	public List<List<Integer>> prepareCntntDtlTable( List<List<Integer>> currentDtlTable ) {

		List<List<Integer>> dtlTbl = new ArrayList<List<Integer>>();

		int maxIdx = getMaxIndex(currentDtlTable);

		int idxShift = maxIdx % 3;

		for(int i = 0; i < 3; i++)
		{
			List<Integer> row = new ArrayList<Integer>();
			row.add(i*3 + idxShift + 1);
			row.add(1);
			dtlTbl.add(row);
		}

		return dtlTbl;
	}

	private int getMaxIndex(List<List<Integer>> dtlTbl){
		int max = 0;
		for(List<Integer> row : dtlTbl){
			if(row.get(0) > max){
				max = row.get(0) ;
			}
		}

		return max;
	}
}