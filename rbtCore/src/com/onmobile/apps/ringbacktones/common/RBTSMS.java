package com.onmobile.apps.ringbacktones.common;

import java.util.ArrayList;

public interface RBTSMS
{

    public ArrayList getSubscriberSMS(String subscriberID);

    public void deleteOldEntry(int days);

    public void insert(String eventType, String subscriberID,
            String subscriberType, String request, String response,
            String requestenTimestamp, String responseTimeinms,
            String referenceID, String requestDetail, String responseDetail);

}