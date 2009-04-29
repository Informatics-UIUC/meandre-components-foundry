/**
 * 
 */
package org.seasr.meandre.components.tools.basic;

import java.io.PrintStream;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
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
import org.seasr.meandre.components.tools.Names;

/** Prints a Jena Model to the console
 * 
 * @author Xavier Llor&agrave
 *
 */
@Component(
		name = "Print to console",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "io, print, console",
		dependency = {"protobuf-java-2.0.3.jar"},
		description = "This component takes the input and prints it to the console. "
)
public class PrintToConsole implements ExecutableComponent {

	//--------------------------------------------------------------------------------------------	
	
	@ComponentProperty(
			name = Names.PROP_WRAP_STREAM,
			description = "Should the printed object be wrapped as a stream? ",
		    defaultValue = "false" 
		)
	private final static String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;

	//--------------------------------------------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_OBJECT,
			description = "The object to print"
		)
	private final static String INPUT_OBJECT = Names.PORT_OBJECT;
	
	@ComponentOutput(
			name = Names.PORT_OBJECT,
			description = "The objet being printed"
		)
	private final static String OUTPUT_OBJECT = Names.PORT_OBJECT;
	
	//--------------------------------------------------------------------------------------------

	/** Should be wrapped */
	private boolean bWrapped;
	
	//--------------------------------------------------------------------------------------------
	
	/**
	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
	 */
	public void initialize(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		bWrapped = Boolean.parseBoolean(ccp.getProperty(PROP_WRAP_STREAM));
	}
	
	/**
	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
	 */
	public void dispose(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		bWrapped = false;
	}

	/**
	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
	 */
	public void execute(ComponentContext cc)
			throws ComponentExecutionException, ComponentContextException {

		Object obj = cc.getDataComponentFromInput(INPUT_OBJECT);
		if ( bWrapped && obj instanceof StreamDelimiter ) {
					printStreamDelimiter(cc, obj);
		}
		else {
			PrintStream con = cc.getOutputConsole();
			if ( obj instanceof Strings )
				for ( String s:((Strings)obj).getValueList())
					con.println(s);
			else
				con.println(obj.toString());
		}
		cc.pushDataComponentToOutput(OUTPUT_OBJECT, obj);		
	}

	/** Prints a stream delimiter
	 * 
	 * @param cc The component context
	 * @param obj The delimiter to print
	 */
	private void printStreamDelimiter(ComponentContext cc, Object obj) {
		StringBuffer sb = new StringBuffer();
		String [] saName = obj.getClass().getName().split("\\"+".");
		sb.append(saName[saName.length-1]+" [ ");
		StreamDelimiter sd = (StreamDelimiter) obj;
		for ( String sKey:sd.keySet())
			sb.append("("+sKey+"="+sd.get(sKey)+") ");
		sb.append("]");				
		cc.getOutputConsole().println(sb);
	}

	//-----------------------------------------------------------------------------------
	

}
