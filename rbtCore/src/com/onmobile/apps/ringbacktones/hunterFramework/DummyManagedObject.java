package com.onmobile.apps.ringbacktones.hunterFramework;

import com.onmobile.snmp.agentx.client.ManagedObjectCallback;
import com.onmobile.snmp.agentx.client.OID;

public class DummyManagedObject extends ManagedObjectCallback
{
	public DummyManagedObject(String oid)
	{
		super(new OID(oid));
	}

	@Override
	public Object getValue()
	{
	    return Boolean.TRUE;
	}
}
