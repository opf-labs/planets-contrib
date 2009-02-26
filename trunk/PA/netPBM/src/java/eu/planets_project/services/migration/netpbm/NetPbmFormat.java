package eu.planets_project.services.migration.netpbm;

import java.net.URI;

/**
 * TODO abr forgot to document this class
 */
public enum NetPbmFormat {
    ;
    private URI format;

    NetPbmFormat(URI format) {
        this.format = format;
    }

    public static NetPbmFormat loopup(URI format) throws NoSuchFormatException{
        NetPbmFormat[] values = NetPbmFormat.values();
        for (NetPbmFormat value: values){
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
