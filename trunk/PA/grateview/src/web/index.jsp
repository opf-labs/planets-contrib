<%@ page
  import="java.io.*,java.net.URI,javax.activation.MimetypesFileTypeMap,eu.planets_project.services.grateview.*,eu.planets_project.services.datatypes.*,eu.planets_project.services.view.CreateViewResult"
%><%

CreateViewResult view = null;
URI testurl = new URI(request.getRequestURL().toString()).resolve("resources/atari.jpg");

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3c.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3c.org/1999/xhtml" 
      xmlns:jsp="http://java.sun.com/JSP/Page" 
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xml:lang="en" lang="en">
<head>
<title>GRATE Viewer Test</title>
</head>
<body>

<% 
view = GrateViewService.createViewerSessionViaService(testurl);
if( view == null ) { %>
<p>FAILED!</p>
<% } else { %>
<p>
<%= view.getViewURL() %> Success!
</p>
<% } %>

</body>
