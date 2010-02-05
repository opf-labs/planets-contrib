<%@ page
  import="java.io.*,java.util.List,java.net.URLDecoder,javax.activation.MimetypesFileTypeMap,eu.planets_project.services.jj2000.JJ2000ViewerService,eu.planets_project.services.datatypes.DigitalObject,eu.planets_project.ifr.core.storage.utils.DigitalObjectDiskCache"
%><% 

// Pick up the parameters:
String sid = request.getParameter("sid");
int fid = Integer.parseInt(request.getParameter("fid"));

// Decode the file name (might contain spaces and on) and prepare file object.
sid = URLDecoder.decode(sid, "UTF-8");

// Open the result:
List<DigitalObject> cache = DigitalObjectDiskCache.recoverDigitalObjects( sid );
DigitalObject dob = cache.get(fid);

// Does this DOB exist?
if( dob != null ) {

    // Set the mime type to be jp2:
    response.setContentType( "image/jp2" );

    // Now stream out the data:
    DataInputStream in = new DataInputStream(dob.getContent().getInputStream());
    ServletOutputStream op = response.getOutputStream();
    byte[] bbuf = new byte[2*1024];
    int length = 0;

    try {
        while ((in != null) && ((length = in.read(bbuf)) != -1))
            {
            op.write(bbuf,0,length);
            }
    } finally {
            in.close();
            op.flush();
            op.close();
    }

}
%>