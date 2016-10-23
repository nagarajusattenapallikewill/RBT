<%@ page import = "java.util.*,com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper,com.onmobile.apps.ringbacktones.content.SubscriberStatus,java.text.SimpleDateFormat,com.onmobile.apps.ringbacktones.content.Subscriber"%><%
RBTMOHelper rbtMO = RBTMOHelper.init(); 
String strIP  = request.getRemoteAddr();
String strName  = request.getRemoteHost();
String strSubID = null;
String strResult = "ERROR:";
SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
strSubID = request.getParameter("subscriber_id");
%>
<%! public static final short NO_ERROR			= 0;	/* Incase of Success */
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
/*
0 : Subscribed
1 : Waiting for Subscribtion
2 : Waiting for unsubscribtion
4 : Unsubscriber
5: subscribed(RBT light)
6: Waiting for subscription(RBT light)
7: Waiting for unsubscription(RBT light)
8:Unsubscriber (RBT light)
9:lifetime subscriber
10: Waiting for subscription(Lifetime)
11: Waiting for unsubscription(Lifetime)
12: Suspended User of  HLR Tick
13: HLR Tick is removed
14: Rental Promotion Pack is existing
15: Ads Subscriber
16: waiting for provisioning Ads RBT
17: waiting for deprovisioning Ads RBT
18: Suspended User of  HLR Tick (Advanced Rental)
19: HLR Tick is removed (Advanced Rental)
20: subscribed(Suffle)
21: Waiting for subscription(Suffle) 
22: Waiting for unsubscription(unsubscription)
23: Suspended User of  Suffle (Shuffle)
24: HLR Tick is removed (Shuffle)
25: subscribed (sampling)
26: waiting for provisioning sampling 
27: waiting for de-provision sampling
28: subscribed(Low Rental)
29: waiting for provision(Low Rental)
30: waiting for de-provision(Low Rental)
*/
try
{
	if(rbtMO!=null)
	{
		Subscriber subscriber = rbtMO.getSubscriber(strSubID);
		if(subscriber == null)
			strResult = "ERROR:"+SUBSCRIBER_DOES_NOT_EXIST;
		else if(subscriber.subYes() == null || subscriber.startDate() == null)
			strResult = "ERROR:"+DB_ERROR;
		else
		{
			String strDate = sdf.format(subscriber.startDate());
			if(subscriber.subYes().equals("A"))
				strResult = "SUCCESS:"+strDate+":"+1;
			else if(subscriber.subYes().equals("N"))
				strResult = "SUCCESS:"+strDate+":"+1;
			else if(subscriber.subYes().equals("B"))
				strResult = "SUCCESS:"+strDate+":"+0;
			else if(subscriber.subYes().equals("D"))
				strResult = "SUCCESS:"+strDate+":"+2;
			else if(subscriber.subYes().equals("P"))
				strResult = "SUCCESS:"+strDate+":"+2;
			else if(subscriber.subYes().equals("E"))
				strResult = "ERROR:"+DB_ERROR;
			else if(subscriber.subYes().equals("F"))
				strResult = "ERROR:"+DB_ERROR;
			else if(subscriber.subYes().equalsIgnoreCase("X"))
				strResult = "SUCCESS:"+strDate+":"+4;
			else if(subscriber.subYes().equalsIgnoreCase("Z"))
				strResult = "SUCCESS:"+strDate+":"+12;
		}
}
else 
	strResult = "ERROR:"+DB_ERROR;
System.out.println ("RBT::Result from rbt_subscriberprofile.jsp " +strResult);
}
catch(Throwable t)
{
	System.out.println ("RBT::Caught exception in rbt_subscriberprofile.jsp " +t.getMessage());
	t.printStackTrace();
		strResult = "ERROR:"+DB_ERROR;
	
}
%>
<%=strResult%>