/**
 * This thread will copy circles from one category to another
 */
package com.onmobile.apps.ringbacktones.daemons;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;

/**
 * @author Sreekar
 *
 * vsreekar@onmobile.com
 */
public class CopyCategoriesFromCircle extends Thread
{
	private static Logger logger = Logger.getLogger(CopyCategoriesFromCircle.class);
	
	String sourceCircle;
	String destinationCircles;
	
	public CopyCategoriesFromCircle()
	{
		Tools.init("COPY_CATEGORIES", true);
		sourceCircle = RBTParametersUtils.getParamAsString("OTHERS", "SOURCE_CIRCLE", null);
		destinationCircles = RBTParametersUtils.getParamAsString("OTHERS", "DESTINATION_CIRCLE", null);
	}
	
	public void run()
	{
		try
		{
			logger.info("RBT::inside run........");
			RBTDBManager dbManager = RBTDBManager.getInstance();
			
			Categories [] allCategories = dbManager.getAllCategoriesForCircle(sourceCircle);

			StringTokenizer stk = new StringTokenizer(destinationCircles, ",");
			
			ArrayList list = new ArrayList();
			while(stk.hasMoreTokens())
			{
				list.add(stk.nextToken());
			}
			String [] m_destinationCirlces = null;
			if(list.size() > 0)
			{
				m_destinationCirlces = (String[])list.toArray(new String[0]);
			}
			if(allCategories != null && allCategories.length > 0 && m_destinationCirlces != null)
			{
				logger.info("RBT:: total number of categories = " + allCategories.length);
				
				for(int j = 0; j < m_destinationCirlces.length; j++)
				{
					logger.info("RBT:: now working with circle " + m_destinationCirlces[j]);
					
					int count = 0;
					int totalCategories = allCategories.length;
					for(int i = 0; i < totalCategories; i++)
					{
						try
						{
							String askMobilenumber = "n";
							if(allCategories[i].askMobileNumber())
								askMobilenumber = "y";
							
							String grammar = allCategories[i].grammar();
							grammar = Tools.findNReplaceAll(grammar, "'", "''");
							Categories cat = dbManager.createCategoryWithId(allCategories[i].id(), allCategories[i].name(), allCategories[i].nameFile(), allCategories[i].previewFile(), grammar, allCategories[i].type(), allCategories[i].index(), askMobilenumber, allCategories[i].greeting(), allCategories[i].startTime(), allCategories[i].endTime(), allCategories[i].parentID(), allCategories[i].classType(), allCategories[i].promoID(), m_destinationCirlces[j], allCategories[i].prepaidYes(), null, null, null);
							if(cat != null)
								count++;
						}
						catch(Exception e)
						{
							logger.info("RBT:: not adding category " + allCategories[i].name() + " for circle " + m_destinationCirlces[j] + ". Got exception" + e.toString());
						}
					}
					logger.info("RBT:: total categories = " + totalCategories  + "& inserted categories = " + count);
				}
			}
			else
			{
				logger.info("RBT::no categories for the source circle " + sourceCircle);
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		CopyCategoriesFromCircle copyThread = new CopyCategoriesFromCircle();
		copyThread.start();
	}
}