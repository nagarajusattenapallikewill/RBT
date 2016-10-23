package com.onmobile.apps.ringbacktones.promotions;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;



public class WorkerThread extends Thread{
	
	private static Logger logger = Logger.getLogger(WorkerThread.class);
	
	public static ParametersCacheManager m_rbtParamCacheManager = null;
	private Vector<String> oscCDRLine;
	private Hashtable<String, Long> nmsCDRLine; 
	
	public WorkerThread(Vector<String> line, Hashtable<String, Long> h) {
		// TODO Auto-generated constructor stub
		oscCDRLine = line;
		nmsCDRLine = h;
		m_rbtParamCacheManager =  CacheManagerUtil.getParametersCacheManager();
	}
		public void run(){
		try {
			while(!oscCDRLine.isEmpty()){
				String line = null;
				synchronized(oscCDRLine)
				{
					if(!oscCDRLine.isEmpty())
						line = oscCDRLine.remove(0);
					else
						continue;
				}
				
				String[] tokens = line.split(";");
				if(tokens.length < 4)
				{
					logger.info("Invalid OSCCDR record "+line);
					continue;
				}
				String caller = tokens[0];
				String called = tokens[1];
				String time = tokens[2];
				String song = tokens[3];
				int clipID = -1;
				String circle = null;
				if((circle = RBTViralMain.isCallerValid(RBTViralMain.subID(caller))) == null)
				{
					logger.info("circleId is null, so skipping off the record");
					continue;
				}
				boolean checkStatus = false;
				Parameters p = m_rbtParamCacheManager.getParameter("VIRAL", "CHK_RBT_LITE_USER");
				if(p != null && p.getValue() != null && p.getValue().equalsIgnoreCase("TRUE"))
					checkStatus = true;
				if(checkStatus){
					boolean isCallerLiteUser=RBTViralMain.isLiteUser(caller);
					boolean isCalledLiteUser=RBTViralMain.isLiteUser(called);
					if(isCallerLiteUser){
						if(!isCalledLiteUser){
							continue;
						}
					}
				}
//				if(!RBTViralMain.isCalledValid(called)){
//					continue;
//				}
				if(!nmsCDRLine.containsKey(caller+"|"+called+"|"+time))
				{
					logger.info(caller+"|"+called+"|"+time + " not found in nms records");
					continue;
				}
				long dur = nmsCDRLine.get(caller+"|"+called+"|"+time); 
				if((clipID = RBTViralMain.isSongNonPromotional(song, dur)) == -1)
				{
					logger.info("Skipping off the record since song is non-promotional");
					continue;
				}
				
				caller = RBTViralMain.subID(caller);
				called = RBTViralMain.subID(called);
				if(!RBTViralMain.addViral(caller, called, ""+clipID))
					continue;
				
				RBTViralMain.sendViralPromotion(caller, called, clipID, circle, time);
			}
				
			
		} catch (Throwable t) {
			// TODO Auto-generated catch block
			logger.error("", t);
		}
	}

}
