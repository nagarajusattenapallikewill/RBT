<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import="com.onmobile.apps.ringbacktones.common.Tools,com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub,java.util.*,java.io.*,com.jspsmart.upload.*" %>
<%@ include file = "validate.jsp" %>
<%
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
String strIP  = request.getRemoteAddr();
String strUser  = (String)(session.getAttribute("UserId"));
String actInfo = null;
String jspName = "rbt_tnb_update.jsp";
if(strUser == null)
	actInfo = strIP + ":Direct";
else
	actInfo = strIP + ":" + strUser;

if(validateUser(request, session, "rbt_tnb.jsp", response)) {
	String path = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);
	String file = null;

	try {
		long maxfilesize = 20000000;
		SmartUpload mySmartUpload = new SmartUpload();
		mySmartUpload.initialize(pageContext); 
		mySmartUpload.setTotalMaxFileSize(maxfilesize);
		mySmartUpload.upload();
		boolean bIgnoreAct = false;
		boolean bPrepaid = false;
		boolean blkSMS = true;
		String strSub = mySmartUpload.getRequest().getParameter("subId");
		String clipId = mySmartUpload.getRequest().getParameter("SongName");
		String subClass = mySmartUpload.getRequest().getParameter("subClassStr");

		String temp = mySmartUpload.getRequest().getParameter("UserType");
		if(temp != null)
			bPrepaid = temp.equals("Prepaid");
		if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "USE_DEFAULT_TYPE_GUI_PROCESSING", "TRUE"))
		{
			if(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID").equalsIgnoreCase("Prepaid"))
				bPrepaid = true;
			if(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID").equalsIgnoreCase("Postpaid"))
				bPrepaid=false;
		}
		
		temp = mySmartUpload.getRequest().getParameter("blackoutSms");
		if(temp != null)
			blkSMS = temp.equals("blackoutSms");
		if(mySmartUpload.getRequest().getParameter("IGNORE_ACTIVE") != null) 
			bIgnoreAct = true; 

		if(blkSMS)
			actInfo = "BULK:" + actInfo;

		if(mySmartUpload.getFiles().getCount() > 0 && mySmartUpload.getFiles().getFile(0).getSize() > 0) {
			file = "TNB-" + System.currentTimeMillis() + ".txt";
			mySmartUpload.getFiles().getFile(0).saveAs(path + java.io.File.separator + file);
		}

		if(strSub != null) {
			String tnbResponse = rbtLogin.processTNBRequest(strSub, bPrepaid, subClass, clipId, blkSMS, actInfo, bIgnoreAct);
			session.setAttribute("tnbStatus", tnbResponse);
		}
		else {
			if(file == null) {
				Tools.logDetail(jspName, jspName, "RBT::file uploaded is of size zero");
				session.setAttribute("tnbStatus", "File size is zero, pls upload proper file");%>
				<jsp:forward page="rbt_tnb.jsp" />
			<%}
			else {
				Tools.logDetail(jspName, jspName, "RBT::file uploaded by user " + strUser);
				try {
					BufferedReader br = new BufferedReader(new FileReader(path + java.io.File.separator + file));
					String line;
					while((line = br.readLine()) != null) {
						rbtLogin.processTNBRequest(line.trim(), bPrepaid, subClass, clipId, blkSMS, actInfo, bIgnoreAct);
					}
					session.setAttribute("tnbStatus", "Request Processed Successfully");
				}
				catch(IOException e) {
					Tools.logFatalError(jspName, jspName, "RBT::error while parsing downloaded file " + file);
					session.setAttribute("tnbStatus", "Internal Error");
				}
			}
		}//end of sub file processing
	}
	catch(Exception e) {
		Tools.logException(jspName, jspName, e);
		session.setAttribute("tnbStatus", "Internal Error");
	}
	finally {
		try {
			java.io.File fileIns = new java.io.File(path + java.io.File.separator + file);
			if(fileIns.exists())
				fileIns.delete();
		}
		catch(Exception e) {
			Tools.logWarning(jspName, jspName, "RBT::not able to delete the file " + file + " from path " + path);
		}
	}
%>
	<jsp:forward page="rbt_tnb.jsp"/>
<%
}
else {
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}%>