<%@page session="false"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTWebHelper,com.onmobile.apps.ringbacktones.common.Tools,java.util.*,java.io.*"%>

<%
String strSubID = null;
String strPrepaid = null;
String strRequestType = null;
String strRequestValue = null;
String callerID = null;
String circleId=null;
String categoryID= null;
String rbt = null;
int frmTime = 0;
int toTime = 23;

StringBuffer strBuffer = new StringBuffer();

boolean isprepaid = false;

RBTWebHelper rbtWeb = null;

try
{
	rbtWeb = new RBTWebHelper();
}
catch(Exception e)
{
	Tools.logException("RBT_WEB","RBT_WEB.jsp",e);
}
System.out.println("RBT: WEB INCOMING REQUEST = " + request.getParameter("REQUEST_TYPE") + " from SubscriberID = " + request.getParameter("SUB_ID"));

strSubID = request.getParameter("SUB_ID");
strPrepaid = request.getParameter("SUB_TYPE");
strRequestType = request.getParameter("REQUEST_TYPE");
strRequestValue = request.getParameter("REQUEST_VALUE");
circleId=request.getParameter("CIRCLE_ID");

if(strRequestType == null || rbtWeb == null)
{
	Tools.logStatus("RBT_WEB","RBT_WEB.jsp","strRequestType is " + strRequestType + " and rbtWeb is " + rbtWeb);
	strBuffer = new StringBuffer();
	strBuffer.append("Error");
}
else
{
	boolean isPrepaid = false;
	if(strPrepaid != null && strPrepaid.equalsIgnoreCase("true"))
	{
		isPrepaid = true;
	}
	
	if(strRequestType.equalsIgnoreCase("status"))
	{
		try
		{
			strBuffer = rbtWeb.getSubscriber(strSubID.trim());
		}
		catch(Exception e)
		{
			Tools.logException("RBT_WEB","RBT_WEB.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
		
	}
	if(strRequestType.equalsIgnoreCase("act"))
	{
		if(strRequestValue == null)
		{
			Tools.logStatus("RBT_WEB","RBT_WEB.jsp","strRequestValue is " + strRequestType);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
		else
		{
			boolean pre = false;
			if(strRequestValue.equalsIgnoreCase("Prepaid"))
			{
				pre = true;
			}
			try
			{
				strBuffer = rbtWeb.activateSubscriber(strSubID.trim(), pre);
			}
			catch(Exception e)
			{
				Tools.logException("RBT_WEB","RBT_WEB.jsp",e);
				strBuffer = new StringBuffer();
				strBuffer.append("Error");
			}
		}
	}
	if(strRequestType.equalsIgnoreCase("deact"))
	{
		try
		{
			
			strBuffer = rbtWeb.deactivateSubscriber(strSubID.trim());
		}
		catch(Exception e)
		{
			Tools.logException("RBT_WEB","RBT_WEB.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}
	if(strRequestType.equalsIgnoreCase("set"))
	{
		if(strRequestValue == null)
		{
			Tools.logStatus("RBT_WEB","RBT_WEB.jsp","strRequestValue is " + strRequestValue);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
		else
		{
			StringTokenizer token = new StringTokenizer(strRequestValue, " ");
			if(token.hasMoreTokens())
			{
				callerID = token.nextToken().trim();
				if(callerID.equalsIgnoreCase("ALL"))
				{
					callerID = null;
				}

			}
			if(token.hasMoreTokens())
			{
				categoryID = token.nextToken().trim();
			}
			if(token.hasMoreTokens())
			{
				rbt = token.nextToken().trim();
			}
			try
			{
				frmTime = Integer.parseInt(token.nextToken().trim());
				toTime = Integer.parseInt(token.nextToken().trim());
			}
			catch(Exception e)
			{
			
			}
			try
			{
				strBuffer = rbtWeb.addSelections(strSubID, callerID, Integer.parseInt(categoryID), rbt, frmTime, toTime);
			}
			catch(Exception e)
			{
				Tools.logException("RBT_WEB","RBT_WEB.jsp",e);
				strBuffer = new StringBuffer();
				strBuffer.append("Error");
			}
		}
	}
	
	if(strRequestType.equalsIgnoreCase("search"))
	{
		try
		{
			strBuffer = rbtWeb.searchSong(strRequestValue);
		}
		catch(Exception e)
		{
			Tools.logException("RBT_WEB","RBT_WEB.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}
	
	if(strRequestType.equalsIgnoreCase("sms"))
	{
		try
		{
			strBuffer = rbtWeb.sendSMS(strSubID.trim(), strRequestValue);
		}
		catch(Exception e)
		{
			Tools.logException("RBT_WEB","RBT_WEB.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}

	if(strRequestType.equalsIgnoreCase("deact"))
	{
		try
		{
			strBuffer = rbtWeb.deactivateSubscriber(strSubID.trim());
		}
		catch(Exception e)
		{
			Tools.logException("RBT_WEB","RBT_WEB.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}
	if(strRequestType.equalsIgnoreCase("del"))
	{
		if(strRequestValue == null)
		{
			Tools.logStatus("RBT_WEB","RBT_WEB.jsp","strRequestValue is " + strRequestType);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
		else
		{
			if(strRequestValue.equalsIgnoreCase("ALL"))
			{
				strRequestValue = null;
			}

			try
			{
				strBuffer = rbtWeb.removeSelection(strSubID, strRequestValue);
			}
			catch(Exception e)
			{
				Tools.logException("RBT_WEB","RBT_WEB.jsp",e);
				strBuffer = new StringBuffer();
				strBuffer.append("Error");
			}
		}
	}
	if(strRequestType.equalsIgnoreCase("cat"))
	{
		try
		{
			strBuffer = rbtWeb.getActiveCategories(circleId,'b');
		}
		catch(Exception e)
		{
			Tools.logException("RBT_WEB","RBT_WEB.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}

}
try
{
	out.write(strBuffer.toString());
//	out.close();
}
catch(Exception e)
{
	Tools.logException("RBT_WEB","RBT_WEB.jsp",e);
}

%>