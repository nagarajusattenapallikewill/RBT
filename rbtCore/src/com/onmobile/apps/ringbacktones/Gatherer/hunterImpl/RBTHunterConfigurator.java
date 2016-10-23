package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.hunterFramework.HunterConfigurator;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;

public class RBTHunterConfigurator extends HunterConfigurator
{
	private static Logger logger = Logger.getLogger(RBTHunterConfigurator.class);
	
    @Override
    public int getParameterValue(String paramName, int defaultValue)
    {
        try
        {
            RBTConnector rbtConnector = RBTConnector.getInstance();
            int paramValue = rbtConnector.getInstance().getRbtGenericCache().getIntParameter(iRBTConstant.GATHERER, paramName, defaultValue);
            return paramValue;
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        return super.getParameterValue(paramName, defaultValue);
    }
}
