package eu.planets_project.services.migration.ffmpeg;

/**
 * TODO abr forgot to document this class
 */
public enum FFMpegMigrationPath {
    ;

    private FFMpegFormat in;
    private FFMpegFormat out;
    private String[] tool;

    public static FFMpegMigrationPath lookup(FFMpegFormat in, FFMpegFormat out) throws NoSuchMigrationPathException{
        FFMpegMigrationPath[] values = FFMpegMigrationPath.values();
        for (FFMpegMigrationPath path: values){
            if (path.in == in && path.out == out){
                return path;
            }
        }
        throw new NoSuchMigrationPathException();
    }

    FFMpegMigrationPath(FFMpegFormat in, FFMpegFormat out, String[] tool) {
        this.in = in;
        this.out = out;
        this.tool = tool;
    }

    public FFMpegFormat getIn() {
        return in;
    }

    public FFMpegFormat getOut() {
        return out;
    }

    public String[] getTool() {
        return tool;
    }
}