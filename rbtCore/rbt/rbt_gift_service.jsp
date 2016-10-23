<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub, java.net.*"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.Clips"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.database.ClipsImpl"%>

<%@ include file = "validate.jsp" %>
<%
String browse  = (String)(session.getAttribute("Browse"));
RBTSubUnsub rbtLogin = RBTSubUnsub.init();

String subId=null;
String gifteeId=null;


	
subId=request.getParameter("SUB_ID");	
gifteeId=request.getParameter("Giftee_Id");
	
if (validateUser(request, session, "rbt_subscriber_status.jsp", response)) {%>
<html>
<head>
<%@ include file = "javascripts/RBTValidate.js" %>
<script language="JavaScript">
function update()
{
		if (isEmpty(giftServiceForm.SUB_ID.value))
		    {
			 alert("Enter a Subscriber Number");
		     giftServiceForm.SUB_ID.focus();
		     return false;
			}
		 else {
				if(!isFieldAPhoneNumber(giftServiceForm.SUB_ID.value)){
					alert("Enter valid subscriber phone no.");
					giftServiceForm.SUB_ID.focus();
					return false;
				}
				if (isEmpty(giftServiceForm.Giftee_Id.value))
		    		{
			 		alert("Enter a Gift To Number");
		     		giftServiceForm.SUB_ID.focus();
		     		return false;
					}else {
					if(!isFieldAPhoneNumber(giftServiceForm.Giftee_Id.value)){
					alert("Enter valid gift to phone no.");
					giftServiceForm.Giftee_Id.focus();
					return false;
					}
				}
				
		 }
	
	if(giftServiceForm.forms[0].SUB_ID.disabled)
		alert('WARNING:Please do not refresh the page while your request is getting processed');
	return true;
}
</script>
<title>RBT Subscription</title>
</head>
<body onLoad = 'return fnSetFocus(frmStatus,0)'>
<form name="giftServiceForm" onsubmit='return update()' method="post" action="rbt_gift_main.jsp">
<%
		if(subId != null && gifteeId!= null)
		{
		int clipId=0;				
			String canBeGifted= rbtLogin.canBeGifted(subId,gifteeId,null);
			if(canBeGifted!=null && canBeGifted.substring(5,6).equals("S"))
			{
			System.out.println("inside if not null is " + request.getParameter("Giftee_Id"));
			
				if (rbtLogin.addGift(gifteeId,subId,"CC",null)!=null){
				%>
				<script language="javascript">
					alert("Gifting to <%=gifteeId%>, request completed succesfully ");
				</script>	 
			<%}
			}
			else
			{
				if (canBeGifted.equals("GIFT_FAILURE_GIFTER_NOT_ACT")){%>
					<script language="javascript">
						alert("Gifting to <%=gifteeId%> failed because gifter is not active");
					</script>
				<%}
				else if (canBeGifted.equals("GIFT_FAILURE_ACT_GIFT_PENDING")){%>
					<script language="javascript">
						alert("Gifting to <%=gifteeId%> failed because giftee has service gift pending");
					</script>
				<%}
				else if (canBeGifted.equals("GIFT_FAILURE_GIFT_IN_USE")){%>
					<script language="javascript">
						alert("Gifting to <%=gifteeId%> failed because giftee is already using a gift");
					</script>
				<%}
				else if (canBeGifted.equals("GIFT_FAILURE_ACT_PENDING")){%>
					<script language="javascript">
						alert("Gifting to <%=gifteeId%> failed because giftee is pending for activation");
					</script>
				<%}
				else if (canBeGifted.equals("GIFT_FAILURE_DEACT_PENDING")){%>
					<script language="javascript">
						alert("Gifting to <%=gifteeId%> failed because giftee is pending for de-activation");
					</script>
				<%}
				else if (canBeGifted.equals("GIFT_FAILURE_TECHNICAL_DIFFICULTIES")){%>
					<script language="javascript">
						alert("Gifting to <%=gifteeId%> failed due to technical difficulties");
					</script>
				<%}
				else if (canBeGifted.equals("GIFT_FAILURE_SONG_PRESENT_IN_DOWNLOADS")){%>
					<script language="javascript">
						alert("Gifting to <%=gifteeId%> failed because giftee already has this song.");
					</script>
				<%}
				else if (canBeGifted.equals("GIFT_FAILURE_SONG_GIFT_PENDING")){%>
					<script language="javascript">
						alert("Gifting to <%=gifteeId%> failed because giftee has this gift pending.");
					</script>
				<%}
				else if (canBeGifted.equals("GIFT_FAILURE_SONG_IN_USE")){%>
					<script language="javascript">
						alert("Gifting to <%=gifteeId%> failed because giftee is already using this song.");
					</script>
				<%}
				else if (canBeGifted.equals("GIFT_FAILURE_GIFTEE_INVALID")){%>
					<script language="javascript">
						alert("Gifting to <%=gifteeId%> failed because giftee is invalid.");
					</script>
				<%}
			%>
				
			<%}
			}
		else
		{
			
			response.sendRedirect("rbt_index.jsp");
		}
%>	
<p><b><center>RBT GiftService</center></b></p>
<table align = "center" width ="100%">
<tr>
<td></td>
<td>
<tr></tr>
<tr>
<td width="30%">Subscriber Id</td>
<td width="60%"><input type=text name="SUB_ID" size = 32 maxlength=32></td>
</tr>
<tr></tr>
<td width="30%">Gift To</td>
<td width="60%"><input type=text name="Giftee_Id" size = 32 maxlength=32></td>
</tr>
<tr></tr>
</table>
<p>
		<input type="submit" value="Gift!" name="gift" onClick = "return update()">
</p>
</form>
</body>
</html>
<%}
else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}%>