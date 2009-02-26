package eu.planets_project.services.migration.netpbm;

/**
 * TODO abr forgot to document this class
 */
public enum NetPbmMigrationPath {
    pnmtopnm(NetPbmFormat.pnm,NetPbmFormat.pnm,new String[]{"pnmtopnm"}),
    pgmtopgm(NetPbmFormat.pgm,NetPbmFormat.pgm,new String[]{"pgmtopgm"}),
    ppmtoppm(NetPbmFormat.ppm,NetPbmFormat.ppm,new String[]{"ppmtoppm"}),
    pamtopam(NetPbmFormat.pam,NetPbmFormat.pam,new String[]{"pamtopam"}),
    pbmtopgm(NetPbmFormat.pbm,NetPbmFormat.pgm,new String[]{"pbmtopgm"}),
    pgmtopbm(NetPbmFormat.pgm,NetPbmFormat.pbm,new String[]{"pgmtopbm"}),
    pgmtoppm(NetPbmFormat.pgm,NetPbmFormat.ppm,new String[]{"pgmtopbm"}),
    ppmtopgm(NetPbmFormat.pbm,NetPbmFormat.pgm,new String[]{"ppmtopgm"}),

    jpegtopng(NetPbmFormat.jpeg,NetPbmFormat.png,new String[]{"jpegtopnm","|","pnmtopng"}),
    ;


    private NetPbmFormat in;
    private NetPbmFormat out;
    private String[] tool;

    public static NetPbmMigrationPath lookup(NetPbmFormat in, NetPbmFormat out) throws NoSuchMigrationPathException{
        NetPbmMigrationPath[] values = NetPbmMigrationPath.values();
        for (NetPbmMigrationPath path: values){
            if (path.in == in && path.out == out){
                return path;
            }
        }
        throw new NoSuchMigrationPathException();
    }

    NetPbmMigrationPath(NetPbmFormat in, NetPbmFormat out, String[] tool) {
        this.in = in;
        this.out = out;
        this.tool = tool;
    }

    public NetPbmFormat getIn() {
        return in;
    }

    public NetPbmFormat getOut() {
        return out;
    }

    public String[] getTool() {
        return tool;
    }
}
