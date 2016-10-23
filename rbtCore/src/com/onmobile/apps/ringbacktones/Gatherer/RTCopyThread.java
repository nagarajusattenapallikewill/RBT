package com.onmobile.apps.ringbacktones.Gatherer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.ViralSMSTable;

public class RTCopyThread extends Thread
{
	private static Logger logger = Logger.getLogger(RTCopyThread.class);
	String _class = "RTCopyThread";
	RBTGatherer m_parentThread = null;
	
	public RTCopyThread(RBTGatherer rbtGatherer)
	{
		m_parentThread = rbtGatherer;	
	}
	
	public void run()
	{
		while(m_parentThread != null && m_parentThread.isAlive())
		{
			logger.info("entered while");
			try
			{
				ViralSMSTable vst = null;
				synchronized(RTCopyProcessor.m_pendingRTCopy)
				{
					while(RTCopyProcessor.m_pendingRTCopy.size()==0)
						RTCopyProcessor.m_pendingRTCopy.wait();
					logger.info(RTCopyProcessor.m_pendingRTCopy.get(0).toString());
					if((RTCopyProcessor.m_pendingRTCopy.get(0)).toString().startsWith("PROCESSING")){
						continue; 
                    	}
					vst = (ViralSMSTable)RTCopyProcessor.m_pendingRTCopy.remove(0); 
					RTCopyProcessor.m_pendingRTCopy.add(RTCopyProcessor.m_pendingRTCopy.size(),"PROCESSING"+vst.toString()); 
				}
				m_parentThread.rtCopyProcessor.processRTCopy(vst);
				synchronized(RTCopyProcessor.m_pendingRTCopy) { 
					RTCopyProcessor.m_pendingRTCopy.remove("PROCESSING"+vst.toString()); 
                } 
			}
			catch(InterruptedException ie)
			{
				ie.printStackTrace();
				logger.error("", ie);
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("", e);
				
			}
			catch(Throwable t)
			{
				t.printStackTrace();
				logger.error("", t);
			}
		}
	}
}

