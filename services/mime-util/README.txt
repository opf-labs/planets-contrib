The Mime-Type Extractor Service uses the Mime Type Detection Utility (Mime-Util) project from sourceforge
https://sourceforge.net/projects/mime-util/

It runs under Apache License V2.0 and enables Java programs to detect MIME types based on file extensions, magic data and content sniffing.
It supports detection from java.io.File, java.io.InputStream, java.net.URL and byte arrays. 
See the project website www.eoss.org.nz/mime-util for detail

The given mime-util service wrapper at hand provides a wrapper following the Planets preservation interfaces
'Identify' and 'Validate'.

See service description for details which mime-type detection operations (e.g. MagicMimeMimeDetector, OpendesktopMimeDetector, TextMimeDetector, WindowsRegistryMimeDetector) are wrapped on how their usage can be specified using Planets.ServiceParameters.