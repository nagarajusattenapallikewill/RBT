package com.onmobile.apps.ringbacktones.subscriptions;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;

public class WaridRBTMOHelper extends RBTMOHelper implements iRBTConstant{
	private static Logger logger = Logger.getLogger(WaridRBTMOHelper.class);
	
	private String SUBSCRIBER_ID = "SUBSCRIBER_ID";
	private String SUBSCRIBER_OBJ = "SUBSCRIBER_OBJ";
	private String CALLER_ID = "CALLER_ID";
	private String CLIP_OBJECT = "CLIP_OBJECT";
    private String CATEGORY_OBJECT = "CATEGORY_OBJECT";
	
	WaridRBTMOHelper() throws Exception{
		
		super();
	}
	
		public void initializeProcessing(HashMap z, ArrayList smsList)
			throws Exception {
		logger.info("Entering.z is "+z + " and smsList is "+smsList );
		if(smsList.contains("sste") || smsList.contains("sstb") ||smsList.contains("sstes"))
		{
			if(m_catRBTkeyword != null && m_catRBTkeyword.size() >  0)
				smsList.add((String)m_catRBTkeyword.get(0));
		}
		if(smsList.contains("ssrc"))
			smsList.remove("ssrc");
		if(smsList.contains("ssrs"))
		{
			if(m_subMsg != null && m_subMsg.size() >  0)
				smsList.add((String)m_subMsg.get(0));
		}
		if(smsList.contains("ssru"))
		{
			if(m_unsubMsg != null && m_unsubMsg.size() >  0)
				smsList.add((String)m_unsubMsg.get(0));
		}


		}
}
