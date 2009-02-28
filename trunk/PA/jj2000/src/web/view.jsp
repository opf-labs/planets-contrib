<%@ page
  import="java.io.*,java.net.URLDecoder,java.util.List,javax.activation.MimetypesFileTypeMap,eu.planets_project.services.jj2000.JJ2000ViewerService,eu.planets_project.services.datatypes.DigitalObject"
%><% 

// Pick up the parameters:
String sid = request.getParameter("sid");
int fid = 0;
if(request.getParameter("fid") != null ) {
    fid = Integer.parseInt(request.getParameter("fid")); 
}

// Decode the file name (might contain spaces and on) and prepare file object.
sid = URLDecoder.decode(sid, "UTF-8");

// Look at the file list:
List<DigitalObject> dobs = JJ2000ViewerService.recoverDigitalObjects(sid);
if( fid >= dobs.size() ) fid = 0;

%>
<html>
<head>
</head>
<body style="background-color: #eeeeee;">

<div id="infoPanel">
<%
  if( sid != null ) {
%>
<b><%= dobs.size() %></b>
<% } else { %>
<b>Hello</b>
<% } %>
</div>

<div
  style="position: absolute; top: 32px; bottom: 0; left: 0; right: 0;">
<applet code="jj2000.j2k.decoder.SimpleAppletDecoder.class"
  archive="resources/jj2000-aplt-4.1.jar" width="100%" height="100%">
  <param name="i" value="cache.jsp?sid=<%= sid %>&fid=<%= fid %>">
</applet></div>

</body>
</html>
