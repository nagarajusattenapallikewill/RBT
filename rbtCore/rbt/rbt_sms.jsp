<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import="java.util.*,com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper"%> <% 
long start = System.currentTimeMillis();
RBTMOHelper rbtMO = RBTMOHelper.init(); 
String strIP  = request.getRemoteAddr();
String strName  = request.getRemoteHost();
String actInfo = strIP + ":SMS";
String strMessage = null;
String strSubID = null;
String strParam = null;
String strPrepaid = null;
String strResult = null;
String strAccess = null;
String strSrc = null;
String strUserInfo = null;
boolean isprepaid;
int retcode = 5;
String X_ONMOBILE_REASON = null;
String smsPromoPrefix = null;
String strTrxId = null;
String shortcode = null;
retcode = 5;
X_ONMOBILE_REASON = null;
strMessage = request.getParameter("SMS_TEXT");
if(request.getParameter("msg") != null && !request.getParameter("msg").equalsIgnoreCase("null"))
	strMessage = request.getParameter("msg");
if(request.getParameter("MESSAGE") != null && !request.getParameter("MESSAGE").equalsIgnoreCase("null"))
	strMessage = request.getParameter("MESSAGE");
strSubID = request.getParameter("SUB_ID");
if(request.getParameter("msisdn") != null && !request.getParameter("msisdn").equalsIgnoreCase("null"))
	strSubID = request.getParameter("msisdn");
if(request.getParameter("MSISDN") != null && !request.getParameter("MSISDN").equalsIgnoreCase("null"))
	strSubID = request.getParameter("MSISDN");
if(request.getParameter("TRX_ID") != null && !request.getParameter("TRX_ID").equalsIgnoreCase("null"))
	strTrxId = request.getParameter("TRX_ID").trim();
if(request.getParameter("SHORTCODE") != null && !request.getParameter("SHORTCODE").equalsIgnoreCase("null"))
	shortcode = request.getParameter("SHORTCODE").trim();
	
strParam = request.getParameter("SMS_PARAM");
strPrepaid = request.getParameter("SUB_TYPE");
strAccess = request.getParameter("ACCESS");
strSrc = request.getParameter("src");
if(strSrc != null && !strSrc.equalsIgnoreCase("null") && rbtMO.m_songCatcherNumberList != null && rbtMO.m_songCatcherNumberList.length > 0 && rbtMO.m_songCatcherNumberList[0] != null && strSrc.length() > rbtMO.m_songCatcherNumberList[0].length() )
{
	for(int i = 0; i < rbtMO.m_songCatcherNumberList.length; i++ )
	{
			if(strSrc.startsWith(rbtMO.m_songCatcherNumberList[i].trim()))		
			{
					strMessage = strMessage.trim() + " " + strSrc.trim().substring(rbtMO.m_songCatcherNumberList[i].trim().length());
					break;
			}
	}
	


}
if(strAccess != null)
	strAccess = strAccess.toLowerCase();
strUserInfo = request.getParameter("USER_INFO");
if(strUserInfo == null || strUserInfo.equalsIgnoreCase("null") || strUserInfo.trim().length() <=0)
	strUserInfo = null;
else 
	strUserInfo = strUserInfo.trim();

boolean sensSMSForAlternateOpr = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SEND_SMS_FOR_ALT_OPR", "FALSE");
System.out.println("RBT: SMS Text = " + strMessage + ", SUB_ID = " + strSubID + " from " + strIP);
rbtMO.addToAccounting("request", request.getParameter("SUB_ID"), request.getParameter("SMS_TEXT"), null, strIP);
try
{
if(rbtMO.isValidIP(strIP) || rbtMO.isValidIP(strName)){
isprepaid = false;

if(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SMS_ACT_PROMO_PREFIX", null) != null)
{
	smsPromoPrefix = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SMS_ACT_PROMO_PREFIX", null).toLowerCase();
}

if(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID") != null)
{
	if(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID").equalsIgnoreCase("prepaid"))
	{
		isprepaid = true;
	}
}

if(strMessage != null)
{
    StringTokenizer message = new StringTokenizer(strMessage, " ");
	strMessage = " ";
	while(message.hasMoreTokens())
    {
        String token = message.nextToken();
		if(smsPromoPrefix != null && token.toLowerCase().startsWith(smsPromoPrefix))
		{
			actInfo =  strIP + ":" + token.trim();
		}
		else
			strMessage = strMessage.trim() + " " + token.trim();

    }

}

if(strMessage != null && strParam != null)
{
	strMessage = strMessage.trim() + " " + strParam.trim();
}

if(strPrepaid != null) 
{
	strPrepaid = strPrepaid.trim().toLowerCase();
	if(strPrepaid.startsWith ("pre")) isprepaid = true;
}
boolean isProfileRequest=false; 
if(strAccess!=null){ 
	   //System.out.println ("RBT::inside sms.jsp::strAccess is not null "); 
	   String strAccessLowerCase=strAccess.toLowerCase(); 
	   isProfileRequest=strAccess != null && strAccessLowerCase.startsWith("profile"); 
	   //System.out.println ("RBT::inside sms.jsp::isProfileRequest=="+isProfileRequest); 
}else{ 
	   //System.out.println ("RBT::inside sms.jsp::strAccess is null "); 
} 

if(strMessage != null && strSubID != null)
{
	if(strAccess == null || (strAccess != null && strAccess.startsWith("ret") && rbtMO.isRetailer(strSubID.trim())) || isProfileRequest )
	{
		if(rbtMO.isRemoteSub(strSubID.trim()))
		{
			X_ONMOBILE_REASON = rbtMO.connectToRemote(strSubID.trim(), strMessage.trim(), false);
		}
		else
		{
			
			strSubID = rbtMO.m_rbtDBManager.subID(strSubID.trim());
			boolean valid = rbtMO.isValidSub(strSubID.trim());
			if(!valid)
			{
				String redirect = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "REDIRECT_REQUEST", "false");
				if(redirect != null && redirect.equalsIgnoreCase("true"))
				{
					System.out.println("RBT::Redirecting Request of SUBSCRIBER "+strSubID);
					HashMap hm = new HashMap();
					hm.put("SUB_ID",strSubID.trim());
					hm.put("SMS_TEXT",strMessage.trim());
					if(shortcode != null)
						hm.put("SHORTCODE",shortcode.trim());
					if(strTrxId != null )
						hm.put("TRX_ID",strTrxId.trim());
					X_ONMOBILE_REASON = rbtMO.connectToRemote(hm, false);
				}
				else
				{
					X_ONMOBILE_REASON = rbtMO.getSMSTextForID(null, "INVALID_PREFIX",rbtMO.m_invalidPrefixDefault);
				}
			}
			else
			{
				if(strPrepaid == null && rbtMO.isValidPrepaidIP(strIP, "SMS"))
					isprepaid = true;
				else if(strPrepaid == null)
				{
					isprepaid = false;
					if(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID") != null)
					{
						if(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID").equalsIgnoreCase("prepaid"))
						{
							isprepaid = true;
						}
					}
				}
				boolean validRequest=true; 
			   if(isProfileRequest){ 
				   //System.out.println ("RBT::inside sms.jsp::going to check for valid profile user"); 
				   boolean validProfileUser=rbtMO.isvalidProfileUser(strSubID.trim()); 
				   //System.out.println ("RBT::inside sms.jsp::validProfileuser=="+validProfileUser); 
				   if(!validProfileUser){ 
						   //System.out.println ("RBT::inside sms.jsp::not a valid Profile user"); 
						   validRequest=false; 
						   String temp = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "INVALID_PROFILE_USER_REQUEST_SMS", null); 
								   X_ONMOBILE_REASON ="This service is not available for you."; 
						   if(temp != null) 
								   X_ONMOBILE_REASON = temp; 
					} 
				} 
				if(validRequest){ 
				   //System.out.println ("RBT::inside sms.jsp::validrequest=="+validRequest); 
					Hashtable result = new Hashtable() ;
					if(strUserInfo != null)
						result.put("MODE",strUserInfo);
					if(strTrxId != null)
						result.put("TRX_ID",strTrxId);
					if(shortcode != null)
						result.put("SHORTCODE",shortcode);
					
					if(isProfileRequest){ 
					   //System.out.println ("RBT::inside sms.jsp::going to call parseSMStext with SMS_PROFILE"); 
					   retcode = rbtMO.parseSMSText(strSubID.trim(), strMessage.trim(), result, isprepaid, actInfo, "SMS_PROFILE"); 
					}else{ 
						retcode = rbtMO.parseSMSText(strSubID.trim(), strMessage.trim(), result, isprepaid, actInfo, "SMS");
					}
					strResult = (String)result.get("Reason");
					X_ONMOBILE_REASON = strResult;
				}
			}
		}
	}
	else
	{
		X_ONMOBILE_REASON = "This service is not available for you.";
			
		if(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SMS_ACCESS_FAILURE_TEXT", null) != null)
			X_ONMOBILE_REASON = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SMS_ACCESS_FAILURE_TEXT", null);
	}
	if(sensSMSForAlternateOpr)
		rbtMO.sendSMS(rbtMO.m_rbtDBManager.subID(strSubID.trim()), X_ONMOBILE_REASON);
}
else
{
	X_ONMOBILE_REASON = "Insufficient Parameters";
}
X_ONMOBILE_REASON = rbtMO.formatResult(strMessage, X_ONMOBILE_REASON, retcode, request.getParameter("TRANS_ID"));
response.setHeader("X-ONMOBILE-STATUS", ""+retcode);
}
else
{
	X_ONMOBILE_REASON = "Invalid IP Address";
}
System.out.println("RBT::Sub ID->" + strSubID + " SMS Text->" + strMessage + " SMS Param->" + strParam + " Sub Type->" +strPrepaid + " SMS Response->" +X_ONMOBILE_REASON);
rbtMO.addToAccounting("response", strSubID, strMessage, X_ONMOBILE_REASON, strIP);

rbtMO.writeTrans(request.getQueryString(), X_ONMOBILE_REASON, ""+(System.currentTimeMillis() - start), strIP);

out.flush();
out.write(X_ONMOBILE_REASON);
}
catch(Throwable t)
{
	System.out.println ("RBT::Caught exception in rbt_sms.jsp " +t.getMessage());
	t.printStackTrace();
	
}
%>