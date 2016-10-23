<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.RbtBulkSelectionTask,java.text.*,java.util.*,java.io.*"%>

<%@ include file = "validate.jsp" %>
<%
RBTSubUnsub rbtBulkStatus = RBTSubUnsub.init();
RbtBulkSelectionTask[] rbtBulkSelecTasks = rbtBulkStatus.getBulkSelectionTasks();
if (validateUser(request, session,  "rbt_bulk_selection_tasks_manager.jsp", response)) { %>
<html>
<head>
<%@ include file = "javascripts/RBTValidate.js" %>
<script language="JavaScript">
function deleteTask(deleteTask){
	document.frmBulkTask.selectedFile.value=deleteTask;
	document.frmBulkTask.action= "rbt_bulk_selection_task_delete.jsp?selectedFile="+document.frmBulkTask.selectedFile.value;
	alert('WARNING:Please do not refresh the page while your request is getting processed');
	document.frmBulk.submit();
}
function processTask(processtask){
	document.frmBulkTask.processedFile.value=processtask;
	alert('WARNING:Please do not refresh the page while your request is getting processed');
	return true;
}
</script>
<title>RBT Bulk Selection Tasks</title>
</head>
<body>
<form action ="rbt_bulk_selection_task_update.jsp" method="post" enctype="multipart/form-data" name="frmBulkTask">
	<p><b><center>Bulk Selection Tasks</center></b></p>
	<br>
	<br>
	<% String status =(String)session.getAttribute("Status"); 
		if(status!=null)
		if(status.indexOf("SUCCESS")>0)
		{
			session.removeAttribute("Status");
			out.print("<font face=\"Arial\" size=\"3\" color = \"green\" align = \"center\"><b>Request completed successfully.</b></font>");
		} else if(status.indexOf("FAILURE")>0) {
			out.print("<font face=\"Arial\" size=\"3\" color = \"green\" align = \"center\"><b>Request Not Processed</b></font>");
		}
	%>
	<% if(rbtBulkSelecTasks!=null) {%>
	<table align=center width="100%" border="1" cellpadding="5" cellspacing="1">
	<tr>
		<th>
			File Name
		</th>
		<th>
			Activated By
		</th>
		<th>
			Activated Class
		</th>
		<th>
			Selection Class
		</th>
		<th>
			Uploaded Date
		</th>
		<th>
			Processed Date
		</th>
		<th>
			Activation Info
		</th>
		<th>
			File Status
		</th>
		<th>
			Actions
		</th>
		</tr>
			<%for(int i=0;i<rbtBulkSelecTasks.length;i++) { %>
			<tr>
			<td>
				<%=rbtBulkSelecTasks[i].fileName().substring(14,rbtBulkSelecTasks[i].fileName().length())%>
			</td>
			<td>
				<%=rbtBulkSelecTasks[i].act_By()%>
			</td>
			<td>
				<%=rbtBulkSelecTasks[i].activation_Class()%>
			</td>
			<td>
				<%=rbtBulkSelecTasks[i].selection_Class()%>
			</td>
			<td>
				<%=rbtBulkSelecTasks[i].uploaded_Date()%>
			</td>
			<td>
				<% if(rbtBulkSelecTasks[i].processed_Date()!=null) { %>
				<%=rbtBulkSelecTasks[i].processed_Date()%>
				<% } else {%>
				<%=new String("-")%>
				<% } %>
			</td>
			<td>
			<% if(rbtBulkSelecTasks[i].activation_Info()!=null) { %>
				<%=rbtBulkSelecTasks[i].activation_Info()%>
				<% } else {%>
				<%=new String("-")%>
				<% } %>
			</td>
				<% StringBuffer procFileDetails=new StringBuffer("");
					procFileDetails.append(rbtBulkSelecTasks[i].fileName()+"::");
					procFileDetails.append(rbtBulkSelecTasks[i].act_By()+"::");
					procFileDetails.append(rbtBulkSelecTasks[i].activation_Class()+"::");
					procFileDetails.append(rbtBulkSelecTasks[i].selection_Class()+"::");
					procFileDetails.append(rbtBulkSelecTasks[i].fileID());
					System.out.println("Proc File Details"+procFileDetails);
				%>		
			<% String userType = (String) (session.getAttribute("Permission"));
				String userTypeRights = rbtBulkStatus.getUserRights(userType);
				System.out.println(userType+"::"+userTypeRights);
				if(userTypeRights.indexOf(",14,") > 0){ %>
				<% if(rbtBulkSelecTasks[i].file_Status().equals("B")) { %>
				<td>
					Uploaded
				</td>

				<td>
					<input type="submit" id="<%=procFileDetails.toString()%>" name="<%=procFileDetails.toString()%>" value="Process File" onclick='return processTask("<%=procFileDetails.toString()%>")'> 	
				</td>
				</tr>
				<% } %>
				<% if(rbtBulkSelecTasks[i].file_Status().equals("X")) { %>
				<td>
					Deleted
				</td>
				<td>
					<input type="submit" id="<%=procFileDetails.toString()%>" name="<%=procFileDetails.toString()%>" value="Deleted File" onclick='return processTask("<%=procFileDetails.toString()%>")' disabled> 	
				</td>
				</tr>
				<% } %>
				<% if(rbtBulkSelecTasks[i].file_Status().equals("P")) { %>
				<td>
					Processed
				</td>
				<td>
					<input type="submit" id="<%=procFileDetails.toString()%>" name="<%=procFileDetails.toString()%>" value="Processed File" onclick='return processTask("<%=procFileDetails.toString()%>")' disabled> 	
				</td>
				</tr>
				<% } %>

				<% } else{%>
			<% if(rbtBulkSelecTasks[i].file_Status().equals("B")) { %>
			<td>
				Uploaded
			</td>
			<td>
			<input type="submit" id="<%=rbtBulkSelecTasks[i].fileID()%>" name="<%=rbtBulkSelecTasks[i].fileID()%>" value="Delete File" onclick='deleteTask("<%=rbtBulkSelecTasks[i].fileID()%>")'> 	
			</td>
			</tr>
			<% } %>
			<% if(rbtBulkSelecTasks[i].file_Status().equals("X")) { %>
			<td>
				Deleted
			</td>
			<td>
			<input type="submit" id="<%=rbtBulkSelecTasks[i].fileID()%>" name="<%=rbtBulkSelecTasks[i].fileID()%>" value="Deleted File" onclick='deleteTask("<%=rbtBulkSelecTasks[i].fileID()%>")' disabled> 	
			</td>
			</tr>
			<% } %>
			<% if(rbtBulkSelecTasks[i].file_Status().equals("P")) { %>
			<td>
				Processed
			</td>
			<td>
			<input type="submit" id="<%=rbtBulkSelecTasks[i].fileID()%>" name="<%=rbtBulkSelecTasks[i].fileID()%>" value="Processed File" onclick='deleteTask("<%=rbtBulkSelecTasks[i].fileID()%>")' disabled> 	
			</td>
			</tr>
			<% } %>
			<% } %>
		<% } %>
	</table>
	<input type="hidden" name="selectedFile" value="">
		<input type="hidden" name="processedFile" value="">
			<% } else {
					 out.print("<font face=\"Arial\" size=\"3\" color = \"green\" align = \"center\"><b>No Records Found</b></font>");
			} %>
</form>
</body>
</html>
<% } else { 
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
	<% } %>