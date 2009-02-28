<%@ page
  import="java.io.*,java.net.URI,javax.activation.MimetypesFileTypeMap,eu.planets_project.services.jj2000.JJ2000ViewerService,eu.planets_project.services.datatypes.*,eu.planets_project.services.view.CreateViewResult"
%><%

// View state:
URI jp2url = null;
CreateViewResult view = null;

// Attempt to parse any passed JP2 URL:
String jp2str = request.getParameter("jp2url");
if( jp2str != null ) jp2url = new URI(jp2str);

// If there is a JP2 to view, create the viewer:
if( jp2url != null ) {
  view = JJ2000ViewerService.createViewerSessionViaService( jp2url );
}

URI example = new URI(request.getRequestURL().toString()).resolve("resources/world.jp2");

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3c.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html 
    xmlns="http://www.w3c.org/1999/xhtml" 
    xmlns:jsp="http://java.sun.com/JSP/Page" 
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xml:lang="en" lang="en">
<head>
<title>JJ2000 Viewer Service</title>
</head>
<body>
<img src="logos/jj2000_logo_150w.png" style="float: right"/>
<h1>JJ2000 Viewer Service</h1>
<p>
This is a JPEG2000 online image viewer service, based on the <a href="http://jj2000.epfl.ch/">JJ2000 code</a>.  It has been created as part of the <a href="http://www.planets-project.eu/">PLANETS project</a>.
It is intended to be used as a web service, but can also be tested directly using the form below.
</p>

<% if( jp2url == null || view.getViewURL() == null ) { %>

<p>
Please specify the URL of a JP2 image you would like to view:
</p>

<form method="get">
  <input type="text" name="jp2url" value="<%= example %>" size="60"/>
  <br/>
  <input type="submit" value="View" />
</form>

<% } else { %>

<p>
<a href="<%= view.getViewURL() %>">Click here to view.</a>
</p>

<% } %>

</body>
