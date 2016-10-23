<%
  if (session != null)
  {
  	   session.invalidate();
	   response.setHeader("Cache-Control", "no-store, no-cache");
	   response.setHeader("Pragma", "no-cache");
	   response.setDateHeader("Expires", 0);  }
	   response.sendRedirect("index.jsp");
%>

