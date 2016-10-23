package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.unknownType;

import java.util.HashMap;

import com.onmobile.apps.ringbacktones.Gatherer.Utility;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.LinkedSubsriberLookup;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.SiteQueueComponent;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.ViralSMSTableImpl;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContext;

public class UnknownCopyQueueComponent extends SiteQueueComponent
{
    public UnknownCopyQueueComponent(ViralSMSTableImpl viralSMSTableImpl, LinkedSubsriberLookup linkedSubsriberLookup)
    {
        super(viralSMSTableImpl, linkedSubsriberLookup);
    }

    @Override 
    public void failed(QueueContext queContext, Throwable e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void executeQueueComponent(QueueContext queueContext)
    {
        try
        {
            ViralSMSTableImpl vst = getViralSMS();
            String extraInfoStr = vst.extraInfo();
			HashMap<String, String> viralInfoMap = DBUtility.getAttributeMapFromXML(extraInfoStr);
			String keyPressed = "NA";
			if (vst.type().equalsIgnoreCase(Utility.COPY) || vst.type().equalsIgnoreCase(Utility.COPYCONFIRM))
				keyPressed = "s";
			else if (vst.type().equalsIgnoreCase(Utility.COPYCONFIRMED))
				keyPressed = "s9";
			if (viralInfoMap != null && viralInfoMap.containsKey(Utility.KEYPRESSED_ATTR))
				keyPressed = viralInfoMap.get(Utility.KEYPRESSED_ATTR);
			
            Utility.logCopyrequest(vst, keyPressed, "UNKNOWN");
        
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
