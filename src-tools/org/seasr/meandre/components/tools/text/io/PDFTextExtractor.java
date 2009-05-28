package org.seasr.meandre.components.tools.text.io;

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
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.io.StreamUtils;
import org.seasr.meandre.support.parsers.DataTypeParser;
import org.seasr.meandre.support.text.PDFUtils;

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
        baseURL = "meandre://seasr.org/components/tools/",
        dependency = {"protobuf-java-2.0.3.jar", "jPod.jar", "iscwt.jar", "isrt.jar", "jbig2.jar"}
)
public class PDFTextExtractor extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            description = "The location of the PDF file",
            name = Names.PORT_LOCATION
    )
    protected static final String IN_PDF_LOCATION = Names.PORT_LOCATION;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            description = "The text extracted from the PDF file",
            name = Names.PORT_TEXT
    )
    protected static final String OUT_TEXT = Names.PORT_TEXT;


    //--------------------------------------------------------------------------------------------

    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    public void executeCallBack(ComponentContext cc) throws Exception {
        URL pdfURL = StreamUtils.getURLforResource(
            DataTypeParser.parseAsURI(cc.getDataComponentFromInput(IN_PDF_LOCATION)));

        console.fine("Processing PDF document: " + pdfURL);

        URLConnection connection = pdfURL.openConnection();
        if (!connection.getContentType().equalsIgnoreCase("application/pdf"))
            console.warning(String.format("%s does not appear to be a PDF document", pdfURL.toString()));

        String text = PDFUtils.extractText(
                PDFUtils.getDocument(connection.getInputStream(), pdfURL.toString(), null));

        cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(text));
    }

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
