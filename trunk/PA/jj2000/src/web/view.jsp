<%@ page
  import="java.io.*,java.net.URLDecoder,java.util.List,javax.activation.MimetypesFileTypeMap,eu.planets_project.services.jj2000.JJ2000ViewerService,eu.planets_project.services.datatypes.DigitalObject,eu.planets_project.services.utils.cache.DigitalObjectDiskCache"
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
List<DigitalObject> dobs = DigitalObjectDiskCache.recoverDigitalObjects(sid);
if( fid >= dobs.size() ) fid = 0;

// Create title
String pageTitle = "Viewing Digital Object "+(fid+1)+" of "+dobs.size();

%>
<html>
<head>
<title>JJ2000 Viewer: <%= pageTitle %></title>
<style type="text/css">
body {
  margin: 0; 
  padding:0;
}
#footer form {
 margin: 0;
}
select, input, #footer {
  font-size: 11px;
}
#footer td {
  font-size: 14px;
}
#footer img {
 margin: 0 3px;
}
</style>
</head>
<body>

<table id="page" cellpadding="0" cellspacing="0" border="1"
  style="width:100%; height:100%; margin:0; padding: 0; border-collapse: collapse;">
  <tr>
  <td style="height: 100%" id="main">

<applet code="jj2000.j2k.decoder.SimpleAppletDecoder.class"
  archive="resources/jj2000-aplt-4.1.jar" width="100%" height="100%">
  <param name="i" value="cache.jsp?sid=<%= sid %>&fid=<%= fid %>">
</applet>

  </td>
  </tr>
  <tr>
  <td style="height: 28px;" id="footer">
  
<table cellpadding="0" cellspacing="0" border="0"
  style="width:100%; height:100%; margin:0; padding: 0; border-collapse: collapse;">
<tr style="vertical-align: middle;">
<td>
  <img src="logos/jj2000_logo_22h.png"/>
</td>
<td>
  <form method="GET">
    <input type="hidden" name="sid" value="<%=sid %>"/>
    <select name="fid" onChange="this.form.submit();">
    <% for( int i=0; i < dobs.size(); i++ ) { 
        String selected="";
        if( fid == i ) selected=" selected=\"selected\"";
    %>
      <option value="<%= i %>"<%=selected %>>View <%= (i+1) %> of <%= dobs.size() %></option>
    <% } %>
    </select>
    <input type="submit" value="go"/>
  </form>
</td>
<td>
  <i><%= pageTitle %></i>
</td>
</tr>
</table>

</td>
</tr>
</table>


</body>
</html>
