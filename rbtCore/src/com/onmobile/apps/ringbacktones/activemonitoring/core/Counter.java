package com.onmobile.apps.ringbacktones.activemonitoring.core;

import java.util.Date;

import com.onmobile.snmp.agentx.client.ManagedObjectCallback;


/**
 * @author vasipalli.sreenadh
 *
 */

public class Counter extends ManagedObjectCallback
{
	private com.onmobile.snmp.agentx.client.OID Oid = null;
	private String counterName = null;
	private int counter = 0;
	private Date date = null;
	
	public Counter(com.onmobile.snmp.agentx.client.OID OID, String counterName)
	{
		super(OID);
		this.counterName = counterName;
	}
	
	/**
	 * @return the oID
	 */
	public com.onmobile.snmp.agentx.client.OID getOid() {
		return Oid;
	}

	/**
	 * @param oID the oID to set
	 */
	public void setOid(com.onmobile.snmp.agentx.client.OID oID) {
		Oid = oID;
	}
	
	/**
	 * @return the counterName
	 */
	public String getCounterName() {
		return counterName;
	}

	/**
	 * @param counterName the counterName to set
	 */
	public void setCounterName(String counterName) {
		this.counterName = counterName;
	}

	/**
	 * @return the counter
	 */
	public int getCounter() {
		return counter;
	}

	/**
	 * @param counter the counter to set
	 */
	public void setCounter(int counter) {
		this.counter = counter;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public Object getValue() 
	{
		return counter;
	}
	
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Counter[Oid=");
		builder.append(Oid);
		builder.append(", counter=");
		builder.append(counter);
		builder.append(", counterName=");
		builder.append(counterName);
		builder.append(", date=");
		builder.append(date);
		builder.append("]");
		return builder.toString();
	}

	
	
	
}
