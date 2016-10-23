<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTFeedHelper,com.onmobile.apps.ringbacktones.common.Tools,java.util.*,java.io.*"
%><%@ page language="java" import="com.jspsmart.upload.*"%><%
String strFeed = null;
String strStatus = null;
String strFile = null;
String strResponse = null;

try
{
	RBTFeedHelper rbtFeed = new RBTFeedHelper();

	String pathDir = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);
	long maxfilesize = 20000000;
	SmartUpload mySmartUpload=null;
	mySmartUpload=new SmartUpload();
	mySmartUpload.initialize(pageContext); 
	mySmartUpload.setTotalMaxFileSize(maxfilesize);
	mySmartUpload.upload();

	strFeed = mySmartUpload.getRequest().getParameter("FEED");
	strStatus = mySmartUpload.getRequest().getParameter("STATUS");

	if(mySmartUpload.getFiles().getCount() > 0){
		if(mySmartUpload.getFiles().getFile(0).getSize() > 0)
		{
			strFile = mySmartUpload.getFiles().getFile(0).getFileName();
			mySmartUpload.save(pathDir);
		}
	}

	System.out.println("RBT: Feed INCOMING REQUEST FROM FEED " + strFeed + " with STATUS " + strStatus + " and FILE " + strFile) ;

	if(rbtFeed == null || //safety check 
			strFeed == null || //mandatory parameter 
			(strStatus == null && strFile == null)) //either the STATUS or file should be sent in the params
	{
		Tools.logStatus("RBT_FEED","rbt_feed.jsp","strFeed is " + strFeed + ", rbtFeed is " + rbtFeed + ", strStatus is " + strStatus + " and strFile is " +strFile);
		strResponse = "FAILURE";
	}
	else
	{
		if(strFeed.equalsIgnoreCase("CRICKET"))
		{
			if(strStatus != null && strFile != null)
			{
				//safety check
				strResponse = "FAILURE";
			}
			else if(strStatus != null)
			{
				boolean result = false;
				if(strStatus.equalsIgnoreCase("ON"))
				{
					result = rbtFeed.addFeed("CRICKET", "ON");
					Tools.logStatus("RBT_FEED","rbt_feed.jsp","for feed-ON result=="+result);
				}
				else if(strStatus.equalsIgnoreCase("OFF"))
				{
					result = rbtFeed.updateFeed("CRICKET", "OFF");
					Tools.logStatus("RBT_FEED","rbt_feed.jsp","for feed-OFF result=="+result);
				}
				if(result)
				{
					strResponse = "SUCCESS";
				}
				else
				{
					strResponse = "FAILURE";
				}
			}
			else 
			{
				//wav file is uploaded
				if(strFile.toLowerCase().endsWith(".wav")){
					strResponse = rbtFeed.updateFile("CRICKET", strFile);
					Tools.logStatus("RBT_FEED","rbt_feed.jsp","for file upload result=="+strResponse);
				}
				else
					strResponse = "FAILURE";
			}
	
		}
		else
		{
			strResponse = "FAILURE";
		}
	}
}
catch(Exception e)
{
	System.out.println("Exception in rbt_feed "+e.getMessage());
	Tools.logException("RBT_Feed", "rbt_feed.jsp", e);
	strResponse = "FAILURE";
}

try
{
	Tools.logStatus("RBT_FEED","rbt_feed.jsp","returning response=="+strResponse);
	out.print(strResponse);
}
catch(Exception e)
{
	Tools.logException("RBT_FEED", "rbt_feed.jsp", e);
}
%>