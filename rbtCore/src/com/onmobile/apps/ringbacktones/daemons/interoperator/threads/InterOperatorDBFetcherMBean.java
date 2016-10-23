package com.onmobile.apps.ringbacktones.daemons.interoperator.threads;

import java.util.ArrayList;

import com.onmobile.apps.ringbacktones.daemons.interoperator.bean.InterOperatorCopyRequestBean;

public interface InterOperatorDBFetcherMBean
{
	public String getOperator();
	
	public int getStatus();
	
	public int getFetchSize();
	
	public int getSleepTimeInSec();
	
	public int getCopyQueueSize();
	
	public int getPendingQueueSize();
	
	public String showCopyQueueRecords();
	
	public String showPendingQueueRecords();
	
}
