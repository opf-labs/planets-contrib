<%@ page
  import="java.io.*,java.net.URLDecoder,javax.activation.MimetypesFileTypeMap,eu.planets_project.services.jj2000.JJ2000ViewerService,eu.planets_project.services.datatypes.DigitalObject,eu.planets_project.services.utils.cache.DigitalObjectDiskCache"
%><% 

// Pick up the parameters:
String sid = request.getParameter("sid");
int fid = Integer.parseInt(request.getParameter("fid"));

// Decode the file name (might contain spaces and on) and prepare file object.
sid = URLDecoder.decode(sid, "UTF-8");

// Open the file:
DigitalObject f = DigitalObjectDiskCache.findCachedDigitalObject( sid, fid );

// Does this DOB exist?
if( f != null ) {

    // Set the mime type to be jp2:
    response.setContentType( "image/jp2" );

    // Now stream out the data:
    byte[] bbuf = new byte[2*1024];
    DataInputStream in = new DataInputStream(f.getContent().read());
    int length = 0;
    ServletOutputStream op = response.getOutputStream();

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