<%@ page
  import="java.io.*,java.net.URL,javax.activation.MimetypesFileTypeMap,eu.planets_project.services.jj2000.JJ2000ViewerService,eu.planets_project.services.datatypes.*,eu.planets_project.services.view.CreateViewResult"
%><%

CreateViewResult view = JJ2000ViewerService.createViewerSessionViaService(
        new URL("http","localhost",8080,"/pserv-pa-jj2000/resources/world.jp2") );

%>
<html>
<head>
<title>Viewer Test Page</title>
</head>
<body>
<p>
This should be a simple test page, where a JP2 can be chosen or uploaded, to be viewed.<br/>
<a href="<%= view.getViewURL() %>"><%= view.getViewURL().getPath() %></a>.
</p>
</body>
