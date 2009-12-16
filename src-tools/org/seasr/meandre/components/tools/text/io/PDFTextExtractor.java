package org.seasr.meandre.components.tools.text.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.Licenses;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Bytes;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
import org.seasr.meandre.support.generic.io.StreamUtils;
import org.seasr.meandre.support.generic.text.PDFUtils;

/**
 * This class provides methods related to text extraction from PDF files
 *
 * @author Boris Capitanu
 * @author Xavier Llor&agrave;
 * @author Loretta Auvil
 *
 */

@Component(
        creator = "Loretta Auvil",
        description = "This component extracts the text from a pdf document. "+
                      "The input is a String or URL specifiying the url of the pdf document. "+
                      "The output is the extracted text.",
        name = "PDF Text Extractor",
        rights = Licenses.UofINCSA,
        tags = "URL, text, pdf",
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar", "jPod.jar", "iscwt.jar", "isrt.jar", "jbig2.jar"}
)
public class PDFTextExtractor extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_LOCATION,
            description = "The location of the PDF file, or the raw byte content" +
                "<br>TYPE: java.net.URI" +
                "<br>TYPE: java.net.URL" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes"
    )
    protected static final String IN_PDF_LOCATION = Names.PORT_LOCATION;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TEXT,
            description = "The text extracted from the PDF file" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_TEXT = Names.PORT_TEXT;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Object input = cc.getDataComponentFromInput(IN_PDF_LOCATION);

        InputStream pdfStream;
        String pdfName;

        if (input instanceof byte[] || input instanceof Bytes) {
            byte[] pdfData = (input instanceof Bytes) ?
                    BasicDataTypesTools.bytestoByteArray((Bytes)input) :
                    (byte[])input;

            pdfStream = new ByteArrayInputStream(pdfData);
            pdfName = "raw_data";
        } else {
            URL pdfURL = StreamUtils.getURLforResource(
                DataTypeParser.parseAsURI(input));

            console.fine("Processing PDF document: " + pdfURL);

            URLConnection connection = pdfURL.openConnection();
            if (!connection.getContentType().equalsIgnoreCase("application/pdf"))
                console.warning(String.format("%s does not appear to be a PDF document", pdfURL.toString()));

            pdfStream = connection.getInputStream();
            pdfName = pdfURL.toString();
        }

        String text = PDFUtils.extractText(
                PDFUtils.getDocument(pdfStream, pdfName, null));

        cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(text));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
