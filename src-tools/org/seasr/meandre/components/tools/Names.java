/**
*
* University of Illinois/NCSA
* Open Source License
*
* Copyright (c) 2008, NCSA.  All rights reserved.
*
* Developed by:
* The Automated Learning Group
* University of Illinois at Urbana-Champaign
* http://www.seasr.org
*
* Permission is hereby granted, free of charge, to any person obtaining
* a copy of this software and associated documentation files (the
* "Software"), to deal with the Software without restriction, including
* without limitation the rights to use, copy, modify, merge, publish,
* distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject
* to the following conditions:
*
* Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimers.
*
* Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimers in
* the documentation and/or other materials provided with the distribution.
*
* Neither the names of The Automated Learning Group, University of
* Illinois at Urbana-Champaign, nor the names of its contributors may
* be used to endorse or promote products derived from this Software
* without specific prior written permission.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
* CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
*
*/

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
	public static final String PROP_WIDTH = "width";
	public static final String PROP_HEIGHT = "height";
	public static final String PROP_FONT_NAME = "font_name";
	public static final String PROP_MAX_SIZE = "max_size";
	public static final String PROP_MIN_SIZE = "min_size";
	public static final String PROP_SHOW_COUNT = "show_count";

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
