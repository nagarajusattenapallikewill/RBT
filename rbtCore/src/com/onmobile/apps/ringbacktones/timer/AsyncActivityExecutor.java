package com.onmobile.apps.ringbacktones.timer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;


import com.onmobile.apps.ringbacktones.utils.SubscriberNoWriter;

public class AsyncActivityExecutor extends TimerTask {

	private static Logger logger = Logger.getLogger(AsyncActivityExecutor.class);
	
    private BatchUpdateActivity updateActivity;
    private Timer timer;

    public AsyncActivityExecutor(Timer timer, BatchUpdateActivity updateActivity) {
        this.timer = timer;
        this.updateActivity = updateActivity; 
        logger.info("AsyncActivityExecutor instantiated >>>");
        logger.info("AsyncActivityExecutor instantiated >>>");
        logger.info("Current Date: " + new Date());
        logger.info("Scheduled Activity Start Time:: " + updateActivity.getStartTime());
        logger.info("Scheduled Activity End Time::   " + updateActivity.getEndTime());
        logger.info("Batch size: " + updateActivity.getBatchSize());
    }
    
    public void run() {
        try {
            logger.info("Executing Task: ");
            execute(updateActivity);
            timer.cancel();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    public void execute(BatchUpdateActivity updateActivity) { 
        SubscriberNoWriter exporter = new SubscriberNoWriter(updateActivity);
        try {
            exporter.export();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
     }    
}

