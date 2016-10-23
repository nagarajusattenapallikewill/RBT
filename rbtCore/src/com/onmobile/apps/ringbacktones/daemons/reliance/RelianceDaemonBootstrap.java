/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.reliance;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;

/**
 * @author vinayasimha.patil
 * 
 */
public class RelianceDaemonBootstrap
{
	private static Logger logger = Logger
			.getLogger(RelianceDaemonBootstrap.class);

	private static RbtProvisioningRequestExecutor provisioningRequestExecutor = null;

	public static void start()
	{
		if (provisioningRequestExecutor == null)
		{
			logger.info("Starting RbtProvisioningRequestExecutor");
			provisioningRequestExecutor = new RbtProvisioningRequestExecutor.Builder()
					.build();
		}
	}

	public static void stop()
	{
		if (provisioningRequestExecutor != null)
			provisioningRequestExecutor.shutdownNow();
	}

	public static boolean canStartDeamon()
	{
		ParametersCacheManager parametersCacheManager = CacheManagerUtil
				.getParametersCacheManager();

		String url = parametersCacheManager.getParameterValue(
				iRBTConstant.DAEMON, "SHAZAMS_DOWNLOAD_TUNE_URL", null);
		
		return (url != null);
	}
}
