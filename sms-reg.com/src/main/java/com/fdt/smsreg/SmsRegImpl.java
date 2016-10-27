package com.fdt.smsreg;

import com.fdt.smsreg.enums.OPSTATE;

public class SmsRegImpl implements ISmsReg {
	
	private String url = "";

	@Override
	public Object[] getNum(String country, String service, String appId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] setReady(int tzid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getState(int tzid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response[] getOperations(OPSTATE opstate, int count) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] setOperationOk(int tzid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] setOperationRevise(int tzid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] setOperationOver(int tzid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getNumRepeat(int tzid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] setOperationUsed(int tzid) {
		// TODO Auto-generated method stub
		return null;
	}

}
