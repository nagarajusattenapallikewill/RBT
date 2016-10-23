<%@page import="com.onmobile.apps.ringbacktones.genericcache.beans.PredefinedGroup"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub, java.net.*"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.Clips"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.database.ClipsImpl"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.GroupMembers"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.Groups"%>

<%@ include file = "validate.jsp" %>
<%
String browse  = (String)(session.getAttribute("Browse"));
RBTSubUnsub rbtLogin = RBTSubUnsub.init();

String subId=null;
String click=null;
String groupName=null;
String callerNumber=null;
String preGroupID=null;
String groupPromoID=null;
String callerName=null;
String result=null;
Groups[] groups=null;
GroupMembers [] groupMembers=null;
int groupID = -1;
int selectGroupID = -1;
String callerID=null;
String moveRemoveMenu = null;
int moveTo=-1;
int fromGroupID=-1;
String addGroup=null;
int deleteGroupID = -1;

subId=request.getParameter("SUB_ID");
groupName=request.getParameter("GROUP_NAME");
if(request.getParameter("GroupMenu") != null)
{
	groupID=Integer.parseInt(request.getParameter("GroupMenu"));
}
preGroupID=request.getParameter("AddpreGroup");
addGroup = request.getParameter("addGroup");
callerNumber=request.getParameter("CALLER_NUMBER");
callerName=request.getParameter("CALLER_NAME");
click = request.getParameter("click");	
callerID = request.getParameter("CallerDetails");	
moveRemoveMenu = request.getParameter("moveOrRemove");
if(request.getParameter("deleteMenu") != null)
{
	deleteGroupID = Integer.parseInt(request.getParameter("deleteMenu"));
}
if(request.getParameter("FROM_GROUP_ID") != null)
{
	fromGroupID = Integer.parseInt(request.getParameter("FROM_GROUP_ID"));
}
if(request.getParameter("MovetoGroupMenu") != null)
{
	moveTo = Integer.parseInt(request.getParameter("MovetoGroupMenu"));
}
System.out.println("The value of click "+click);
System.out.println("The value of subscriberId "+subId);
System.out.println("The value of groupName "+groupName);
System.out.println("The value of groupID "+groupID);
System.out.println("The value of selectGroupID "+selectGroupID);
	
if (validateUser(request, session, "rbt_subscriber_status.jsp", response)) {%>
<html>
<head>
<%@ include file = "javascripts/RBTValidate.js" %>
<script language="JavaScript">
function update1()
{
		if (isEmpty(SubscriberIdForm.SUB_ID.value))
		{
			alert("Enter a Subscriber Number");
		    SubscriberIdForm.SUB_ID.focus();
		    return false;
		}
		else 
		{
			if(!isFieldAPhoneNumber(SubscriberIdForm.SUB_ID.value))
			{
			alert("Enter valid subscriber phone no.");
			SubscriberIdForm.SUB_ID.focus();
			return false;
			}else return true;
		}
		
}

function update2()
{
	if(addGroupForm.addGroup.value == "userGroup")
	{
		if (isEmpty(addGroupForm.GROUP_NAME.value))
		{
			alert("Enter a Group Name");
		    addGroupForm.GROUP_NAME.focus();
		    return false;
		}
	}
}
	
function update3()
{
		if(isEmpty(addContactForm.GroupMenu.value))
		{
			alert("There is no active group for subscriber to add contact");
			addContactForm.GroupMenu.focus();
			return false;
		}
		if (isEmpty(addContactForm.CALLER_NUMBER.value))
		{
			alert("Enter a Caller Number");
		    addContactForm.CALLER_NUMBER.focus();
		    return false;
		}
		else 
		{
			if(!isFieldAPhoneNumber(addContactForm.CALLER_NUMBER.value))
			{
				alert("Enter valid Caller phone no.");
				addContactForm.CALLER_NUMBER.focus();
				return false;
			}
		}
}

function update4()
{
	if(isEmpty(moveOrRemoveForm.FROM_GROUP_ID.value))
	{
		alert("There is no active group for subscriber");
		moveOrRemoveForm.SelectGroupMenu.focus();
		return false;
	}
	if(isEmpty(moveOrRemoveForm.MovetoGroupMenu.value))
	{
		alert("There is no active group for subscriber");
		moveOrRemoveForm.MovetoGroupMenu.focus();
		return false;
	}
	if(moveOrRemoveForm.MovetoGroupMenu.disabled == false){
		if(moveOrRemoveForm.FROM_GROUP_ID.value == moveOrRemoveForm.MovetoGroupMenu.value)
		{
			alert("The selected group and the move to group are same");
			moveOrRemoveForm.MovetoGroupMenu.focus();
			return false;
		}
	}

}

function update5()
{
	if(isEmpty(deleteGroupForm.deleteMenu.value))
	{
		alert("There is no active group for subscriber to deactivate");
		deleteGroupForm.deleteMenu.focus();
		return false;
	}
	

}	

</script>
<title>RBT Subscription</title>
</head>
<body onLoad = 'return fnSetFocus(frmStatus,0)'>
<%

	groups = rbtLogin.getActiveGroupsForSubscriberID(subId);
	if(request.getParameter("SelectGroupMenu") != null)
	{
		selectGroupID=Integer.parseInt(request.getParameter("SelectGroupMenu"));
	}
	else if(groups != null)
	{
		selectGroupID=groups[0].groupID();
	}
	else
	{
		selectGroupID=fromGroupID;
	}
	if(click != null){
	if(click.equals("addgroup"))
	{
		if(addGroup != null && addGroup.equals("userGroup"))
		{
			result = rbtLogin.addGroupForSubscriberID(null, groupName, subId, groupPromoID);
		}
		else
		{
			result = rbtLogin.addGroupForSubscriberID(preGroupID, groupName, subId, groupPromoID);
		}
		if(result != null)
		{
			if(result.equals("SUCCESS"))
			{%>
				<script language="javascript">
					alert("New Group has been added succesfully ");
				</script>
			<%}
			else if(result.equals("MAX_GROUP_PRESENT"))
			{%>
				<script language="javascript">
					alert("You have maximum no of groups allowed");
				</script>
			<%}
			else if(result.equals("USER_NOT_ACTIVE"))
			{%>
				<script language="javascript">
					alert("The Subscriber does not exist");
				</script>
			<%}else{%>
				<script language="javascript">
					alert("Request for adding a new group has failed");
				</script>
			<%}
		}
		else{%>
			<script language="javascript">
					alert("Request for adding a new group has failed");
				</script>
		<%}
	}
	else if(click.equals("addcaller"))
	{
		result = rbtLogin.addCallerInGroup(subId, groupID, callerNumber, callerName);
		if(result != null)
		{
			if(result.equals("SUCCESS"))
			{%>
				<script language="javascript">
					alert("New Caller has been added to group succesfully ");
				</script>
			<%}
			else if(result.startsWith("FAILURE_PERSONALIZED"))
			{%>
				<script language="javascript">
					alert("Caller cannot be added as a personalized selection exists");
				</script>
			<%}
			else if(result.startsWith("FAILURE_ALREADY"))
			{%>
				<script language="javascript">
					alert("Caller cannot be added as caller is present in other group");
				</script>
			<%}
			else
			{%>
				<script language="javascript">
					alert("Caller cannot be added due to some internal error");
				</script>
			<%}
		}else{%>
				<script language="javascript">
					alert("Caller cannot be added due to some internal error");
				</script>
		<%}
	}
	else if(click.equals("moveOrRemoveDetails"))
	{
		if(moveRemoveMenu.equalsIgnoreCase("Remove"))
		{
			result = rbtLogin.removeCallerFromGroup(subId, fromGroupID, callerID);	
			if(result.equals("SUCCESS"))
			{%>
				<script language="javascript">
				alert("Caller has been removed succesfully");
			</script>
			<%}
			else
			{%>
				<script language="javascript">
				alert("Caller cannot be removed due to some internal error");
			</script>
			<%}
		}
		else if(moveRemoveMenu.equalsIgnoreCase("Move"))
		{
			result = rbtLogin.changeGroupForCaller(subId, callerID, fromGroupID, moveTo);
			if(result.equals("SUCCESS"))
			{%>
				<script language="javascript">
				alert("Caller has been moved to other group succesfully");
			</script>
			<%}
			else
			{%>
				<script language="javascript">
				alert("Caller cannot be moved to other group due to some internal error");
			</script>
			<%}
		}
	
	}
	else if(click.equals("delGroup"))
	{
		result = rbtLogin.deleteGroup(subId, deleteGroupID, "CC");
		if(result.equals("SUCCESS"))
		{%>
			<script language="javascript">
				alert("Group has been succesfully removed");
			</script>
		<%}
		else
		{%>
			<script language="javascript">
				alert("Group cannot be removed due to internal error");
			</script>
		<%}
	}

}
groups = rbtLogin.getActiveGroupsForSubscriberID(subId);
PredefinedGroup[] predefinedGroups = rbtLogin.getPredefinedGroupsNotAddedForSubscriber(subId);
if(selectGroupID != -1)
	{
		groupMembers = rbtLogin.getActiveMembersForGroupID(selectGroupID);
	}
%>
<%if(subId == null){%>
<form name="SubscriberIdForm" onsubmit='return update1()' method="post" action="rbt_group_main.jsp">
<table>
<tr>
<td width="30%">Subscriber Id</td>
<td width="60%"><input type=text name="SUB_ID" size = 32 maxlength=32></td>
<td width="60%"><input type="submit" value="Submit" name="SubscriberIdbutton" onClick = "return update1()"></td>
</tr>
</table>
<%}else{%>
<form name="addGroupForm" onsubmit='return update2()' method="post" action="rbt_group_main.jsp">
<p><b><center>RBT Groups</center></b></p>
<b>Add New Group</b>
<br/>
<table width ="100%">
<tr></tr>
<tr>
<%if(rbtLogin.m_allowAddUserGroup){%>
<td width="30%"><input type="radio" name="addGroup" checked value="preGroup" onClick = "document.addGroupForm.GROUP_NAME.disabled=true;document.addGroupForm.AddpreGroup.disabled=false;">Add Predefined Groups</td>
<%}else{%>
<td width="30%">Add Predefined Group</td>
<%}%>
<td width="10%">
<select name="AddpreGroup">
<%if(predefinedGroups != null){
for(int i=0;i<predefinedGroups.length;i++){
%>
<option value=<%=predefinedGroups[i].getPreGroupID()%>><%=predefinedGroups[i].getPreGroupName()%></option>
<%}}%>
</select>
</td>
</tr>
<tr></tr>
<%if(rbtLogin.m_allowAddUserGroup){%>
<tr>
<td width="30%"><input type="radio" name="addGroup" value="userGroup" onClick = "document.addGroupForm.AddpreGroup.disabled=true;document.addGroupForm.GROUP_NAME.disabled=false;">Add User Group</td>
<td width="10%">Group Name</td>
<td width="20%"><input type=text name="GROUP_NAME" size = 32 maxlength=32 disabled></td>
</tr>
<%}%>
<tr>
<td width="60%"><input type=hidden name="click" value="addgroup" size = 32 maxlength=32></td>
<td width="60%"><input type=hidden name="SUB_ID" value=<%=subId%> size = 32 maxlength=32></td>
<td width="60%"><input type=hidden name="FROM_GROUP_ID" value=<%=selectGroupID%> size = 32 maxlength=32></td>
<td width="60%"><input type="submit" value="ADD" name="ADDGROUP" onClick = "return update2()"></td>
</tr>
</table>
</form>
<form name="deleteGroupForm" onsubmit='return update5()' method="post" action="rbt_group_main.jsp">
<b>Delete Group</b>
<br/>
<table width ="50%">
<tr></tr>
<tr>
<td width="20%">Group</td>
<td width="20%">
<select name="deleteMenu">
<%if(groups != null){
for(int i=0;i<groups.length;i++){
%>
<option value=<%=groups[i].groupID()%>><%=groups[i].groupName()%></option>
<%}}%>
</select>
</td>
<td width="60%"><input type=hidden name="click" value="delGroup" size = 32 maxlength=32></td>
<td width="60%"><input type=hidden name="SUB_ID" value=<%=subId%> size = 32 maxlength=32></td>
<td width="60%"><input type=hidden name="FROM_GROUP_ID" value=<%=selectGroupID%> size = 32 maxlength=32></td>
<td width="10%"><input type="submit" value="Delete" name="deleteGroup" onClick = "return update5()"></td>
</tr>
</table>
</form>
<form name="addContactForm" onsubmit='return update3()' method="post" action="rbt_group_main.jsp">
<br/><br/>
<b>Add New Contact</b>
<br/>
<table width ="100%">
<tr></tr>
<tr>
<td width="20%">Group</td>
<td width="20%">
<select name="GroupMenu">
<%if(groups != null){
for(int i=0;i<groups.length;i++){
%>
<option value=<%=groups[i].groupID()%>><%=groups[i].groupName()%></option>
<%}}%>
</select>
</td>
<td></td>
<td width="20%">Name</td>
<td width="40%"><input type=text name="CALLER_NAME" size = 32 maxlength=32></td>
<td></td>
<td width="20%">Phone No</td>
<td width="40%"><input type=text name="CALLER_NUMBER" size = 32 maxlength=32></td>
<td></td>
<td width="60%"><input type=hidden name="click" value="addcaller" size = 32 maxlength=32></td>
<td width="60%"><input type=hidden name="SUB_ID" value=<%=subId%> size = 32 maxlength=32></td>
<td width="60%"><input type=hidden name="FROM_GROUP_ID" value=<%=selectGroupID%> size = 32 maxlength=32></td>
<td width="40%"><input type="submit" value="ADD" name="ADDCALLER" onClick = "return update3()"></td>
</tr>
</table>
</form>
<br/><br/>
<form name="viewDetailsForm" method="post" action="rbt_group_main.jsp">
<b>View Group Details</b>
<br/>
<table width ="50%">
<tr>
<td width="30%">Selected Group</td>
<td width="20%">
<select name="SelectGroupMenu" onchange='this.form.submit()'>
<%if(groups != null){
for(int i=0;i<groups.length;i++){
if(selectGroupID != -1){
if(selectGroupID == groups[i].groupID()){%>
<option value=<%=groups[i].groupID()%>  selected="selected"><%=groups[i].groupName()%></option>
<%}else{%>
<option value=<%=groups[i].groupID()%>><%=groups[i].groupName()%></option>
<%}}else{%>
<option value=<%=groups[i].groupID()%>><%=groups[i].groupName()%></option>
<%}}}%>
</select>
</td>
<td width="60%"><input type=hidden name="SUB_ID" value=<%=subId%> size = 32 maxlength=32></td>
</tr>
</table>
</form>

<form name="moveOrRemoveForm" onsubmit='return update4()' method="post" action="rbt_group_main.jsp">
<table width ="60%">
<tr>
<td width="10%"></td>
<td width="20%"><b>Caller ID</b></td>
<td width="20%"><b>Caller Name</b></td>
</tr>
<tr></tr>
<%if(groupMembers != null){
for(int i=0;i<groupMembers.length;i++){%>
<tr>
<%if(i==0){%>
<td width="10%"><input type="radio" name="CallerDetails" checked value =<%=groupMembers[i].callerID()%> ></td>
<%}else{%>
<td width="10%"><input type="radio" name="CallerDetails" value =<%=groupMembers[i].callerID()%> ></td>
<%}%>
<td width="20%"><%=groupMembers[i].callerID()%></td>
<td width="20%"><%=groupMembers[i].callerName()%></td>
</tr>
<%}}%>
</table>
<br/><br/>
<table width="70%">
<tr>
<td width="30%"><input type="radio" name="moveOrRemove" checked value ="Remove" onClick = "document.moveOrRemoveForm.MovetoGroupMenu.disabled=true;">Remove</td>
<td width="30%"><input type="radio" name="moveOrRemove" value ="Move" onClick = "document.moveOrRemoveForm.MovetoGroupMenu.disabled=false;">Move</td>
</tr>
<tr></tr>
<tr>
<td width="30%">Move Selected To</td>
<td width="30%">
<select name="MovetoGroupMenu" disabled>
<%if(groups != null){
for(int i=0;i<groups.length;i++){
%>
<option value=<%=groups[i].groupID()%>><%=groups[i].groupName()%></option>
<%}}%>
</select>
</td>
<td width="60%"><input type="hidden" name="click" value="moveOrRemoveDetails" size = 32 maxlength=32></td>
<td width="60%"><input type=hidden name="SUB_ID" value=<%=subId%> size = 32 maxlength=32></td>
<td width="60%"><input type=hidden name="FROM_GROUP_ID" value=<%=selectGroupID%> size = 32 maxlength=32></td>
<td width="10%"><input type="submit" value="Submit" name="moveRemovebutton" onClick = "return update4()"></td>
</tr>
</table>
</form>
<%}%>
</body>
</html>
<%}
else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}%>