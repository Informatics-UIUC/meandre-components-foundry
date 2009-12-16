/**
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * All rights reserved.
 *
 * Developed by:
 *
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */


package org.seasr.meandre.components.transform.text;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;

@Component(
        creator = "Lily Dong",
        description = "Removes characters or replaces characters with replacement.",
        name = "Text Cleaner",
        tags = "text, remove, replace",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)

public class TextCleaner extends AbstractExecutableComponent{
	//------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            description = "The text to clean or replace",
            name = Names.PORT_TEXT
    )
    protected static final String IN_TEXT = Names.PORT_TEXT;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            description = "The cleaned or replaced text",
            name = Names.PORT_TEXT
    )
    protected static final String OUT_TEXT = Names.PORT_TEXT;

    //------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
			description = "The characters to remove or replace. " +
			"If the replacement leaves blank, the characters are cleaned out.",
			name=Names.PROP_FIND,
			defaultValue = ""
	)
	protected static final String PROP_FIND = Names.PROP_FIND;

	@ComponentProperty(
	        description = "The character to use for replacement(default is space).",
            name = Names.PROP_REPLACE,
            defaultValue = " "
	)
	protected static final String PROP_REPLACE = Names.PROP_REPLACE;


	//--------------------------------------------------------------------------------------------

	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	}

	public void executeCallBack(ComponentContext cc) throws Exception {
		String theFind = cc.getProperty(PROP_FIND);
		String theReplace = cc.getProperty(PROP_REPLACE);

		 for (String text : DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT))) {
			text = text.replaceAll("\t|\r|\n", " ");
			text = text.replaceAll(theFind, theReplace);
			cc.pushDataComponentToOutput(
					OUT_TEXT,
					BasicDataTypesTools.stringToStrings(text));
		 }
	}

	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	}
}
