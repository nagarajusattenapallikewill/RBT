package com.onmobile.apps.ringbacktones.servlets;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

public class GUIDaemon implements Runnable{
	private static Logger logger = Logger.getLogger(GUIDaemon.class);
	private static boolean canRefreshContextElement=false;
	private static ServletContext context=null;

	public GUIDaemon(ServletContext context){
		this.context=context;
	}



	public void run(){
		while(true){
			Calendar cal=Calendar.getInstance();
			Date currentdate=cal.getTime();
			int currenthour=currentdate.getHours();
			if(currenthour>=1 && currenthour<=2 && !canRefreshContextElement){
				logger.info("CCTHREAD::starting contextAttributeReinitialization from run for sc=>"+context);
				ServletListener.contextAttributeReinitialization(context);
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
