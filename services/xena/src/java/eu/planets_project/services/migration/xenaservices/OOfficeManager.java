/**
 * 
 */
package eu.planets_project.services.migration.xenaservices;

import com.sun.star.beans.PropertyValue;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;


/**
 * 
 * This is just example code that illustrates some options. It is not a service.
 * 
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 *
 */
public class OOfficeManager {

        public static void main(String[] args) {
            String loadUrl="file:///c:/dev/netbeans/oootest/mydoc.odt";
            String storeUrl="file:///c:/dev/netbeans/oootest/mydoc.pdf";

            try {
                XComponentContext xContext = Bootstrap.bootstrap();
                XMultiComponentFactory xMultiComponentFactory = xContext.getServiceManager();
                XComponentLoader xcomponentloader = (XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class,xMultiComponentFactory.createInstanceWithContext("com.sun.star.frame.Desktop", xContext));

                Object objectDocumentToStore = xcomponentloader.loadComponentFromURL(loadUrl, "_blank", 0, new PropertyValue[0]);

                // Create PDF filter data
                PropertyValue pdfFilterData[] = new PropertyValue[19];

                // Filter data comments origin:
                // http://www.openoffice.org/nonav/issues/showattachment.cgi/37895/draft-doc-pdf-security.odt
                // http://specs.openoffice.org/appwide/pdf_export/PDFExportDialog.odt

                // Set the password that a user will need to change the permissions
                // of the exported PDF. The password should be in clear text.
                // Must be used with the "RestrictPermissions" property
                pdfFilterData[0] = new PropertyValue();
                pdfFilterData[0].Name = "PermissionPassword";
                pdfFilterData[0].Value = "nopermission";

                // Specify that PDF related permissions of this file must be
                // restricted. It is meaningfull only if the "PermissionPassword"
                // property is not empty
                pdfFilterData[1] = new PropertyValue();
                pdfFilterData[1].Name = "RestrictPermissions";
                pdfFilterData[1].Value = new Boolean(true);

                // Set the password you must know to open the PDF document
                pdfFilterData[2] = new PropertyValue();
                pdfFilterData[2].Name = "DocumentOpenPassword";
                pdfFilterData[2].Value = "open";

                // Specifies that the PDF document should be encrypted while
                // exporting it, meanifull only if the "DocumentOpenPassword"
                // property is not empty
                pdfFilterData[3] = new PropertyValue();
                pdfFilterData[3].Name = "EncryptFile";
                pdfFilterData[3].Value = new Boolean(true);

                // Specifies printing of the document:
                //   0: PDF document cannot be printed
                //   1: PDF document can be printed at low resolution only
                //   2: PDF document can be printed at maximum resolution.
                pdfFilterData[4] = new PropertyValue();
                pdfFilterData[4].Name = "Printing";
                pdfFilterData[4].Value = new Integer(0);

                // Specifies the changes allowed to the document:
                //   0: PDF document cannot be changed
                //   1: Inserting, deleting and rotating pages is allowed
                //   2: Filling of form field is allowed
                //   3: Filling of form field and commenting is allowed
                //   4: All the changes of the previous selections are permitted,
                //      with the only exclusion of page extraction
                pdfFilterData[5] = new PropertyValue();
                pdfFilterData[5].Name = "Changes";
                pdfFilterData[5].Value = new Integer(4);

                // Specifies that the pages and the PDF document content can be
                // extracted to be used in other documents: Copy from the PDF
                // document and paste eleswhere
                pdfFilterData[6] = new PropertyValue();
                pdfFilterData[6].Name = "EnableCopyingOfContent";
                pdfFilterData[6].Value = new Boolean(false);

                // Specifies that the PDF document content can be extracted to
                // be used in accessibility applications
                pdfFilterData[7] = new PropertyValue();
                pdfFilterData[7].Name = "EnableTextAccessForAccessibilityTools";
                pdfFilterData[7].Value = new Boolean(false);
               
                // Specifies which pages are exported to the PDF document.
                // To export a range of pages, use the format 3-6.
                // To export single pages, use the format 7;9;11.
                // Specify a combination of page ranges and single pages
                // by using a format like 2-4;6.
                // If the document has less pages than defined in the range,
                // the result might be the exception
                // "com.sun.star.task.ErrorCodeIOException".
                // This exception occured for example by using an ODT file with
                // only one page and a page range of "2-4;6;8-10". Changing the
                // page range to "1" prevented this exception.
                // For no apparent reason the exception didn't occure by using
                // an ODT file with two pages and a page range of "2-4;6;8-10".
                pdfFilterData[8] = new PropertyValue();
                pdfFilterData[8].Name = "PageRange";
                pdfFilterData[8].Value = "2-4;6;8-10";
                // pdfFilterData[8].Value = "1";
               
                // Specifies if graphics are exported to PDF using a
                // lossless compression. If this property is set to true,
                // it overwrites the "Quality" property
                pdfFilterData[9] = new PropertyValue();
                pdfFilterData[9].Name = "UseLosslessCompression";
                pdfFilterData[9].Value = new Boolean(true);
               
                // Specifies the quality of the JPG export in a range from 0 to 100.
                // A higher value results in higher quality and file size.
                // This property affects the PDF document only, if the property
                // "UseLosslessCompression" is false
                pdfFilterData[10] = new PropertyValue();
                pdfFilterData[10].Name = "Quality";
                pdfFilterData[10].Value = new Integer(50);

                // Specifies if the resolution of each image is reduced to the
                // resolution specified by the property "MaxImageResolution".
                // If the property "ReduceImageResolution" is set to true and
                // the property "MaxImageResolution" is set to a DPI value, the
                // exported PDF document is affected by this settings even if
                // the property "UseLosslessCompression" is set to true, too
                pdfFilterData[11] = new PropertyValue();
                pdfFilterData[11].Name = "ReduceImageResolution";
                pdfFilterData[11].Value = new Boolean(true);
               
                // If the property "ReduceImageResolution" is set to true
                // all images will be reduced to the given value in DPI
                pdfFilterData[12] = new PropertyValue();
                pdfFilterData[12].Name = "MaxImageResolution";
                pdfFilterData[12].Value = new Integer(100);

                // Specifies whether form fields are exported as widgets or
                // only their fixed print representation is exported
                pdfFilterData[13] = new PropertyValue();
                pdfFilterData[13].Name = "ExportFormFields";
                pdfFilterData[13].Value = new Boolean(false);

                // Specifies that the PDF viewer window is centered to the
                // screen when the PDF document is opened
                pdfFilterData[14] = new PropertyValue();
                pdfFilterData[14].Name = "CenterWindow";
                pdfFilterData[14].Value = new Boolean(true);

                // Specifies the action to be performed when the PDF document
                // is opened:
                //   0: Opens with default zoom magnification
                //   1: Opens magnified to fit the entire page within the window
                //   2: Opens magnified to fit the entire page width within
                //      the window
                //   3: Opens magnified to fit the entire width of its boundig
                //      box within the window (cuts out margins)
                //   4: Opens with a zoom level given in the "Zoom" property
                pdfFilterData[15] = new PropertyValue();
                pdfFilterData[15].Name = "Magnification";
                pdfFilterData[15].Value = new Integer(4);

                // Specifies the zoom level a PDF document is opened with.
                // Only valid if the property "Magnification" is set to 4
                pdfFilterData[16] = new PropertyValue();
                pdfFilterData[16].Name = "Zoom";
                pdfFilterData[16].Value = new Integer(120);

                // Specifies that automatically inserted empty pages are
                // suppressed. This option only applies for storing Writer
                // documents.
                pdfFilterData[17] = new PropertyValue();
                pdfFilterData[17].Name = "IsSkipEmptyPages";
                pdfFilterData[17].Value = new Boolean(true);

                // Whether to use PDF/A-1, set the integer to zero if not.
                // Requires 2.4 or later.
                pdfFilterData[18] = new PropertyValue();
                pdfFilterData[18].Name = "SelectPdfVersion";
                pdfFilterData[18].Value = new Integer(1);
                
                PropertyValue[] conversionProperties = new PropertyValue[3];
                conversionProperties[0] = new PropertyValue();
                conversionProperties[0].Name = "FilterName";
                conversionProperties[0].Value = "writer_pdf_Export";
                conversionProperties[1] = new PropertyValue();
                conversionProperties[1].Name = "Overwrite ";
                conversionProperties[1].Value = new Boolean(true);
                conversionProperties[2] = new PropertyValue();
                conversionProperties[2].Name = "FilterData";
                conversionProperties[2].Value = pdfFilterData;

                XStorable xstorable = (XStorable) UnoRuntime.queryInterface(XStorable.class,objectDocumentToStore);
                xstorable.storeToURL(storeUrl,conversionProperties);
            }
            catch (java.lang.Exception e) {
                e.printStackTrace();
            }
            finally {
                System.exit(0);
            }
        }   
    }
