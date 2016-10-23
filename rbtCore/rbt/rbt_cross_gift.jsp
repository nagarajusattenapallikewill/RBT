<%@ page import = "com.onmobile.apps.ringbacktones.content.database.RBTDBManager,com.onmobile.apps.ringbacktones.content.Clips,com.onmobile.apps.ringbacktones.common.Tools,com.onmobile.apps.ringbacktones.cache.content.ClipMinimal,com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper"%>
<%! public static final short NO_ERROR			= 0;	/*Incase of Success*/
public static final short SUSPEND_PROCESS_STATUS	= 0x9e;	/*Incase of Subscriber De-Provisioning fails or suspended*/
public static final short SUSPEND_STATUS		= 0x01;	/*Incase of Insufficient Balance*/
public static final short DATABASE_DOWN			= 0x92;	/*Incase of DataBase is down */
public static final short POLY_DB_NOT_CONNECTED		= 171;	/*Incase of Polyhedra Database is not connected*/ 
public static final short RECORD_NOT_FOUND		= 0x91;	/*Incase of subscriber record is not found*/
public static final short RECORD_EXISTS_ALREADY		= 0x93;	/*Incase of a request made to add a entry , that already exit*/
public static final short ILLIGAL_DATE_TIME		= 0x70;	/*Incase of Invalid date format*/
public static final short SUBSCRIBER_DOES_NOT_EXIST	= 0x82;	/*Incase of request for subscriber that doesnot exist*/
public static final short ILLIGAL_WEEKDAY		= 5;	/*Incase of Invalid WEEDAY*/
public static final short INVALID_PACKET		= 0x01;	/*Incase of Content of the packet is malformed */
public static final short PASSWORD_NOT_MATCHED		= 0x89;	/*Incase of Incorrect password*/
public static final short DB_ERROR			= 0x9F;	/*Incase of any kind of error related to database*/
public static final short SUBSCRIBER_GIFT_PENDING	= 55;	/*Incase of presenting a gift to a non-HT subscriber already having one gift*/
public static final short GIFT_INBOX_FULL		= 56;	/*Incase of presenting one more gift to HT subscriber already having 5 gift in the inbox*/
public static final short SYNTAX_ERROR			= 1001;	/*Incase of malformed request*/
%>
<%
String sResponse = "ERROR:";
try
{
	RBTMOHelper rbtMO = RBTMOHelper.init();
	RBTDBManager rbtDBManager = RBTDBManager.getInstance();
	String subscriberId = request.getParameter("subscriber_id");
	String callerID = request.getParameter("caller_id");
	String wavFile = request.getParameter("wav_file");
	String clipID = null;
	ClipMinimal clip = null;
	if(wavFile != null)
		clip = rbtDBManager.getClipRBT("rbt_"+wavFile.trim()+"_rbt");
	if(clip != null)
		clipID = clip.getClipId()+"";
	String smsType = request.getParameter("sms_type");
	if(smsType == null || smsType.equalsIgnoreCase("null"))
		smsType = "GIFT_CHARGED";

	if(rbtDBManager == null)
		sResponse = sResponse + DB_ERROR;
	else if(subscriberId == null || callerID == null)
		sResponse += SYNTAX_ERROR;
	else
	{
		if(clipID != null || !clipID.equalsIgnoreCase("null"))
		{
			rbtDBManager.insertViralSMSTableMap(subscriberId, null, smsType, callerID, clipID, 0, null, null, null);
			sResponse = "SUCCESS";
		}
		else
				sResponse = "ERROR:"+SYNTAX_ERROR;
	}
}
catch(Exception e)
{
	Tools.logException("rbt_cross_gift.jsp", "RBT::Exception caught ", e);
	sResponse = "ERROR:"+SYNTAX_ERROR;
}
%><%=sResponse%>