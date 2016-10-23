<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.RbtBulkSelectionTask,com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub,java.util.*,java.io.*,com.jspsmart.upload.*,java.text.*"%><%@ include file = "validate.jsp" %><%!
String strUser=null;
String file=null;
//java.io.File statusFile;
String actInfo=null;String strSubClass=null; String statusInsert=null;
String strChargeClass = null;
%><%
//Calendar cal = Calendar.getInstance();
//SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMMddhhmmss");
//String strFileName = "Bulk_Selection"+sdf.format(cal.getTime())+".txt";
//response.setContentType("application/octet-stream");
//response.setHeader("Content-Disposition","attachment; filename="+strFileName+";");
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
file = null;
String strIP  = request.getRemoteAddr();
strUser  = (String)(session.getAttribute("UserId"));
if(strUser == null)
	actInfo = strIP + ":Direct"; 
else
	actInfo = strIP + ":" + strUser;
if (validateUser(request, session,  "rbt_bulk_selection_task_manager.jsp", response)) { %><% 
			String actBy=null;
			boolean bBlackoutSMS=false;
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
			
			strSubClass = "DEFAULT";

			if(mySmartUpload.getRequest().getParameter("SUBCLASS") != null )
			{
				strSubClass = mySmartUpload.getRequest().getParameter("SUBCLASS");
			}
			
			
			strChargeClass = null;

			if(mySmartUpload.getRequest().getParameter("CHARGECLASS") != null )
			{
				strChargeClass = mySmartUpload.getRequest().getParameter("CHARGECLASS");
			}
			

			actBy = mySmartUpload.getRequest().getParameter("comboActBy");
			bBlackoutSMS = false;
			if(mySmartUpload.getRequest().getParameter("blackout_sms") != null)
				bBlackoutSMS = true;
			if(bBlackoutSMS)
				actInfo = "BULK:" + actInfo;
			
			if(mySmartUpload.getFiles().getCount() > 0){
				if(mySmartUpload.getFiles().getFile(0).getSize() > 0)
				{

					String filename = mySmartUpload.getFiles().getFile(0).getFileName();
					filename =System.currentTimeMillis()+"-"+filename; 
					statusInsert = rbtLogin.insertBulkSelectionTask(filename,actBy,strSubClass,strChargeClass,actInfo);	
					mySmartUpload.getFiles().getFile(0).saveAs(pathDir+java.io.File.separator + filename);
					String statusUpdate;
					statusUpdate = rbtLogin.updateBulkSelectionTaskStatus(filename,"B");
					//file = "Bulk-Selection-" + System.currentTimeMillis() + ".txt";
					//mySmartUpload.getFiles().getFile(0).saveAs(pathDir + java.io.File.separator + file);
				}
			}
			

				if(mySmartUpload.getFiles().getCount() <= 0 || mySmartUpload.getFiles().getFile(0).getSize() <= 0) {
				session.setAttribute("flag","Subscriber file size is zero");%>
					<jsp:forward page="rbt_bulk_selection_task_manager.jsp" />
				<%}	else{
					System.out.println("Bulk Selection Done for a File by User "+strUser);
					//added by eswar
					/*String filename = mySmartUpload.getFiles().getFile(0).getFileName();
					System.out.println(filename);*/
					/*boolean fileExists = fileAlreadyExists(filename);
					if(fileExists == false)
					{
						statusInsert = rbtLogin.insertBulkSelectionTask(filename,actBy,strSubClass,strChargeClass);	
						System.out.println("Status::"+statusInsert);
					}else {
						statusInsert=new String("RESULT:ALREADYEXISTS");
					}*/
				}
			if(statusInsert!=null)
			{
				session.setAttribute("StatusInsert",statusInsert);
			}
	}
	catch(Exception e)
	{
		System.out.println("Exception in bulk_selection_update "+e.getMessage());
		session.setAttribute("flag","Internal Error");
	}
	finally
	{
		//removed By eswar
		/*try
		{
			java.io.File temp = new java.io.File(pathDir + java.io.File.separator + file);
			if(temp.exists())
			{
				temp.delete();
			}
		}
		catch(Exception e)
		{
			System.out.println("Could not delete the file " + file + " from " + pathDir);
		}*/
	}
			%>
					<jsp:forward page="rbt_bulk_selection_task_manager.jsp" />
<%}else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}
%>
