/**
 * 
 */
package com.onmobile.apps.ringbacktones.bulkreporter;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;

/**
 * @author vinayasimha.patil
 *
 */
public class ReportDownloader
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			String reports = RBTParametersUtils.getParamAsString(iRBTConstant.REPORTER, "REPORTS", null);
			reports = reports.toUpperCase();
			if(reports.indexOf("BULKACTIVATION") >= 0)
				new BulkActReportDownloader().downloader();
			if(reports.indexOf("RECOMMENDATION") >= 0)
				new RecommendationReportDownloader().downloader();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
