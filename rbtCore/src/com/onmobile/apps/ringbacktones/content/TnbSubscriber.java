package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

public interface TnbSubscriber
{

	public long seqID();
	 
	public String subID();
	 
	public String circleID();
	 
	public String chargepack();
	 
	public Date startDate();
	
	public int iterID();
	
	public void setSeqID(long seqId);
	 
	public void setSubID(String subId);
	 
	public void setCircleID(String circleId);
	 
	public void setChargepack(String chargePack);
	 
	public void setStartDate(Date startDate);
	
	public void setIterID(int iterId);
	
}
