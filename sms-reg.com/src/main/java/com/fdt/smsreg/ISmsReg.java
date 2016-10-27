package com.fdt.smsreg;

import com.fdt.smsreg.enums.OPSTATE;

public interface ISmsReg 
{
	/**
	 * return string[0] - responce(1-OK, others - error msg)
	 * return string[1] - tzid id
	 */
	public Object[] getNum(String country, String service, String appId);
		
	/**
	 * 
	 * @param tzid
	 * @return object[0] - 1 -- OK, others - error msg
	 */
	public Object[] setReady(int tzid);
	
	/**
	 * 
	 * @param tzid
	 * @return object[0] - RESPONSE
	 * return object[1] - SERVICE
	 * return object[2] - String on number
	 * return object[3] - Code from sms
	 */
	public Object[] getState(int tzid);
	
	
	public Response[] getOperations(OPSTATE opstate, int count);
	
	/**
	 * return string[0] - responce(1-OK, others - error msg)
	 * return string[1] - tzid id
	 */
	public Object[] setOperationOk(int tzid);
	
	/**
	 * return string[0] - responce(1-OK, others - error msg)
	 * return string[1] - tzid id
	 */
	public Object[] setOperationRevise(int tzid);
	
	/**
	 * return string[0] - responce(1-OK, others - error msg)
	 * return string[1] - tzid id
	 */
	public Object[] setOperationOver(int tzid);	
	
	/**
	 * return string[0] - responce(
						    0 — повтор по указанной операции невозможен;
						    1 — запрос выполнен успешно;
						    2 — номер оффлайн, используйте метод getNumRepeatOffline;
						    3 — Этот номер сейчас занят. Попробуйте позже.
						
							NEWTZID = id новой операции операции.
	)
	 * return string[1] - new tzid id
	 */
	public Object[] getNumRepeat(int tzid);
	
	/**
	 * return string[0] - responce(1-OK, others - error msg)
	 * return string[1] - tzid id
	 */
	public Object[] setOperationUsed(int tzid);	
	
}
