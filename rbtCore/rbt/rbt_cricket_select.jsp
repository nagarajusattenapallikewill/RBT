<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ page import = "java.util.StringTokenizer"%>
<%@ include file = "validate.jsp" %>
<%
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
%>
<%
if (validateUser(request, session,  "rbt_cricket.jsp", response)) { %>
	<html>
		<head>
		<%@ include file = "javascripts/RBTValidate.js" %>
		<script language="JavaScript">
			function update()
			{   
				if(document.forms[0].UPDATE.value != "Submit")
				{
					alert("Processing in progress. Please do not refresh");
					return false;
				}
				if(document.forms[0].SUB_ID.disabled == false){
					if (isEmpty(frmCricket.SUB_ID.value))
					{
						alert("Enter a Subscriber Number");
						frmCricket.SUB_ID.focus();
						return false;
					}
					else {
						if(!isFieldAPhoneNumber(frmCricket.SUB_ID.value)){
							alert("Enter valid phone no.");
							frmCricket.SUB_ID.focus();
							return false;
						}
					}
				}
				document.frmCricket.UPDATE.value='Processing in progress...DO NOT REFRESH';
				return true;
			}
		</script>
		<title>RBT Cricket Selection</title>
		</head>
		
		<body>
		<form action="rbt_cricket_update.jsp" method="post" enctype="form-data" name="frmCricket">
			<p><b><center>Add Cricket Selections</center></b></p>
			<table align=center width="100%">
				<tr>
					<td></td>
					<td>
						<% 
						String flag =(String)(session.getAttribute("cricFlag"));
						String color = "red";
						if(flag != null && flag.equalsIgnoreCase("success"))
							color = "green";
						if(flag !=null) {
							out.println("<center><font face=\"Arial\" size=\"3\"  color=\"" + color + "\" align=\"center\"\t \t> <b>" + flag + "</b></font></center>");
						}
						session.removeAttribute("cricFlag");
						%>
					</td>
				</tr>
				<tr></tr>
				<tr></tr>
				<tr></tr>
				<tr>
					<td><input type="radio" name="user" checked value ="subscriber" onClick = "document.frmCricket.SUB_ID.value='';document.frmCricket.SUB_ID.disabled=false;">Subscriber</td>
					<td width="60%"><input type="text" name="SUB_ID" size=32 maxlength=32></td>
				</tr>
				<tr></tr>
				<tr>
					<td width = "30%">Activated By</td>
					<td width ="60%">
						<select name="comboActBy">
							<%
							String act_by = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "ACTIVATED_BY", null);
							if(act_by != null){
								StringTokenizer stk = new StringTokenizer(act_by,",");
								while(stk.hasMoreTokens()){
								String tmp = stk.nextToken();%>
								<option name="<%=tmp%>" value ="<%=tmp%>"><%=tmp%></option>
								<%}
							}else {%> 
								<option name="NA" value ="NA" disabled> NA </option>
							<%}%>
						</select>
					</td>
				</tr>
				<tr></tr>
				<tr>
					<td width="30%">Cricket Pack</td>
					<td width="60%">
						<select name="comboPack">
							<%
							String[] packs = rbtLogin.getAvailablePacks();
							for(int i = 0; packs != null && i < packs.length; i++) {
							String temp = packs[i];
							String packKeyword = temp.substring(0, temp.indexOf(","));
							%>
							<option name="<%=packKeyword%>" value="<%=packKeyword%>"><%=temp.substring(temp.indexOf(",") + 1)%></option>
							<% } %>
						</select>
					</td>
				</tr>
				<tr></tr>
				<tr>
					<td width="30%">Cricket Status</td>
					<td width="60%">
						<select name="comboCricStatus">
							<option name="ON" value="ON" selected>Activate</option>
							<option name="OFF" value="OFF">Deactivate</option>
						</select>
					</td>
				</tr>
				<tr></tr>
				<tr></tr>
				<tr></tr>
				<tr>
					<td></td>
					<td><input type=submit border = "0" value="Submit" name="UPDATE" onClick = 'return update()'></td>
				</tr>
		</form>
		</body>
<% }
else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}%>