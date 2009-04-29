/**
 * 
 */
package org.seasr.meandre.components.tools;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/** This class just provide a convenience place for JSTOR RDF vocabulary
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class ModelVocabulary {

	 /** The RDF model that holds the vocabulary terms */
    private static Model m_model = ModelFactory.createDefaultModel();

    /** The namespace of the vocabulary as a string  */
    public static final String NS = "meandre://meandre.org/components/vocabulary#";

    /** The namespace of the vocabulary as a string */
    public static String getURI() {
          return NS;
    }

    /** The namespace of the vocabulary as a resource */
    public static final Resource NAMESPACE = m_model.createResource(NS);

    /** Text property used for having the text. */
    public static final Property text = m_model.createProperty(NS+"text");
    
    /** Text property used for creating keys. */
    public static final Property key = m_model.createProperty(NS+"key");
    
    /** Text property used for creating values associated to a key. */   
    public static final Property value = m_model.createProperty(NS+"value");

    /** Used to annotate sequences of tokens. */
    public static final Resource tokens = m_model.createResource(NS+"tokens");
    
    /** Used to annotate sequences of token counts. */
    public static final Resource token_counts = m_model.createResource(NS+"token_counts");
    
    /** Used to annotate sequences of sentences. */
    public static final Resource sentences = m_model.createResource(NS+"sentences");

}
