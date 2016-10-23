<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub, com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper"%>
<%@ page import = "java.util.StringTokenizer"%>

<%@ include file = "validate.jsp" %>
<%
if (validateUser(request, session,  "rbt_all_clips.jsp", response)) {
	%>

<html>
<head>
<title> RBT Clips </title>



<script language="javascript">
	function init(){
		document.allClipsForm.CLIP_ID.disabled=true;
	}
</script>
</head>
<body topmargin="0" bottommargin="0" leftmargin="0" rightmargin="0" onload="javascript:init();">
<table border="0" cols=3 width="100%" cellspacing="0" cellpadding="0" height="100%">
	<tr>
		<td colspan=3 valign="top" height="15%">
			<jsp:include page="rbt_header_inc.jsp"/>
		</td>
	</tr>

	<tr>
		<td colspan=3 valign="top" height="85%">
			<table border="0" width="100%" cellspacing="0" cellpadding="0" height="100%">
				<tr>
					<!--Menu Start -->
					<td width="23%" bgcolor="#ffdec8" valign="top">
						<jsp:include page="rbt_menu_inc.jsp" />
					</td>
					<td width="77%" bgcolor="#ffedd9" valign="top">
<br><br>
<form name="allClipsForm" action="rbt_all_clips_result.jsp" method=post>

	<p><b><center>View Clips</center></b></p>
	<table align="center" cols=2 cellpadding=6 width="75%" style="VERTICAL-ALIGN: top" >
	<tr>
		<td width="30%"><input type="radio" name="SELECT_CLIP" value="SELECTED_CLIP" onClick = "document.allClipsForm.CLIP_ID.value='';document.allClipsForm.CLIP_ID.disabled=false;document.allClipsForm.ARTIST_ID.value='';document.allClipsForm.ARTIST_ID.disabled=true;">Song Name</td>
		<td width="60%"><input type="text" name="CLIP_ID" size = 32 maxlength=32></td>
	</tr>
	<tr></tr>
	<tr>
		<td width="30%"><input type="radio" name="SELECT_CLIP" value="ARTIST" onClick = "document.allClipsForm.ARTIST_ID.value='';document.allClipsForm.ARTIST_ID.disabled=false;document.allClipsForm.CLIP_ID.value='';document.allClipsForm.CLIP_ID.disabled=true;">Artist Name</td>
		<td width="60%"><input type="text" name="ARTIST_ID" size = 32 maxlength=32 disabled="true"></td>
	</tr>
	<tr></tr>
	<tr>
		<td width="30%"><input type="radio" name="SELECT_CLIP" value="ALL_CLIP" checked  onClick = "document.allClipsForm.CLIP_ID.value='';document.allClipsForm.CLIP_ID.disabled=true;document.allClipsForm.ARTIST_ID.value='';document.allClipsForm.ARTIST_ID.disabled=true;" >All Songs</td>
		<td></td>
	</tr>
	<tr></tr>
	<tr>
		<td></td>
		<td><input type="submit" border = "0" value="Submit" name="SUBMIT" ></td>
	</tr>
	</table>
	
</form>
</td>
</tr>
</table>
</td>
</tr>
</table>
</body>
</html>
<%				
}else{
	session.invalidate();
	response.sendRedirect("index.jsp"); 
}%>
