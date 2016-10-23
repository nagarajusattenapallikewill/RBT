package com.onmobile.apps.ringbacktones.ussd;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;


public class USSDCacheRefresher implements Runnable{
	/*author@Abhinav Anand
	 */
	private static Logger logger = Logger.getLogger(USSDCacheRefresher.class);
	private static boolean canRefreshContextElement=false;
	private static ServletContext context=null;

	public USSDCacheRefresher(ServletContext context){
		this.context=context;
	}



	@SuppressWarnings("deprecation")
	public void run(){
		while(true){
			Calendar cal=Calendar.getInstance();
			Date currentdate=cal.getTime();
			int currenthour=currentdate.getHours();
			if(currenthour>=1 && currenthour<=2 && !canRefreshContextElement){
				logger.info("CCTHREAD::starting contextAttributeReinitialization from run for sc=>"+context);
				USSDServletListner.contextAttributeInitialization(context);
				logger.info("CCTHREAD::finished contextAttributeReinitialization from run for sc=>"+context);
				if(!canRefreshContextElement){
					canRefreshContextElement=true;
					logger.info("CCTHREAD::Thread entering refreshing context attributes" ); 
				}
			}else{
				canRefreshContextElement=false;
			}
			try {
				Thread.sleep(1800000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}

