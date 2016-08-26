package com.fdt.doorgen.key.pooler.content.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.NotImplementedException;

import com.fdt.doorgen.key.pooler.content.StrategyPoller;
import com.fdt.doorgen.key.pooler.dao.DaoCommon;
import com.fdt.doorgen.key.pooler.util.DoorUtils;

public class VtopaxStrategyPoller extends StrategyPoller {

	public static void main(String... args){
		VtopaxStrategyPoller poller = new VtopaxStrategyPoller();
		List<List<Integer>> resutl = poller.prepareCntntDtlTable(null);
		
		for(List<Integer> row : resutl){
			System.out.println(row.get(0) + ":" + row.get(1));
		}
	}
	
	@Override
	public List<List<Integer>> prepareCntntDtlTable( List<List<Integer>> currentDtlTable ) {
		Random rnd = new Random();
		List<List<Integer>> dtlTbl = new ArrayList<List<Integer>>();
		
		int rndBatchSnpCnt[] = DoorUtils.getRndBlocksSize(3, 3);
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 1; j <= rndBatchSnpCnt[i]; j++)
			{
				//get discription count
				int descCnt = 1+rnd.nextInt(3);
				boolean ifMainNotInserted = true;

				for(int k = 0; k < descCnt; k++)
				{
					List<Integer> row = new ArrayList<Integer>();
					row.add(i*3 + j);
					row.add((ifMainNotInserted || false)?1:0);
					dtlTbl.add(row);
					ifMainNotInserted = false;
				}
			}
		}
		
		return dtlTbl;
	}
}