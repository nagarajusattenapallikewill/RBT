package com.onmobile.apps.ringbacktones.subscriptions;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.ViralSMSTable;

public class CommonCopyThread extends Thread
{
	private static Logger logger = Logger.getLogger(CommonCopyThread.class);
	
	private CommonCopyHelper i_ccHelper = null;
	
	public CommonCopyThread(CommonCopyHelper cch)
	{
		i_ccHelper = cch;
	}
	public void run()
	{
		logger.info("entering..");
		while(i_ccHelper != null && i_ccHelper.isAlive())
		{
			//Tools.logDetail(i_className, method, "Entering run ");
			ViralSMSTable vst = null;
			synchronized (i_ccHelper.i_copyRequestList)
			{
				try
				{
					while(i_ccHelper.i_copyRequestList.size() == 0 )
						i_ccHelper.i_copyRequestList.wait();
					Object obj = i_ccHelper.i_copyRequestList.get(0);
					if(obj.toString().startsWith("Processing"))
						continue;
					else
					{
						vst = (ViralSMSTable)obj;
						i_ccHelper.i_copyRequestList.remove(0);
						i_ccHelper.i_copyRequestList.add("Processing"+vst.toString());
					}
				}
				catch(Exception e)
				{
					logger.error("", e);
				}
			}
			i_ccHelper.processTransfer(vst);
			synchronized (i_ccHelper.i_copyRequestList)
			{
				i_ccHelper.i_copyRequestList.remove("Processing"+vst.toString());
			}
		}
		
	}
}
