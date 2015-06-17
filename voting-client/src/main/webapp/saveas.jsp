<%@page contentType="text/html" pageEncoding="UTF-8"%><%

	response.setHeader("Content-Disposition", "attachment; filename=\""+request.getParameter("name")+"\"" );
	out.print(request.getParameter("data"));

%>