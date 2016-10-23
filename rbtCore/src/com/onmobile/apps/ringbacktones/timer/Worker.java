/*
 * Created on Apr 2, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.onmobile.apps.ringbacktones.timer;

import java.util.Timer;

import org.apache.log4j.Logger;

/**
 * @author manoj.jaiswal
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Worker {
    
	private static Logger logger = Logger.getLogger(Worker.class);
    private BatchUpdateActivity activity;
    
    public Worker(BatchUpdateActivity updateActivity) {
        logger.info("Worker :: In Constructor");
        this.activity = updateActivity;
        Timer timer = new Timer();   
        timer.schedule(new AsyncActivityExecutor(timer,activity), activity.getStartTime());
    }
}
