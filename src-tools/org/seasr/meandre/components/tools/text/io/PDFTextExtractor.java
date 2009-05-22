package org.seasr.meandre.components.tools.text.io;

import java.net.URI;
import java.util.logging.Logger;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.Licenses;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.parsers.DataTypeParser;
import org.seasr.meandre.support.text.PDFUtils;

/** This class provides methods related to text extraction from PDF files
 *
 * @author Boris Capitanu
 * @author Xavier Llor&agrave;
 * @author Loretta Auvil
 *
 */

@Component(creator = "Loretta Auvil",
           description = "This component extracts the text from a pdf document. "+
                         "The input is a String or URL specifiying the url of the pdf document. "+
                         "The output is the extracted text.",
           name = "PDF Text Extractor",
           rights = Licenses.UofINCSA,
           tags = "URL, text, pdf",
           dependency = {"protobuf-java-2.0.3.jar", "jPod.jar", "iscwt.jar", "isrt.jar", "jbig2.jar"},
           baseURL = "meandre://seasr.org/components/")

public class PDFTextExtractor extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            description = "The URL of the PDF file." +
                          "<br>TYPE: String, URL",
            name = Names.PORT_LOCATION
    )
    protected static final String IN_PDF_URL = Names.PORT_LOCATION;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            description = "The text extracted from the PDF file." +
                          "<br>TYPE: Text",
            name = Names.PORT_TEXT
    )
    protected static final String OUT_TEXT = Names.PORT_TEXT;

    //--------------------------------------------------------------------------------------------


    private Logger _console;


    //--------------------------------------------------------------------------------------------

    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _console = getConsoleLogger();
    }

    public void executeCallBack(ComponentContext cc) throws Exception {
        URI uri = DataTypeParser.parseAsURI(cc.getDataComponentFromInput(IN_PDF_URL));
        _console.fine("Processing PDF document: " + uri);

        String text = PDFUtils.extractText(uri.toURL());

        cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(text));
    }

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
