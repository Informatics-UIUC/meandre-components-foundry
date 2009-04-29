package org.seasr.meandre.components.tools.basic;

import org.meandre.annotations.Component;
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
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.components.tools.Names;

/** Pushes a property value to the output
 * 
 * @author Xavier Llor&agrave
 *
 */
@Component(
		name = "Push text",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "io, string",
		dependency = {"protobuf-java-2.0.3.jar"},
		description = "Pushes the value of the text message property to the output. It provides " +
				      "a couple of properties to control how many times it needs to be pushed, " +
				      "and if it needs to be wrapped with terminators "
)
public class PushText implements ExecutableComponent{

	//--------------------------------------------------------------------------------------------

	@ComponentProperty(
			name = Names.PROP_MESSAGE,
			description = "The text message to push. ",
		    defaultValue = "Hello World!" 
		)
	private final static String PROP_MESSAGE = Names.PROP_MESSAGE;
	
	@ComponentProperty(
			name = Names.PROP_TIMES,
			description = "The number of times to push the message. ",
		    defaultValue = "1" 
		)
	private final static String PROP_TIMES = Names.PROP_TIMES;
	
	@ComponentProperty(
			name = Names.PROP_WRAP_STREAM,
			description = "Should the pushed message be wrapped as a stream. ",
		    defaultValue = "false" 
		)
	private final static String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;

	//--------------------------------------------------------------------------------------------
	
	@ComponentOutput(
			name = Names.PORT_TEXT,
			description = "The text message being pushed"
		)
	private final static String OUTPUT_TEXT = Names.PORT_TEXT;
	
	//--------------------------------------------------------------------------------------------
	
	/** The message */
	private String sMessage;

	/** The number of times to push the string */
	private long lTimes;
	
	/** Should be wrapped */
	private boolean bWrapped;

	//--------------------------------------------------------------------------------------------
		
	/**
	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
	 */
	public void initialize(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		sMessage = ccp.getProperty(PROP_MESSAGE);
		lTimes = Long.parseLong(ccp.getProperty(PROP_TIMES));
		bWrapped = Boolean.parseBoolean(ccp.getProperty(PROP_WRAP_STREAM));
	}
	
	/**
	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
	 */
	public void dispose(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		sMessage = null;
		lTimes = 0;
		bWrapped = false;
	}

	/**
	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
	 */
	public void execute(ComponentContext cc)
			throws ComponentExecutionException, ComponentContextException {
		
		if ( bWrapped ) 
			pushInitiator(cc);
		for ( long l=0 ; l<lTimes ; l++ )
			cc.pushDataComponentToOutput(OUTPUT_TEXT, BasicDataTypesTools.stringToStrings(sMessage));
		if ( bWrapped ) 
			pushTerminator(cc);
	}


	//-----------------------------------------------------------------------------------

	/** Pushes an initiator.
	 * 
	 * @param cc The component context
	 * @throws ComponentContextException Something went wrong when pushing
	 */
	private void pushInitiator(ComponentContext cc) throws ComponentContextException {
		StreamInitiator si = new StreamInitiator();
		si.put(PROP_TIMES, lTimes);
		cc.pushDataComponentToOutput(OUTPUT_TEXT,si);		
	}

	/** Pushes a terminator.
	 * 
	 * @param cc The component context
	 * @throws ComponentContextException Something went wrong when pushing
	 */
	private void pushTerminator(ComponentContext cc) throws ComponentContextException {
		StreamTerminator st = new StreamTerminator();
		st.put(PROP_TIMES, lTimes);
		cc.pushDataComponentToOutput(OUTPUT_TEXT,st);
	}
	
}
