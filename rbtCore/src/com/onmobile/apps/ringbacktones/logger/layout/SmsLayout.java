package com.onmobile.apps.ringbacktones.logger.layout;

import org.apache.log4j.PatternLayout;
  
public class SmsLayout extends PatternLayout
{  
    @Override  
    public String getHeader()
    {  
        return "REQUEST PARAMS|RESPONSE|TIME DELAY|REQ IP"+System.getProperty("line.separator");  
    }  
}