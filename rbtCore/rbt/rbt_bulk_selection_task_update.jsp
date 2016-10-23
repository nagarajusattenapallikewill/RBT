<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub,java.util.*,java.io.*,com.jspsmart.upload.*,java.text.*"%><%@ include file = "validate.jsp" %><%! 
String strUser= null;
String file = null;
java.io.File statusFile=null;
String actInfo, strSubClass;
String strChargeClass = null;
%><%
Calendar cal = Calendar.getInstance();
SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMMddhhmmss");
String strFileName = "Bulk_Selection"+sdf.format(cal.getTime())+".txt";
response.setContentType("application/octet-stream");
response.setHeader("Content-Disposition","attachment; filename="+strFileName+";");
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
file = null;
String strIP  = request.getRemoteAddr();
strUser  = (String)(session.getAttribute("UserId"));
if(strUser == null)
	actInfo = strIP + ":Direct"; 
else
	actInfo = strIP + ":" + strUser;
if (validateUser(request, session,  "rbt_bulk_selection_tasks_manager.jsp", response)) { %><%! 
			String actBy;
			boolean bBlackoutSMS;
		%><%
		String pathDir = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);	
		try
			{
			long maxfilesize = 20000000;
			SmartUpload mySmartUpload=null;
			mySmartUpload=new SmartUpload();
			mySmartUpload.initialize(pageContext); 
			mySmartUpload.setTotalMaxFileSize(maxfilesize);
			mySmartUpload.upload();
		
			String processfileDetails = mySmartUpload.getRequest().getParameter("processedFile");
			if(processfileDetails!=null && !processfileDetails.equals(""))
			{
				String[] strtok = processfileDetails.split("::");
				String filename = strtok[0];
				String actBy = strtok[1];
				String sub_class = strtok[2];
				String sel_class = strtok[3];
				String file_id = strtok[4];
				System.out.println(rbtLogin.getBulkSelectionTaskStatus(filename).equals("B"));
				System.out.println("Bulk Selection Done for a File by User "+strUser);
				statusFile = rbtLogin.processSelectionsTask(filename, null, actBy, actInfo+":BU:"+file_id, sub_class,sel_class);
				String actInfoUploaded = rbtLogin.getActivationInfoTask(filename);
				if(rbtLogin.getBulkSelectionTaskStatus(filename).equals("P"))
				{
					String actInfoProcessed=null;
					if(actInfoUploaded.indexOf("\\") >0)
					{
						actInfoProcessed = actInfoUploaded;
					}
					else
					{
						actInfoProcessed = actInfoUploaded+"\\"+actInfo;							
					}
					if(actInfoProcessed!=null)
					{
					String result = rbtLogin.updateActivationInfoTask(filename,actInfoProcessed);
					System.out.println("Status of the update of the activation Info"+result);
					System.out.println("Status Of after Update"+statusFile.getAbsolutePath());
					}
				}
					session.setAttribute("File",statusFile.getAbsolutePath());%>
				<jsp:forward page ="rbt_download.jsp" />
			<%}
		}
		catch(Exception e)
		{
			System.out.println("Exception in bulk_selection_update "+e.getMessage());
			session.setAttribute("flag","Internal Error");
		}
	%>
					<jsp:forward page="rbt_bulk_selection_tasks_manager.jsp" />
<%}else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}
%>