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
String strAction = null;
String promoIdList = null;
String ADD_UGC = "ADD";
String EXPIRE_UGC = "EXPIRE";
String sessionId = null;
try
{

String pathDir = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);
SmartUpload mySmartUpload=null;
long maxfilesize = 20000000; 
try 
{ 
	mySmartUpload=new SmartUpload();
	mySmartUpload.initialize(pageContext); 
	mySmartUpload.setTotalMaxFileSize(maxfilesize);
	mySmartUpload.upload();
}
catch(Exception e)
{}

RBTUGCHelper rbtUGC = RBTUGCHelper.init();
RBTSubUnsub rbtLogin = RBTSubUnsub.init();

//rbtUGC = RBTUGCHelper.init();


strSubID = mySmartUpload.getRequest().getParameter("SUB_ID");
strAction = mySmartUpload.getRequest().getParameter("ACTION");
promoIdList = mySmartUpload.getRequest().getParameter("PROMO_ID");
strTransID =  mySmartUpload.getRequest().getParameter("TRANS_ID");
sessionId = session.getId();

if(mySmartUpload.getFiles().getCount() > 0){
	if(mySmartUpload.getFiles().getFile(0).getSize() > 0)
	{
		strFile = mySmartUpload.getFiles().getFile(0).getFileName();
		strSubID = strFile.trim().substring(0,10);
		mySmartUpload.save(pathDir);
	}
}

System.out.println("RBT: UGC INCOMING REQUEST FOR SUB_ID" + strSubID + " and ACTION " + strAction + " and PROMO_ID " + promoIdList + " and FILE " + strFile + " and TRANS_ID "+strTransID + " and SESSION-ID "+sessionId) ;

if(rbtUGC == null || rbtLogin == null )
{
	Tools.logStatus("RBT_UGC","rbt_ugc.jsp","rbtUGC is "+rbtUGC + " and rbtLogin is "+rbtLogin);
	strResponse = "FAILURE";
}
else if(strAction != null && strAction.toUpperCase().equalsIgnoreCase(EXPIRE_UGC))
	 {
	         boolean isTransExist = false;
	         if (strTransID != null && strSubID != null)
	         {
	                 isTransExist = rbtLogin.checkTransIDExist(strTransID,
	                                                                                           "UGC_EXPIRE");
	                 if (!isTransExist)
	                 {
	                         rbtLogin.addTransData(strTransID, strSubID,
	                                                           "UGC_EXPIRE");
	                 }
	         }
	         if (isTransExist)
	                 strResponse = "Invalid Request. Already recieved a request with the same TransID : "
	                         + strTransID
	                         + " and subscriber ID : "
	                         + strSubID + " and promoIDList "+promoIdList;
	         else
	         {
	                 boolean success = false;
	                 if(promoIdList != null)
	                 success = rbtUGC.expireUGCClipsForPromoIDs(promoIdList);
	                 else if (strSubID != null)
	                	 success = rbtUGC.expireUGCClipsOfCreator(strSubID);
	                 if(success)
	                         strResponse = "SUCCESS";
	                 else
	                         strResponse = "FAILURE";
	                 }
	 }
else if(strAction == null || strAction.toUpperCase().equalsIgnoreCase(ADD_UGC))
{
			if(strFile.endsWith(".wav"))
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
	System.out.println("Exception in rbt_ugc "+e.getMessage());
	Tools.logException("RBT_UGC", "rbt_ugc.jsp", e);
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
	Tools.logException("RBT_UGC", "rbt_ugc.jsp", e);
}
%>
</form>