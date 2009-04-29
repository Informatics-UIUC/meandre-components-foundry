/**
 * 
 */
package org.seasr.meandre.components.tools.semantic;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.meandre.components.tools.ModelVocabulary;
import org.seasr.meandre.components.tools.Names;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/** A base class for JSTOR extractors
 * 
 * @author Xavier Llor&agrave
 *
 */
@Component(
		name = "Wrap text into model",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		dependency = {"protobuf-java-2.0.3.jar"},
		tags = "semantic, tools, text",
		description = "Give the text provided in the input, this component generates a " +
				      "new semantic wrapper model containing the inputed text."
)
public class WrapTextIntoModel implements ExecutableComponent {

	//--------------------------------------------------------------------------------------------
			
	//--------------------------------------------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TEXT,
			description = "The text to be wrapped"
		)
	private final static String INPUT_TEXT = Names.PORT_TEXT;
	
	@ComponentOutput(
			name = Names.PORT_DOCUMENT,
			description = "The model containing the semantic document containing the page text"
		)
	private final static String OUTPUT_DOCUMENT = Names.PORT_DOCUMENT;
	
	//--------------------------------------------------------------------------------------------
	
	//--------------------------------------------------------------------------------------------
	
	/**
	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
	 */
	public void initialize(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
	}
	
	/**
	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
	 */
	public void dispose(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
	}

	/**
	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
	 */
	public void execute(ComponentContext cc)
			throws ComponentExecutionException, ComponentContextException {

		Object obj = cc.getDataComponentFromInput(INPUT_TEXT);
		if ( obj instanceof StreamDelimiter )
			cc.pushDataComponentToOutput(OUTPUT_DOCUMENT, obj);
		else {
			Model model = ModelFactory.createDefaultModel();
			String sText = "";
			if (obj instanceof Strings) {
				for ( String s:((Strings)obj).getValueList() )
					sText += s+" ";
			}
			else
				sText = obj.toString();
			model.add(
				model.createStatement(
					model.createResource(), 
					ModelVocabulary.text, 
					model.createTypedLiteral(sText))
			);	
			cc.pushDataComponentToOutput(OUTPUT_DOCUMENT, model);	
		}	
	}


	//-----------------------------------------------------------------------------------
	


}
