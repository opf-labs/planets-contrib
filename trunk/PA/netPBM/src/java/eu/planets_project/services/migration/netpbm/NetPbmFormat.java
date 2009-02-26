package eu.planets_project.services.migration.netpbm;

import eu.planets_project.ifr.core.techreg.api.formats.Format;

import java.net.URI;

/**
 * TODO abr forgot to document this class
 */
public enum NetPbmFormat {
    pnm(Format.extensionToURI("pnm")),
    pam(Format.extensionToURI("pam")),
    pbm(Format.extensionToURI("pbm")),
    pgm(Format.extensionToURI("pgm")),
    ppm(Format.extensionToURI("ppm")),

    jpeg(Format.extensionToURI("ppm")),
    jpeg2k(Format.extensionToURI("ppm")),
    bmp(Format.extensionToURI("ppm")),
    winicon(Format.extensionToURI("ppm")),

    gif(Format.extensionToURI("ppm")),
    png(Format.extensionToURI("ppm")),
    palm(Format.extensionToURI("ppm")),
    pddbug(Format.extensionToURI("ppm")),

    jbig(Format.extensionToURI("ppm")),
    fiasco(Format.extensionToURI("ppm")),
    svg(Format.extensionToURI("ppm")),
    pfm(Format.extensionToURI("ppm")),

    mrf(Format.extensionToURI("ppm")),
    hpcd(Format.extensionToURI("ppm")),
    nokia(Format.extensionToURI("ppm")),
    wbmp(Format.extensionToURI("ppm")),

    octave(Format.extensionToURI("ppm")),
    htmltbl(Format.extensionToURI("ppm")),
    mda(Format.extensionToURI("ppm")),
    atk(Format.extensionToURI("ppm")),

    brush(Format.extensionToURI("ppm")),
    cmuwm(Format.extensionToURI("ppm")),
    g3(Format.extensionToURI("ppm")),
    icon(Format.extensionToURI("ppm")),

    gem(Format.extensionToURI("ppm")),
    macp(Format.extensionToURI("ppm")),
    mgr(Format.extensionToURI("ppm")),
    info(Format.extensionToURI("ppm")),

    neo(Format.extensionToURI("ppm")),
    pi1(Format.extensionToURI("ppm")),
    pc1(Format.extensionToURI("ppm")),
    pi3(Format.extensionToURI("ppm")),

    xbm(Format.extensionToURI("ppm")),
    vbm(Format.extensionToURI("ppm")),
    ybm(Format.extensionToURI("ppm")),
    epson(Format.extensionToURI("ppm")),

    escp2(Format.extensionToURI("ppm")),
    g10x(Format.extensionToURI("ppm")),
    pclxl(Format.extensionToURI("ppm")), //HP PCL-XL (PCL6) printer language
    pjxl(Format.extensionToURI("ppm")), //HP Paintjet XL PCL

    lj(Format.extensionToURI("ppm")), //HP laserjet
    pj(Format.extensionToURI("ppm")),//HP paintjet
    thinkjet(Format.extensionToURI("ppm")), //HP Thinkjet printer stream
    ppa(Format.extensionToURI("ppm")), //HP Printer Performance Architecture printer stream

    mitsu(Format.extensionToURI("ppm")),
    ibm23xx(Format.extensionToURI("ppm")),
    att4425(Format.extensionToURI("ppm")),
    ascii(Format.extensionToURI("ppm")),


    bbnbg(Format.extensionToURI("ppm")),
    fits(Format.extensionToURI("ppm")),
    fs(Format.extensionToURI("ppm")),

    hips(Format.extensionToURI("ppm")),
    lispm(Format.extensionToURI("ppm")),
    ps(Format.extensionToURI("ppm")),
    psid(Format.extensionToURI("ppm")),

    lps(Format.extensionToURI("ppm")),
    epsi(Format.extensionToURI("ppm")),
    psg3(Format.extensionToURI("ppm")),
    raw(Format.extensionToURI("ppm")),

    //THere is plenty more.....

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
