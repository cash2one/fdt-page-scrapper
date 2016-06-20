package com.fdt.doorgen.key.pooler;

public abstract class AbstractDoorgenPoolerThread<T>
{
	public abstract T threadCall() throws Exception;
}
