package org.seasr.meandre.components.tools;

/** This abstract class just provide a list of standardized port and property names.
 *
 * @author Xavier Llor&agrave;
 *
 */
public abstract class Names {

    public static final String PROP_ERROR_HANDLING = "error_handling";
    public static final String PROP_BASE_URI = "base_uri";
    public static final String PROP_RDF_DIALECT = "rdf_dialect";
    public static final String PROP_MESSAGE = "message";
    public static final String PROP_TIMES = "times";
    public static final String PROP_WRAP_STREAM = "wrap_stream";
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
	public static final String PROP_ID = "id";
	public static final String PROP_CSS = "css";
	public static final String PROP_TEMPLATE = "template";

	public static final String PORT_LOCATION = "location";
	public static final String PORT_DOCUMENT = "document";
	public static final String PORT_TEXT = "text";
	public static final String PORT_TOKENS = "tokens";
	public static final String PORT_TOKEN_BLACKLIST = "tokens_blacklist";
	public static final String PORT_TOKEN_COUNTS = "token_counts";
	public static final String PORT_TOKENIZED_SENTENCES = "tokenized_sentences";
	public static final String PORT_SENTENCES = "sentences";
	public static final String PORT_OBJECT = "object";
	public static final String PORT_XML = "xml";
	public static final String PORT_TOKEN_MAP = "token_map";
	public static final String PORT_JAVA_STRING = "java_string";
	public static final String PORT_RAW_DATA = "raw_data";
	public static final String PORT_HTML = "html";
}
