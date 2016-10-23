<%@ page import = "com.onmobile.apps.ringbacktones.content.UserRights"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub" %>
<%@ page import = "java.util.*"%>

<%@ include file = "validate.jsp" %>
<%
String strUser  = (String)(session.getAttribute("UserId"));
if (validateUser(request, session,  "rbt_add_user_permissions.jsp", response)) {
	System.out.println("User Permissions Changed By" + strUser);
%>
<html>
<head>
<%@ include file = "javascripts/RBTValidate.js" %>
<script language="JavaScript">
function update()
{
	if(isEmpty(document.forms[0].accessPages.value))
	{
		 alert('Please select atleast one option');
		 document.forms[0].accessPages.focus();
	     return false;
	}
	
	return true;
}
</script>
<title>RBT Add Permissions</title>
</head>
<body>
<form action="rbt_add_user_permissions.jsp" onsubmit='return update()' method="post" name="frmRBT">
<p><b><center>Add User Permissions</center></b></p>

<table width="100%" align = "center">
<% String strStatus=null ;String strReason=null;
UserRights userRights=null;
%>
<% 

RBTSubUnsub	rbtLogin = RBTSubUnsub.init();
userRights = null;
String rights="",user;
String[] str = request.getParameterValues("accessPages");
HashMap hMap = (HashMap)session.getAttribute("UserRights");
%><tr> 
<td></td> 
<td>
<%if(str != null)
{
	rights = str[0];
	for(int i=1;i<str.length;i++)
	{
		if(str[i].equalsIgnoreCase("All"))
		{
			rights = "All";
			break;
		}
		rights = rights + "," + str[i];
	}

	user = request.getParameter("userType");
	if(rights.equalsIgnoreCase("All"))
	{
		int len = hMap.size();
		rights ="0";
		int k =1;
		while(k<len)
		{
			String val = (String) hMap.get(String.valueOf(k));
			if(val != null)
				rights = rights + "," + k;
			else
				len++;
			k++;
		}
	}


	userRights = rbtLogin.insertUserRights(user,rights);
	if(userRights == null)
		out.print("<font face=\"Arial\" size=\"3\" color = \"red\" align = \"center\"><b>ERROR : Request Could Not be Completed</b></font>");
	else 
		out.print("<font face=\"Arial\" size=\"3\" color = \"green\" align = \"center\"><b>Request Completed Successfully.</b></font>");
}%>
</td></tr>
<tr></tr><tr></tr><tr></tr><tr></tr>
<tr>

<td width="30%">User Type</td>
<td width="70%">	

<select name="userType" >
<option selected value = '1'>1</option>		
<option value = '2'>2</option>		
<option value = '3'>3</option>		
<option value = '4'>4</option>		
<option value = '5'>5</option>		
<option value = '6'>6</option>		

</select>
</td>
</tr>

<tr>

<td width="30%">Access Pages</td>
<td width="70%">	

<select name="accessPages" multiple size="10">
	
	<% if(hMap == null)
		{
			session.invalidate();%>
			<jsp:forward page="index.jsp" />
		<%}
		
		else
		{
			int size = hMap.size();
			int i =0;
			while(i<size)
			{
				String val = (String) hMap.get(String.valueOf(i));
				if(val != null)
				{
					StringTokenizer tkn = new StringTokenizer(val,",");%>

					<option value = '<%=String.valueOf(i)%>'><%=tkn.nextToken()%></option>					
				<%}
				else
					size++;
				i++;
			}
			%>		<option value = 'All'>All</option>					
		
		<%}%>


</select>
</td>
</tr>
<tr>
</tr>
<tr>
</tr>
</table>
<center><input type="image" border="0" name="RBT_SUB" src="images/update1.gif"></center>
</form>
</body>
</html>
<%}
else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}%>