/**
 * 
 */
package org.seasr.meandre.components.analytics.text.io.console;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.meandre.components.tools.Names;


/** Converts sentences in to text.
 * 
 * @author Xavier Llorˆ
 *
 */
@Component(
		name = "Sentences to text",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		dependency = {"protobuf-java-2.0.3.jar"},
		tags = "semantic, tools, text, tokenizer, counting",
		description = "Given a collection of sentences, this component converts it " +
				      "into text."
)
public class SentencesToText 
extends AnalysisToText {
	
	//--------------------------------------------------------------------------------------------

	@ComponentProperty(
			name = Names.PROP_MESSAGE,
			description = "The header to use. ",
		    defaultValue = "Available sentences" 
		)
	final static String PROP_MESSAGE = Names.PROP_MESSAGE;
	
	//--------------------------------------------------------------------------------------------
		
	@ComponentInput(
			name = Names.PORT_SENTENCES,
			description = "The senntences to convert to text"
		)
	private final static String INPUT_SENTENCES = Names.PORT_SENTENCES;
	
	//--------------------------------------------------------------------------------------------
	
	//--------------------------------------------------------------------------------------------

	/**
	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
	 */
	public void execute(ComponentContext cc)
			throws ComponentExecutionException, ComponentContextException {
		Object obj = cc.getDataComponentFromInput(INPUT_SENTENCES);
		if ( obj instanceof StreamDelimiter ) 
			cc.pushDataComponentToOutput(OUTPUT_TEXT, obj);
		else {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			try {
				Strings str = (Strings)obj;
				printStrings(ps, str, this.iCount, this.iOffset);
			} catch (ClassCastException e ) {
				String sMessage = "Input data is not a sequence of sentences";
				cc.getLogger().warning(sMessage);
				cc.getOutputConsole().println("WARNING: "+sMessage);
				if ( !bErrorHandling ) 
					throw new ComponentExecutionException(e);
			}
			
			cc.pushDataComponentToOutput(OUTPUT_TEXT, BasicDataTypesTools.stringToStrings(baos.toString()));
		}
	}

	
	
	//--------------------------------------------------------------------------------------------

}
