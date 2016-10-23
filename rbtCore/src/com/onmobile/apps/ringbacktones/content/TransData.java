package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

/* This class is related to TransData */
public interface TransData
{
    /* Returns subscriber ID */
    public String subscriberID();

    /* Returns transID */
    public String transID();

    /* Returns trans Date */
    public Date transDate();
    
    public String type();
    
    public String accessCount();
}