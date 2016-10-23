package com.onmobile.apps.ringbacktones.servlets;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.smsgateway.accounting.Accounting;

public class UserDetails {
	
public String opParam=null;
public String wstid=null;
public String udid=null;
public String userName=null;
public static String CLASSNAME="OperatorUserDetails";

public UserDetails(String udid,String wstid,String opParam,String userName){
	this.opParam=opParam;
	this.udid=udid;
	this.wstid=wstid;
	this.userName=userName;
}

}
