<%@ page
  import="java.io.*,java.net.URI,javax.activation.MimetypesFileTypeMap,
  eu.planets_project.services.qemu.*,
  eu.planets_project.services.datatypes.*,eu.planets_project.services.view.CreateViewResult"
%>

<%
  String sessName = request.getParameter("sessName");
 
  if(sessName != "")
  {
    QemuViewService qv = new QemuViewService();
    CreateViewResult viewResult = qv.createView(null, null);
 
    if(viewResult != null)
    {
    %>
      <script type="text/javascript">
        window.open("<%= viewResult.getViewURL()  %>");
      </script>
    <%
    }
  }
%>
 
 
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>PLANETS project, Qemu Viewer Service: "Session Recording"</title>
    <link rel="stylesheet" type="text/css" media="all" charset="UTF-8" href="styles.css">
  </head>
  
  <body>
    <div id="page">
      <table>
	<tr>
	  <td id="sessRec">
	   <applet archive=vncplay.jar code="VncViewer.class" width=650 height=650>
		<param name="HOST"       value="localhost">
		<param name="PORT"       value="5900">
		<param name="autorecord" value="yes">
	</applet>
	  </td>

	  <td id="sessOpt">
	    <form method="get">
	      <div class="options">
		Session Name: <br />
	        <input type="text" name="sessName" id="sessName" /> <br />
              </div>

	      <div class="options">
		Session Description: <br />
	        <textarea type="text" name="sessDesc" id="sessDesc"></textarea> <br />
	      </div>

	      <div class="options">
		OS Image: <br />
		<select id="osImage">
		  <option>Windows3.11</option>
		  <option>Windows95</option>
		  <option>Windows98</option>
		</select>
		<br />
	      </div>	

	      <div class="options">
		Inject file(s): <br />
		<input type="file" name="fileInject" id="fileInject" /> <br />
		<select multiple id="injectedFiles">
		  <option>File1</option>
		  <option>File2</option>
		  <option>File3</option>
		  <option>File4</option>
		</select>
		<br />
              </div>

	      <div class="options">
	        <input type="submit" value="Start" class="buttons" />
	        <input type="reset" value="Clear" class="buttons" onClick="startApplet" />
	      </div>
	    </form>
	  </td>
	</tr>
      </table>
    </div>
  </body>
</html>
