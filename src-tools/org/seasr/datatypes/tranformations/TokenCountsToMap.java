/**
 * 
 */
package org.seasr.datatypes.tranformations;

import java.util.Map;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.IntegersMap;
import org.seasr.meandre.components.tools.Names;


/** This component transforms a token count into a map.
 * 
 * @author Xavier Llorˆ
 *
 */
@Component(
		name = "Token counts to map",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		dependency = {"protobuf-java-2.0.3.jar"},
		tags = "tools, tokenizer, counting, transformations",
		description = "Given a collection of token counts, this component converts them " +
				      "to a Java map."
)
public class TokenCountsToMap {
	
	//--------------------------------------------------------------------------------------------
	
	//--------------------------------------------------------------------------------------------
		
	@ComponentInput(
			name = Names.PORT_TOKEN_COUNTS,
			description = "The token counts to convert to text"
		)
	private final static String INPUT_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;
	
	@ComponentOutput(
			name = Names.PORT_TOKEN_MAP,
			description = "The converted token map"
		)
	private final static String OUPUT_TOKEN_MAP = Names.PORT_TOKEN_MAP;
		
	//--------------------------------------------------------------------------------------------
	
	//--------------------------------------------------------------------------------------------
	
	/**
	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
	 */
	public void execute(ComponentContext cc)
			throws ComponentExecutionException, ComponentContextException {
		Object obj = cc.getDataComponentFromInput(INPUT_TOKEN_COUNTS);
		if ( obj instanceof StreamDelimiter ) 
			cc.pushDataComponentToOutput(OUPUT_TOKEN_MAP, obj);
		else {
			try {
				IntegersMap im = (IntegersMap)obj;
				Map<String, Integer> map = BasicDataTypesTools.IntegerMapToMap(im);
				cc.pushDataComponentToOutput(OUPUT_TOKEN_MAP, map);
			} catch (ClassCastException e ) {
				String sMessage = "Input data is not a sequence of token countss";
				cc.getLogger().warning(sMessage);
				cc.getOutputConsole().println("WARNING: "+sMessage);
				throw new ComponentExecutionException(e);
			}
		}
	}

	//--------------------------------------------------------------------------------------------

}
