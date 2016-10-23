<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ include file = "validate.jsp" %>
<% 
	RBTSubUnsub rbtLogin = RBTSubUnsub.init();
	String fileID = request.getParameter("selectedFile");
	System.out.println(fileID);
	if (validateUser(request, session,  "rbt_bulk_selection_tasks_manager.jsp", response)) {
		if(fileID!=null && !fileID.equals(""))
		{
			int fileid = Integer.parseInt(fileID);
			String filename = rbtLogin.getFileFromFileID(fileid);
				if(rbtLogin.getBulkSelectionTaskStatus(filename).equals("B"))
				{
					System.out.println("Fileid to be deleted:: "+fileid);
					String status = rbtLogin.updateBulkSelectionTaskStatus(filename,"X");
					session.setAttribute("Status",status);
				} 
		}%>
		<jsp:forward page="rbt_bulk_selection_tasks_manager.jsp" />

			<% } else { 
			session.invalidate();%>
			<jsp:forward page="index.jsp" />
	<% }%>