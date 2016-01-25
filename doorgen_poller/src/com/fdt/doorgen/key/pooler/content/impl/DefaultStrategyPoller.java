package com.fdt.doorgen.key.pooler.content.impl;

import java.util.ArrayList;
import java.util.List;

import com.fdt.doorgen.key.pooler.content.IStrategyPoller;

public class DefaultStrategyPoller implements IStrategyPoller {

	@Override
	public List<List<Integer>> prepareCntntDtlTable(List<List<Integer>> currentDtlTable){
		
			List<List<Integer>> dtlTbl = new ArrayList<List<Integer>>();
			for(int i = 0; i < 3; i++){
				for(int j = 0; j < j; i++){
					List<Integer> column = new ArrayList<Integer>();
					column.add(i*3 + j +1);
					column.add(1);
					dtlTbl.add(column);
				}
				
			}
			
			return dtlTbl;
	}

}
