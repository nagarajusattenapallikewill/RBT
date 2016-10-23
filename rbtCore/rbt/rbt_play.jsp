<%@ page import = "java.util.*,com.onmobile.apps.ringbacktones.subscriptions.RBTTonePlayerHelper"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.SubscriberStatus"%>
<%! 
	
	RBTTonePlayerHelper rbtTP = null;
	public void jspInit(){
  System.out.println("in jspInit()");
//  javax.servlet.ServletConfig servletConfig = getServletConfig();
//  servletConfig.getServletContext().setAttribute("inittime",""+System.currentTimeMillis());
try
{
	 rbtTP = RBTTonePlayerHelper.init(); 
}
catch(Exception e)
	{
	}
}
%>
<%
String strIP = request.getRemoteAddr();
String strSubID = null;
String strCaller = null;
String strType = null;
String strOutput = null; 
String strFeedFile = null; 

Hashtable shuffleTable = new Hashtable();
SubscriberStatus subscriberStatus = null;

System.out.println("RBT::SUB_ID = " + request.getParameter("SUB_ID") + ", CALLER_ID = " + request.getParameter("CALLER_ID") + ", TYPE = " + request.getParameter("TYPE") + " from " + strIP);

try
{
if(rbtTP != null)
{
	shuffleTable = rbtTP.getShuffleTable();
	strFeedFile = rbtTP.getFeedFile();

	if(rbtTP.isValidServerIP(strIP))
	{ 
		strSubID = request.getParameter("SUB_ID");
		strCaller = request.getParameter("CALLER_ID");
		strType = request.getParameter("TYPE");

		if(strSubID != null)
		{
			strSubID = rbtTP.subID(strSubID);
		}

		if(strCaller != null)
		{
			if(strCaller.startsWith("0"))
			{
				strCaller = strCaller.substring(1);
			}
			if(strCaller.startsWith("+91"))
			{
				strCaller = strCaller.substring(3);
			}
			if(strCaller.startsWith("91"))
			{
				strCaller = strCaller.substring(2);
			}
		}

		if(strSubID != null && strType != null)
		{
			try
			{
				subscriberStatus = rbtTP.getRBTwavFile(strSubID, strCaller, strType, shuffleTable);
			}
			catch(Exception e)
			{
				subscriberStatus = null;
				System.out.println ("RBT::Couldnot get subscriber file "+strSubID);
			}
		}
	}
	try
	{
		if(subscriberStatus != null) 
		{
			if(shuffleTable != null && shuffleTable.containsKey(new Integer(subscriberStatus.categoryID())))
			{
				out.write(subscriberStatus.subscriberFile() + ":S" + subscriberStatus.categoryID() + ":" + subscriberStatus.status());
			}
			else
			{
				out.write(subscriberStatus.subscriberFile() + ":" + subscriberStatus.categoryID() + ":" + subscriberStatus.status());
			}
			System.out.println("RBT::SUB_ID = " + strSubID + ", CALLER_ID = " + strCaller + ", TYPE = " + strType + ", Response->" +subscriberStatus.subscriberFile());
		}
	}
	catch(Exception e)
	{
		System.out.println ("RBT::Unable to get subscriber file for "+strSubID);
	}

	try
	{
		if(rbtTP.isValidServerIP (strIP)) 
		{
			rbtTP.doPlayerHangUp(strSubID, subscriberStatus, shuffleTable);
		}
	}
	catch(Exception e)
	{
		System.out.println ("RBT::Unable to do player hangup processing for "+strSubID);
	}
}
}
catch(Throwable t)
{
	System.out.println ("RBT::Caught exception in rbt_play.jsp " +t.getMessage());
}
%>