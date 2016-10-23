<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper,java.util.Hashtable,com.onmobile.apps.ringbacktones.common.Tools"%><%
try
{
String strIP  = request.getRemoteAddr();
String strName  = request.getRemoteHost();
String actInfo = strIP + ":SMS";
String strSubID = null;
String strSmsText = null;
String strCatID = null;
String strMode = null;

String X_ONMOBILE_REASON = "FAILURE";

RBTMOHelper rbtMO = RBTMOHelper.init();
System.out.println("RBT_OBD:  SMS = " + request.getParameter("SMS_TEXT") + ", SUB_ID = " + request.getParameter("SUB_ID") + " strMode "+ request.getParameter("MODE")+", CATEGORY_ID = " + request.getParameter("CAT_ID") + " from " + strIP);

if(rbtMO.isValidIP(strIP) || rbtMO.isValidIP(strName))
{
	
	strSubID = request.getParameter("SUB_ID");
	strMode = request.getParameter("MODE");
	strSmsText = request.getParameter("SMS_TEXT");
	strCatID = request.getParameter("CAT_ID");


if(strMode == null)
	strMode = "OBD";
if(strCatID == null)
	strCatID = "3";
else
{
	try
	{
		Integer.parseInt(strCatID);
	}
	catch(Exception e)
	{
		strCatID = "3";
	}
}
if(strSubID != null)
{
	strSubID = rbtMO.subID(strSubID);
}	

if(strSmsText != null && strSubID != null)
{
	if(rbtMO.isValidSub(strSubID))
	{
		Hashtable result = new Hashtable() ;
		if(strCatID != null)
			result.put("CAT_ID", strCatID);
		if(strMode != null)
			result.put("MODE", strMode);

		if(rbtMO.isVodafoneOCGInvalidSMS(strSmsText.trim()))
		{
			X_ONMOBILE_REASON = "INVALID_SMS";
		}
		else
		{
			int retcode = rbtMO.parseSMSText(strSubID.trim(), strSmsText.trim(), result, false, actInfo, "SONGSET");
			if(result.containsKey("SONG_SET_RESPONSE"))
			{
				X_ONMOBILE_REASON = "SUCCESS";
			}
		}
	}

}
}

System.out.println("RBT::Sub ID->" + strSubID + " SMS->" + strSmsText + " CATEGORY_ID->" + strCatID + " MODE->" +strMode + " SMS Response->" +X_ONMOBILE_REASON);

out.flush();
out.write(X_ONMOBILE_REASON);
}
catch(Exception e)
{
	e.printStackTrace();
}
%>