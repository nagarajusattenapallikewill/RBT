package com.onmobile.apps.ringbacktones.daemons.interoperator;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.ContentInterOperatorDaemon;
import com.onmobile.apps.ringbacktones.daemons.interoperator.threads.FTPInterOperatorCopyDaemon;
import com.onmobile.apps.ringbacktones.daemons.interoperator.threads.InterOperatorDBFetcher;
import com.onmobile.apps.ringbacktones.daemons.interoperator.threads.InterOperatorDBFetcherMBean;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;

public class InterOperatorCopyDaemon extends Thread 
{
	static private Logger logger = Logger.getLogger(InterOperatorCopyDaemon.class);
	private static List<Integer> operatorList = new ArrayList<Integer>();
	private static MBeanServer mbs = null;

	@Override
	public void run()
	{
		main(null);
	}
	public static void main(String[] args)
	{
		try
		{
			logger.info("InterOperatorCopyDaemon Started !!");
			listLiveOperators();
			if(operatorList.size() == 0)
			{
				logger.error("No live operators found. Exiting..");
				return;
			}
			mbs = ManagementFactory.getPlatformMBeanServer();
		       
			//resolve the operator id
			startMnpPushingThreads();
			//resolve the content mapping
			startContentResolvingThreads();
			//send the request to respective operators
			startOperatorPushingThreads();
			//clean up
			startCleanUpThreads();
			//process the failed MNP call back requests
			startFtpPullThread();
			ContentInterOperatorDaemon contentInterOperatorDaemon = new ContentInterOperatorDaemon();
			contentInterOperatorDaemon.main(null);
		}
		catch(Exception e)
		{
			logger.error("Exception in main thread ", e);
		}
	}

	private static void startFtpPullThread()
	{
		FTPInterOperatorCopyDaemon ftpThread = new FTPInterOperatorCopyDaemon();
		ftpThread.setName("FTPPullThread");
		ftpThread.start();
	}

	private static void listLiveOperators()
	{
		Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "LIVE_OPERATORS", null);
		if(parameter == null || parameter.getValue() == null || parameter.getValue().trim().length() == 0)
			return;
		StringTokenizer stk = new StringTokenizer(parameter.getValue().trim(), ",");
		while(stk.hasMoreTokens())
			operatorList.add(Integer.parseInt(stk.nextToken()));
		logger.info("operatorList="+operatorList);
	}

	private static void startMnpPushingThreads()
	{
			InterOperatorDBFetcher dbFetcher = new InterOperatorDBFetcher(0,0);
			dbFetcher.setName("MnpPush-Db-Fetcher");
			dbFetcher.start();
			registerMBean(dbFetcher, "MNPPush", dbFetcher.getName());
	}

	private static void startCleanUpThreads()
	{
			InterOperatorDBFetcher dbFetcher = new InterOperatorDBFetcher(0,1);
			dbFetcher.setName("CleanUp-Db-Fetcher");
			dbFetcher.start();
			registerMBean(dbFetcher, "CleanUp", dbFetcher.getName());
	}

	private static void startContentResolvingThreads()
	{
		for (int  operatorId : operatorList)
		{
			InterOperatorDBFetcher dbFetcher = new InterOperatorDBFetcher(operatorId,2);
			dbFetcher.setName("ContentResolver-Db-Fetcher-Opr-"+operatorId);
			dbFetcher.start();
			registerMBean(dbFetcher, "ContentResolve", dbFetcher.getName());
		}
	}

	private static void startOperatorPushingThreads()
	{
		for (int  operatorId : operatorList)
		{
			InterOperatorDBFetcher dbFetcher = new InterOperatorDBFetcher(operatorId,4);
			dbFetcher.setName("OprPush-Db-Fetcher-Opr-"+operatorId);
			dbFetcher.start();
			registerMBean(dbFetcher, "OprPush", dbFetcher.getName());
		}	
	}
	
	private static void registerMBean(InterOperatorDBFetcher dbFetcher, String groupName, String  fetcherName)
	{
		try
		{
			String completeName = "RDCCopy:group=";
			completeName += groupName;
			completeName += ",name=";
			completeName += fetcherName;
			ObjectName objectName = new ObjectName(completeName);
		    mbs.registerMBean(dbFetcher, objectName);
		}
		catch (InstanceAlreadyExistsException e)
		{
			e.printStackTrace();
		}
		catch (MBeanRegistrationException e)
		{
			e.printStackTrace();
		}
		catch (NotCompliantMBeanException e)
		{
			e.printStackTrace();
		}
		catch (MalformedObjectNameException e)
		{
			e.printStackTrace();
		} catch (NullPointerException e)
		{
			e.printStackTrace();
		}
	}
	
}
