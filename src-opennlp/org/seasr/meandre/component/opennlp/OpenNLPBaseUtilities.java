package org.seasr.meandre.component.opennlp;

import java.io.File;

import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.meandre.components.tools.ModelVocabulary;
import org.seasr.meandre.components.tools.Names;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;

/** Provides basic utilities for the OpenNLP components.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public abstract class OpenNLPBaseUtilities {

	//--------------------------------------------------------------------------------------------

	@ComponentProperty(
			name = Names.PROP_ERROR_HANDLING,
			description = "If set to true errors will be handled and they will be reported to the screen ." +
					      "Otherwise, the component will throw an exception an force the flow to abort. ",
		    defaultValue = "true" 
		)
	protected final static String PROP_ERROR_HANDLING = Names.PROP_ERROR_HANDLING;

	@ComponentProperty(
			name = Names.PROP_LANGUAGE,
			description = "The language to use in the tokenizer. ",
		    defaultValue = "english" 
		)
	protected final static String PROP_LANGUAGE = Names.PROP_LANGUAGE;
	
	//--------------------------------------------------------------------------------------------

	/** The error handling flag */
	protected boolean bErrorHandling;
	
	/** The language of the text being processed */
	protected String sLanguage;
	
	//--------------------------------------------------------------------------------------------
	
	/**
	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
	 */
	public void initialize(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		this.sLanguage = ccp.getProperty(PROP_LANGUAGE).trim().toLowerCase();
		this.bErrorHandling = Boolean.parseBoolean(ccp.getProperty(PROP_ERROR_HANDLING));
		String sRunFile = ccp.getRunDirectory()+File.separator+"opennlp"+File.separator+"models";
		boolean bRes = ModelInstaller.installJar(sRunFile, "opennlp-"+sLanguage+"-models.jar", false);
		if ( !this.bErrorHandling && !bRes )
			throw new ComponentExecutionException("Failed to install OpenNLP models at "+new File(sRunFile).getAbsolutePath());
	}

	/**
	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
	 */
	public void dispose(ComponentContextProperties ccp)
			throws ComponentExecutionException, ComponentContextException {
		this.sLanguage = null;
		this.bErrorHandling = false;	
	}


	//--------------------------------------------------------------------------------------------


	/** Extract the text of a model.
	 * 
	 * @param model The model to use
	 * @return The text contained in the model
	 */
	protected String extractTextFromModel(Model model) {
		StringBuffer sbBuffer = new StringBuffer();
		NodeIterator modelObjects = model.listObjectsOfProperty(ModelVocabulary.text);
		while ( modelObjects.hasNext() ) {
			Literal node = (Literal)modelObjects.nextNode();
			sbBuffer.append(node.getValue().toString());
			sbBuffer.append(" ");
		}
		return sbBuffer.toString();
	}

}
