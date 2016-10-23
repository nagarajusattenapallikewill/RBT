<%@ page import = "java.util.*,com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper,com.onmobile.apps.ringbacktones.content.SubscriberStatus,com.onmobile.apps.ringbacktones.content.Subscriber"%><%
RBTMOHelper rbtMO = RBTMOHelper.init(); 
String strIP  = request.getRemoteAddr();
String strName  = request.getRemoteHost();
String strSubID = null;
String strCallerID = null;
String strResult = "ERROR:";
strSubID = request.getParameter("subscriber_id");
strCallerID = request.getParameter("caller_id");
%>
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
try
{
	if(rbtMO!=null)
	{
	Subscriber subscriber = rbtMO.getSubscriber(strSubID);
	boolean isPrepaid = true;
	if(subscriber != null)
		isPrepaid = subscriber.prepaidYes();
	String playUncharged = "ALL";
	if(rbtMO.localSitePrefix != null && !rbtMO.localSitePrefix.playUncharged(isPrepaid))
		playUncharged = "NONE";

	if(subscriber == null || !rbtMO.isSubActive(subscriber))
		strResult = "ERROR:"+SUBSCRIBER_DOES_NOT_EXIST;
	else
	{
		SubscriberStatus subscriberStatus = rbtMO.getSubscriberFile(strSubID,
                                                              strCallerID,
                                                              playUncharged,rbtMO.isMemCacheModel,0);
	String subWavFile = null;
	if(subscriberStatus != null && subscriberStatus.subscriberFile() != null)
	{
		subWavFile = subscriberStatus.subscriberFile().trim();
		if(subWavFile.indexOf("rbt_") != -1)
			subWavFile = subWavFile.substring(4);
		if(subWavFile.indexOf("_rbt") != -1)
			subWavFile = subWavFile.substring(0,subWavFile.length()-4);
		strResult = "SUCCESS:"+subWavFile;
	}
	else
		strResult = "ERROR:"+RECORD_NOT_FOUND;
	}
}
else 
	strResult = "ERROR:"+DB_ERROR;
System.out.println ("RBT::Result from rbt_tonecopy.jsp " +strResult);
}
catch(Throwable t)
{
	System.out.println ("RBT::Caught exception in rbt_tonecopy.jsp " +t.getMessage());
	t.printStackTrace();
	strResult = "ERROR:"+SYNTAX_ERROR;
}
%>
<%=strResult%>