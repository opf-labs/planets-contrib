package eu.planets_project.services.migration.ffmpeg;



import java.net.URI;

/**
 * TODO abr forgot to document this class
 */
public enum FFMpegFormat {
    ;
    private URI format;

    FFMpegFormat(URI format) {
        this.format = format;
    }

    public static FFMpegFormat loopup(URI format) throws NoSuchFormatException{
        FFMpegFormat[] values = FFMpegFormat.values();
        for (FFMpegFormat value: values){
            if (value.format.equals(format)){
                return value;
            }
        }

        throw new NoSuchFormatException();
    }

    public URI getFormat() {
        return format;
    }
}