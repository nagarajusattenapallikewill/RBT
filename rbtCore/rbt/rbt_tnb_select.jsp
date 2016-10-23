<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import="com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub,com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper,com.onmobile.apps.ringbacktones.content.Categories,java.util.*"%>
<%@ include file="validate.jsp" %>
<%
boolean browse = false;
if(validateUser(request, session, "rbt_tnb.jsp", response)) {
	RBTMOHelper rbtMO = RBTMOHelper.init();
	RBTSubUnsub rbtLogin = RBTSubUnsub.init();
	String browseStr = (String)session.getAttribute("Browse");
	if(browseStr != null && browseStr.equalsIgnoreCase("true"))
		browse = true;
%>
<HTML>
	<HEAD>
		<%@ include file = "javascripts/RBTValidate.js" %>
		<script language="JavaScript">
			function update() {
				var inProgressStr = "Processing in progress. Please do not refresh";
				if(document.tnbForm.UPDATE.value != "Submit") {
					 alert(inProgressStr);
					 return false;
				}

				if(document.tnbForm.subId.disabled == false) {
					if (isEmpty(tnbForm.subId.value)) {
						alert("Enter a Subscriber Number");
						tnbForm.subId.focus();
						return false;
					}
					else if(!isFieldAPhoneNumber(tnbForm.subId.value)) {
						alert("Enter valid phone number");
						tnbForm.subId.focus();
						return false;
					}
				}
				else {
					if(isEmpty(tnbForm.subFile.value)) {
						 alert("Enter a Subscriber File");
						 tnbForm.subFile.focus();
						 return false;
					}else {
						if(tnbForm.subFile.value.indexOf(".txt") == -1) {
							alert("Enter only text file");
							tnbForm.subFile.focus();
							return false;
						}
					}
				}
				if(tnbForm.SongName.value == null || tnbForm.SongName.value == "null") {
					var confirmStat = confirm("No clip has been selected. Do you want to continue");
					if(!confirmStat) {
						tnbForm.SongName.focus();
						return confirmStat;
					}
				}
				document.tnbForm.UPDATE.value = inProgressStr;
				return true;
			}
		</script>
	</HEAD>
	<BODY>
		<p><b><CENTER>Try and Buy</CENTER></b></p>
		<%
		List tnbPackList = rbtLogin.getTNBPackList();
		if(tnbPackList == null || tnbPackList.size() == 0) {%>
			<br><br><br><br><br><br><br><br><br><br>
			<p><font face="Arial" size="3" color="red" align="center"><b>No Try And Buy Packs are available</b></font></p>
		<%}
		else {%>
			<FORM name="tnbForm" action="rbt_tnb_update.jsp" enctype="multipart/form-data" method="post">
				<TABLE align=center width="100%">
					<TR>
						<TD></TD>
						<TD>
						<%
						String tnbStatus = (String)session.getAttribute("tnbStatus");
						if(tnbStatus != null) {
							String color = "red";
							if((tnbStatus.indexOf("Successfully") != -1) || (tnbStatus.indexOf("accepted") != -1))
								color = "green";
						%>
							<font face="Arial" size="3" color="<%=color%>" align="center"><b><%=tnbStatus%></b></font>
						<%
						}
						session.removeAttribute("tnbStatus");
						%>
						</TD>
					</TR>
					<TR></TR>
					<TR></TR>
					<TR></TR>
					<TR>
						<TD>
						<%if(browse) {%>
							<input type="radio" name="user" checked value="subscriber" onclick="document.tnbForm.subId.value=''; document.tnbForm.subFile.value=''; document.tnbForm.subId.disabled=false; document.tnbForm.subFile.disabled=true;"/><%}%>Subscriber
						</TD>
						<TD width="60%"><input type="text" name="subId" size=20 maxlength=32/></TD>
					</TR>
					<TR></TR>
					<%if(browse) {%>
						<TR>
							<TD width="30%"><input type="radio" name="user" value="subscriber" onclick="document.tnbForm.subId.value=''; document.tnbForm.subFile.value=''; document.tnbForm.subId.disabled=true; document.tnbForm.subFile.disabled=false;"/>Subscriber File</TD>
							<TD width="60%"><input type="file" name="subFile" size=32 maxlength=32 disabled/></TD>
						</TR>
					<%}%>
					<TR></TR>
					<TR></TR>
					<TR>
						<TD width="30%">Subscriber Type</TD>
						<TD width="60%">
							<SELECT name="UserType">
								<OPTION name="comboPost" value="Postpaid">Postpaid</OPTION>
								<OPTION name="comboPre" value="Prepaid">Prepaid</OPTION>
							</SELECT>
						</TD>
					</TR>
					<TR></TR>
					<TR></TR>
					<TR>
						<TD width="30%">Subscription Pack</TD>

						<TD width="60%">
							<SELECT name="subClassStr">
							<%for(int i = 0; i < tnbPackList.size(); i++) {
								String temp = (String)tnbPackList.get(i);%>
								<OPTION name="<%=temp%>" value="<%=temp%>"><%=temp%></OPTION>
							<%}%>
							</SELECT>
						</TD>
					</TR>
					<TR></TR>
					<TR></TR>
					<TR>
					<TD width="30%">Select Song</TD>
						<TD width="60%">
							<%
							SortedMap clip  = rbtMO.getSMSPromoClips();
							if(clip != null && !clip.isEmpty())
							{
								try {
									Iterator i = clip.keySet().iterator();
									%>
									<select name="SongName">
									<option value='null'>No Selection
									<%
									while(i.hasNext()) {
										String tmp = (String) i.next();%>
										<option value='<%=clip.get(tmp)%>'><%=tmp%>
									<%}
								}
								catch(Throwable e) {%>
									<select name="SongName" disabled>
									<option value="" disabled>No SMS Promo Selections
								<%}
							}
							else {%>
								<select name="SongName" disabled>
								<option value="" disabled>No SMS Promo Selections
							<%}%>
						</TD>
					</TR>
					<TR></TR>
					<TR></TR>
					<tr> 
				   <td colspan =2><input type=checkbox name="IGNORE_ACTIVE" checked = true>Ignore Active Subscribers</td> 
				   </tr> 
				   <TR></TR> 
				   <TR></TR> 

					<%if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "BLACKOUT_SMS", "FALSE")) {%>
						<TR>
						<TD colspan=2>
							<input type=checkbox name="blackoutSms" checked />Do not send SMS during Blackout period
						</TD>
						</TR>
					<%}%>
					<TR></TR>
					<TR></TR>
					<TR>
						<TD></TD>
						<TD>
							<input type=submit border=0 name="UPDATE" value="Submit" onclick='return update()' />
						</TD>	
					</TR>
				</TABLE>
			</FORM>
		<%}%>
	</BODY>
</HTML>
<%
}
else {
	session.invalidate();%>
	<jsp:forward page="rbt_index.jsp"/>
<%}%>