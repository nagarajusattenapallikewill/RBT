<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper"%>
<%@ page import = "com.onmobile.apps.ringbacktones.cache.content.ClipMinimal"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ page import = "com.onmobile.apps.ringbacktones.lucene.LuceneClip"%>
<%@ page import = "java.util.*"%>
<%@ page import = "java.text.SimpleDateFormat"%>

<%@ include file = "validate.jsp" %>
<%
if (validateUser(request, session,  "rbt_all_clips.jsp", response)) { 
	String selected=null;
	boolean allowSel = false; 
    String allowSelStr  = (String)(session.getAttribute("SelFromListClip")); 
    if(allowSelStr != null && allowSelStr.equals("TRUE")) 
		allowSel = true; 
	if(request.getParameter("SELECT_CLIP") != null)
	{
		selected = request.getParameter("SELECT_CLIP");
		session.setAttribute("SELECT_CLIP",selected);
		if ("ALL_CLIP".equals(selected))
		{
			session.setAttribute("CLIP_ID",null);
		}
		else if ("SELECTED_CLIP".equals(selected))
		{
			String selected_clip = request.getParameter("CLIP_ID");
			if (!"".equals(selected_clip))
				session.setAttribute("CLIP_ID",selected_clip);
			else
				session.setAttribute("CLIP_ID",null);
		}
		else if ("ARTIST".equals(selected)) { 
		    String selected_clip = request.getParameter("ARTIST_ID"); 
		    if (!"".equals(selected_clip)) 
				   session.setAttribute("ARTIST_ID",selected_clip); 
		    else 
				   session.setAttribute("ARTIST_ID",null); 
	    } 

	}%>

<html>
<head>
<title> RBT Clips </title>
</head>
<body topmargin="0" bottommargin="0" leftmargin="0" rightmargin="0">
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
<form name="allClipsResultForm" method=post>

	<%RBTMOHelper rbtMOHelper = RBTMOHelper.init();
	RBTSubUnsub rbtLogin = RBTSubUnsub.init();
	if ("ALL_CLIP".equals((String)session.getAttribute("SELECT_CLIP"))){
	String startVar ="A";
	if(request.getQueryString() !=null)
		startVar = request.getQueryString();
	String [] alphabets = {"[1-9]","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"}; 
	ClipMinimal[] matchedClips = rbtMOHelper.getAllClipsByStartLetter(startVar);
%>
	<p><b><center>Clips</center></b></p>
	<center>
	<%for(int i=0; i<alphabets.length; i++){ if(i<(alphabets.length-1)){%>
	<a href = "rbt_all_clips_result.jsp?<%=alphabets[i]%>"><b><%=alphabets[i]%></b></a>&nbsp;|&nbsp;
	<%}else{%>
	<a href = "rbt_all_clips_result.jsp?<%=alphabets[i]%>"><b><%=alphabets[i]%></b></a></center><br><br>
	<% }}if(matchedClips == null){%>
		&nbsp;&nbsp;&nbsp;No clips available.
	<%}else{ %>
	<table align="center" cols=1 cellpadding=6 width="95%" style="VERTICAL-ALIGN: top" >

			<td width="20%" height="19" align="center"><b>CLIP NAME</b></td>
			<td width="20%" height="19" align="center"><b>PROMO ID</b></td>
			<td width="20%" height="19" align="center"><b>CLIP ID</b></td>
			<td width="20%" height="19" align="center"><b>ARTIST</b></td>
			<% if(rbtLogin.m_showClipExpiryDateInListClips){%>
			<td width="45%" height="19" align="center"><b>CLIP EXPIRY DATE</b></td>
			<%}%>
			<%for(int i = 0; i < matchedClips.length; i++){
				ClipMinimal clip = matchedClips[i];

				/*String clipName = (String) clipsIter.next();
				String promoFromMap = (String)matchedClips.get(clipName);
				String promoID = " - ";	
				String clipID = " - ";	
				if(promoFromMap != null && !promoFromMap.trim().equalsIgnoreCase("?"))
				{
					promoID = promoFromMap;
				}
				if(rbtMOHelper.m_clipIDPromoID != null && promoID != null && !promoID.equalsIgnoreCase(" - "))
				{
					if(rbtMOHelper.m_clipIDPromoID.containsKey(promoID))
						clipID = (String)rbtMOHelper.m_clipIDPromoID.get(promoID);
				}*/
				String clipName = clip.getClipName();
				String promoID = " - ";
				Date expiryDate = null;
				String clipExpiryDate = "-";
				int clipID = clip.getClipId();
				String artist = " - ";
				if(clip.getPromoID() != null && !clip.getPromoID().equals("?"))
					promoID = clip.getPromoID();
				if(clip.getArtist() != null && !clip.getArtist().equals("?"))
					artist = clip.getArtist();
				if(clip.getEndTime() != null)
				{
					expiryDate = clip.getEndTime();
					SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
					clipExpiryDate = sdf.format(expiryDate);
				}
			%>
			<tr>
				<% if(allowSel) { %><td width="20%" height="19" align="center"><a href="rbt_subs_selections_promocode.jsp?promoCode=<%=promoID%>"><%=clipName%></a></td>
				<% } else { %>
				<td width="20%" height="19" align="center"><%=clipName%></td>
				<% } %>
				<td width="20%" height="19" align="center"><%=promoID%></td>
				<td width="20%" height="19" align="center"><%=clipID%></td>
				<td width="20%" height="19" align="center"><%=artist%></td>
				<% if(rbtLogin.m_showClipExpiryDateInListClips){%>
				<td width="45%" height="19" align="center"><%=clipExpiryDate%></td>
				<%}%>
			</tr>
		<% }%>
		</table>
		<%}}else if("SELECTED_CLIP".equals((String)session.getAttribute("SELECT_CLIP")) || "ARTIST".equals((String)session.getAttribute("SELECT_CLIP"))){
			
			HashMap<String, String> searchMap  = new HashMap<String, String>();
			String searchString = null;
			if ("ARTIST".equals((String)session.getAttribute("SELECT_CLIP"))) 
			{
				searchString = ((String)session.getAttribute("ARTIST_ID")).trim();
				searchMap.put("artist",searchString);
			}
			else
			{
				searchString = ((String)session.getAttribute("CLIP_ID")).trim();
				searchMap.put("song",searchString);
			}
				
			ArrayList<LuceneClip> results = rbtLogin.luceneIndexer.searchQuery(searchMap, 0, rbtLogin._ccSearchCount);
			
			%>
			<p><b><center>Clips</center></b></p>
			<table align="center" cols=1 cellpadding=6 width="95%" style="VERTICAL-ALIGN: top" >
			<%if(results == null || results.size() <= 0){%>
			&nbsp;&nbsp;&nbsp;No clips available.
			<%}else{%>
				<tr><td width="20%" height="19" align="center"><b>CLIP NAME</b></td>
				<td width="20%" height="19" align="center"><b>PROMO ID</b></td>
				<td width="20%" height="19" align="center"><b>CLIP ID</b></td>
				<td width="20%" height="19" align="center"><b>ARTIST</b></td>
				<% if(rbtLogin.m_showClipExpiryDateInListClips){%>
				<td width="45%" height="19" align="center"><b>CLIP EXPIRY DATE</b></td></tr>
				<%}%>
				<%for(int index=0; index < results.size(); index++)
				{
					LuceneClip clip = results.get(index);
					String promoID = " - ";
					String artist = " - ";
					Date expiryDate = null;	
					String clipExpiryDate = "-";
					String clipID = ""+clip.getClipId();
					if(clip.getClipPromoId() != null && !clip.getClipPromoId().trim().equalsIgnoreCase("?"))
					{
						promoID = clip.getClipPromoId();
					}
					if(clip.getArtist() != null && !clip.getArtist().trim().equalsIgnoreCase("?"))
					{
						artist = clip.getArtist();
					}
					if(clip.getClipEndTime() != null)
					{
						expiryDate = clip.getClipEndTime();
						SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
						clipExpiryDate = sdf.format(expiryDate);
					}
				%><tr>
					<% if(allowSel) { %><td width="25%" height="19" align="center"><a href="rbt_subs_selections_promocode.jsp?promoCode=<%=promoID%>"><%=clip.getClipName()%></a></td>
					<% } else { %>
					<td width="25%" height="19" align="center"><%=clip.getClipName()%></td>
					<% } %>
					<td width="20%" height="19" align="center"><%=promoID%></td>
					<td width="20%" height="19" align="center"><%=clipID%></td>
					<td width="20%" height="19" align="center"><%=artist%></td>
					<% if(rbtLogin.m_showClipExpiryDateInListClips){%>
					<td width="45%" height="19" align="center"><%=clipExpiryDate%></td>
					</tr>
					<%}%>
				<%}
			}%>
			</table>
			<%}%>
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
