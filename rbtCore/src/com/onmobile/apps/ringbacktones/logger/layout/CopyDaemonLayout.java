package com.onmobile.apps.ringbacktones.logger.layout;

import org.apache.log4j.PatternLayout;
  
public class CopyDaemonLayout extends PatternLayout
{  
    @Override  
    public String getHeader()
    {  
        return "CALLED,CALLER,SONG,CATEGORY,COPY_TIME,CALLER_TYPE,CALLER_SUBSCRIBED_AT_COPY,COPY_DONE,SMS_TYPE,KEY_PRESSED,COPY_TYPE,COPY_MODE"+System.getProperty("line.separator");  
    }  
}