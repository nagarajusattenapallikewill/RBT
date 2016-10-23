package com.onmobile.apps.ringbacktones.subscriptions;

import java.util.ArrayList;
import java.util.HashMap;

interface SMSProcessInterface
{
    public ArrayList preProcess(HashMap z,ArrayList smsText);

    public Object getInstance() throws Exception;
}