<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTUGCHelper,com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub,com.onmobile.apps.ringbacktones.common.Tools,java.util.*,java.io.*"%>
<%@ page language="java" import="com.jspsmart.upload.*" %>
<form name="feed"  ENCTYPE="multipart/form-data">
<%
String strSubID = null;
String strResponse = "FAILURE";
String strTransID = null;
String strFile = null;
String sessionId = null;
try
{

String pathDir = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);
SmartUpload mySmartUpload=null;
long maxfilesize = 20000000;
mySmartUpload=new SmartUpload();
mySmartUpload.initialize(pageContext); 
mySmartUpload.setTotalMaxFileSize(maxfilesize);
mySmartUpload.upload();

RBTUGCHelper rbtUGC = RBTUGCHelper.init();
RBTSubUnsub rbtLogin = RBTSubUnsub.init();

strTransID =  mySmartUpload.getRequest().getParameter("TRANS_ID");
strSubID = mySmartUpload.getRequest().getParameter("USER_INFO");
sessionId = session.getId();

if(mySmartUpload.getFiles().getCount() > 0){
	if(mySmartUpload.getFiles().getFile(0).getSize() > 0)
	{
		strFile = mySmartUpload.getFiles().getFile(0).getFileName();
		if(strSubID == null || strSubID.equalsIgnoreCase("null"))
			strSubID = strFile.trim().substring(0,10);
		mySmartUpload.save(pathDir);
	}
}

System.out.println("RBT: UGC INCOMING REQUEST FOR SUB_ID" + strSubID + " and FILE " + strFile + " and TRANS_ID "+strTransID + " and SESSION-ID "+sessionId) ;

if(rbtUGC == null || rbtLogin == null )
{
	Tools.logStatus("RBT_UGC","rbt_ugc_add.jsp","rbtUGC is "+rbtUGC + " and rbtLogin is "+rbtLogin);
	strResponse = "FAILURE";
}
else
{
			if(rbtLogin.isValidSub(strSubID).equalsIgnoreCase("failure"))
				strResponse = "INVALID SUBSCRIBER";
			else if(strFile.endsWith(".wav"))
			{
				boolean isTransExist = false;
				if (strTransID != null)
				{
					isTransExist = rbtLogin.checkTransIDExist(strTransID,
														  "UGC_ADD");
					if (!isTransExist)
					{
						rbtLogin.addTransData(strTransID, strSubID,
										  "UGC_ADD");
					}
				}
				if (isTransExist)
					strResponse = "Invalid Request. Already recieved a request with the same TransID : "
						+ strTransID
						+ " and subscriber ID : "
						+ strSubID + " and wavFile "+strFile;
				else
					strResponse = rbtUGC.addUGC(strSubID, strFile);
				
			}
			else
				strResponse = "FAILURE";
		
}
}
catch(Exception e)
{
	System.out.println("Exception in rbt_ugc_add "+e.getMessage());
	Tools.logException("RBT_UGC", "rbt_ugc_add.jsp", e);
	strResponse = "FAILURE";
}

try
{
	out.flush();
	out.write(strResponse);
//	out.close();
}
catch(Exception e)
{
	Tools.logException("RBT_UGC", "rbt_ugc_add.jsp", e);
}
%>
</form>