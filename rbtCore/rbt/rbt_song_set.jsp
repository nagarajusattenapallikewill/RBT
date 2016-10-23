<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "java.util.*,com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper,com.onmobile.apps.ringbacktones.common.Tools"%><%
String strIP  = request.getRemoteAddr();
String actInfo = strIP + ":SMS";
String strMessage = null;
String strSubID = null;
String strResult = null;
String strTrxID = null;
String X_ONMOBILE_REASON = null;
RBTMOHelper rbtMO = RBTMOHelper.init(); 
String strName  = request.getRemoteHost();
String smsPromoPrefix = null; 
if(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SMS_ACT_PROMO_PREFIX", null) != null) { 
	smsPromoPrefix = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SMS_ACT_PROMO_PREFIX", null); 
} 
System.out.println("RBT: SMS Text = " + request.getParameter("SMS_TEXT") + ", SUB_ID = " + request.getParameter("SUB_ID") + ", TRANS_ID = " + request.getParameter("TRANS_ID") + " from " + strIP);
rbtMO.addToAccounting("request", request.getParameter("SUB_ID"), request.getParameter("SMS_TEXT"), null, strIP);
//try
//{
	if(rbtMO.isValidIP(strIP) || rbtMO.isValidIP(strName)){
		strMessage = request.getParameter("SMS_TEXT");
		strSubID = request.getParameter("SUB_ID");
		strTrxID = request.getParameter("TRANS_ID");
		if(strMessage != null && strSubID != null && strTrxID != null && !strTrxID.equalsIgnoreCase("null"))
		{
			StringTokenizer message = new StringTokenizer(strMessage, " "); 
			strMessage = " "; 
			while(message.hasMoreTokens()) 
			{ 
				   String token = message.nextToken(); 
				   if(smsPromoPrefix != null && token.toLowerCase().startsWith(smsPromoPrefix)) 
						   actInfo =  "RET:" + token.trim(); 
				   else 
						   strMessage = strMessage.trim() + " " + token.trim(); 
			} 
			if(rbtMO.isRemoteSub(strSubID.trim()))
			{
				X_ONMOBILE_REASON = rbtMO.connectToRemoteForSM(strSubID.trim(), strMessage.trim(), strTrxID);
			}
			else
			{
			
				boolean valid = rbtMO.isValidSub(strSubID.trim());
				Hashtable result = new Hashtable() ;
				result.put("TRXID", strTrxID);
				if(!valid || rbtMO.isVodafoneOCGInvalidSMS(strMessage.trim()))
				{
					X_ONMOBILE_REASON = rbtMO.getSMSTextForID(null, "HELP", rbtMO.m_helpDefault);
				}
				else
				{
					rbtMO.parseSMSText(strSubID.trim(), strMessage.trim(), result, false, actInfo, "SONGSET");
					X_ONMOBILE_REASON = (String)result.get("Reason");
				}
			}
		}
		else
		{
			X_ONMOBILE_REASON = "Insufficient Parameters";
		}
	}
	else
	{
		X_ONMOBILE_REASON = "Invalid IP Address";
	}

	System.out.println("RBT::Sub ID->" + strSubID + " SMS Text->" + strMessage + " SMS Response->" +X_ONMOBILE_REASON);
rbtMO.addToAccounting("response", strSubID, strMessage, X_ONMOBILE_REASON, strIP);
	out.flush();
	out.write(X_ONMOBILE_REASON);
//}
//catch(Throwable t)
//{
//	System.out.println ("RBT::Caught exception in rbt_sms.jsp " +t.getMessage());
//}
%>