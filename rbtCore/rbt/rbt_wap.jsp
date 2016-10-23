<%@page session="false"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTWAPHelper,com.onmobile.apps.ringbacktones.common.Tools,java.util.*,java.io.*"%>

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
String strActivatedBy = null;
StringBuffer strBuffer = new StringBuffer();

boolean isprepaid = false;

RBTWAPHelper rbtWap = null;

try
{
	rbtWap = new RBTWAPHelper();
}
catch(Exception e)
{
	Tools.logException("RBT_WAP","RBT_WAP.jsp",e);
}
System.out.println("RBT: WAP INCOMING REQUEST = " + request.getParameter("REQUEST_TYPE") + " from SubscriberID = " + request.getParameter("SUB_ID"));

strSubID = request.getParameter("SUB_ID");
strPrepaid = request.getParameter("SUB_TYPE");
strRequestType = request.getParameter("REQUEST_TYPE");
strRequestValue = request.getParameter("REQUEST_VALUE");
strActivatedBy = request.getParameter("ACTIVATED_BY");
circleId= request.getParameter("CIRCLE_ID");

if(strRequestType == null || rbtWap == null)
{
	Tools.logStatus("RBT_WAP","RBT_WAP.jsp","strRequestType is " + strRequestType + " and rbtWap is " + rbtWap);
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
			strBuffer = rbtWap.getSubscriber(strSubID.trim());
		}
		catch(Exception e)
		{
			Tools.logException("RBT_WAP","RBT_WAP.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
		
	}
	if(strRequestType.equalsIgnoreCase("act"))
	{
		if(strRequestValue == null)
		{
			Tools.logStatus("RBT_WAP","RBT_WAP.jsp","strRequestValue is " + strRequestType);
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
				strBuffer = rbtWap.activateSubscriber(strSubID.trim(), pre, strActivatedBy);
			}
			catch(Exception e)
			{
				Tools.logException("RBT_WAP","RBT_WAP.jsp",e);
				strBuffer = new StringBuffer();
				strBuffer.append("Error");
			}
		}
	}
	if(strRequestType.equalsIgnoreCase("deact"))
	{
		try
		{
			
			strBuffer = rbtWap.deactivateSubscriber(strSubID.trim());
		}
		catch(Exception e)
		{
			Tools.logException("RBT_WAP","RBT_WAP.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}
	if(strRequestType.equalsIgnoreCase("search"))
	{
		try
		{
			strBuffer = rbtWap.searchSong(strRequestValue);
		}
		catch(Exception e)
		{
			Tools.logException("RBT_WAP","RBT_WAP.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}
	
	/*if(strRequestType.equalsIgnoreCase("sms"))
	{
		try
		{
			strBuffer = rbtWap.sendSMS(strSubID.trim(), strRequestValue);
		}
		catch(Exception e)
		{
			Tools.logException("RBT_WAP","RBT_WAP.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}*/

	if(strRequestType.equalsIgnoreCase("set"))
	{
		if(strRequestValue == null)
		{
			Tools.logStatus("RBT_WAP","RBT_WAP.jsp","strRequestValue is " + strRequestValue);
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
				frmTime = 0;
				toTime = 23;
			}

			try
			{
				strBuffer = rbtWap.addSelections(strSubID, callerID, Integer.parseInt(categoryID), rbt, frmTime, toTime);
			}
			catch(Exception e)
			{
				Tools.logException("RBT_WAP","RBT_WAP.jsp",e);
				strBuffer = new StringBuffer();
				strBuffer.append("Error");
			}
		}
	}
	if(strRequestType.equalsIgnoreCase("deact"))
	{
		try
		{
			strBuffer = rbtWap.deactivateSubscriber(strSubID.trim());
		}
		catch(Exception e)
		{
			Tools.logException("RBT_WAP","RBT_WAP.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}
	if(strRequestType.equalsIgnoreCase("del"))
	{
		if(strRequestValue == null)
		{
			Tools.logStatus("RBT_WAP","RBT_WAP.jsp","strRequestValue is " + strRequestType);
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
				strBuffer = rbtWap.removeSelection(strSubID, strRequestValue);
			}
			catch(Exception e)
			{
				Tools.logException("RBT_WAP","RBT_WAP.jsp",e);
				strBuffer = new StringBuffer();
				strBuffer.append("Error");
			}
		}
	}
	if(strRequestType.equalsIgnoreCase("cat"))
	{
		try
		{
			strBuffer = rbtWap.getActiveCategories(circleId,'b');
		}
		catch(Exception e)
		{
			Tools.logException("RBT_WAP","RBT_WAP.jsp",e);
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
	Tools.logException("RBT_WAP","RBT_WAP.jsp",e);
}

%>