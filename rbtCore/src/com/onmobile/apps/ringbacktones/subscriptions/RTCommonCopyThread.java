package com.onmobile.apps.ringbacktones.subscriptions;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.ViralSMSTable;

public class RTCommonCopyThread extends Thread
{
	private static Logger logger = Logger.getLogger(RTCommonCopyThread.class);
	
	private RTCommonCopyHelper i_rtccHelper = null;
	
	public RTCommonCopyThread(RTCommonCopyHelper rtcch)
	{
		i_rtccHelper = rtcch;
	}
	public void run()
	{
		logger.info("entering..");
		while(i_rtccHelper != null && i_rtccHelper.isAlive())
		{
			//logger.info("Entering run ");
			ViralSMSTable vst = null;
			
			synchronized (i_rtccHelper.i_rtCopyRequestList)
			
			{
				logger.info("before  processing request list " + i_rtccHelper.i_rtCopyRequestList);
				try
				{
					while(i_rtccHelper.i_rtCopyRequestList.size() == 0 )
						i_rtccHelper.i_rtCopyRequestList.wait();
					logger.info("before  processing request list  part 2 " + i_rtccHelper.i_rtCopyRequestList);
					Object obj = i_rtccHelper.i_rtCopyRequestList.get(0);
					if(obj.toString().startsWith("Processing"))
						continue;
					else
					{
						logger.info("before  processing request list  part 3 " + i_rtccHelper.i_rtCopyRequestList);
						vst = (ViralSMSTable)obj;
						i_rtccHelper.i_rtCopyRequestList.remove(0);
						i_rtccHelper.i_rtCopyRequestList.add("Processing"+vst.toString());
					}
				}
				catch(Exception e)
				{
					logger.error("", e);
				}
			}
			i_rtccHelper.processTransfer(vst);
			logger.info("after  processing request list " + i_rtccHelper.i_rtCopyRequestList);
			synchronized (i_rtccHelper.i_rtCopyRequestList)
			{
				i_rtccHelper.i_rtCopyRequestList.remove("Processing"+vst.toString());
			}
		}
		
	}
}
