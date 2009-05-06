package org.seasr.meandre.components.tools;

/** This abstract class just provide a list of standardized port and property names.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public abstract class Names {

	public final static String PROP_ERROR_HANDLING = "error_handling";
	public final static String PROP_BASE_URI = "base_uri";
	public final static String PROP_RDF_DIALECT = "rdf_dialect";
	public final static String PROP_MESSAGE = "message";
	public final static String PROP_TIMES = "times";
	public final static String PROP_WRAP_STREAM = "wrap_stream";
	public static final String PROP_ENCODING = "encoding";
	public static final String PROP_LANGUAGE = "language";
	public static final String PROP_ORDERED = "ordered";
	public static final String PROP_EXPRESSION = "expression";
	public static final String PROP_RECURSIVE = "recursive";
	public static final String PROP_HEADER = "header";
	public static final String PROP_OFFSET = "offset";
	public static final String PROP_COUNT = "count";
	public static final String PROP_REPLACE = "replace";
	public static final String PROP_N_TOP_SENTENCES = "n_top_sentences";
	public static final String PROP_N_TOP_TOKENS = "n_top_tokens";
	public static final String PROP_ITERATIONS = "iterations";
	
	public final static String PORT_LOCATION = "location";
	public final static String PORT_DOCUMENT = "document";
	public final static String PORT_TEXT = "text";
	public final static String PORT_TOKENS = "tokens";
	public final static String PORT_TOKEN_BLACKLIST = "tokens_blacklist";
	public final static String PORT_TOKEN_COUNTS = "token_counts";
	public static final String PORT_TOKENIZED_SENTENCES = "tokenized_sentences";
	public final static String PORT_SENTENCES = "sentences";
	public final static String PORT_OBJECT = "object";
	public final static String PORT_XML = "xml";
	public static final String PORT_TOKEN_MAP = "token_map";
}
