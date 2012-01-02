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

package org.seasr.meandre.components.tools.tuples;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
import org.seasr.meandre.support.generic.io.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
		description = "Converts tuples into an XML structure. " +
				"Each tuple is represented by a node whose attributes are the tuple fields.",
        name = "Tuple To XML",
        tags = "xml, tuples",
        rights = Licenses.UofINCSA,
        baseURL="meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TupleToXML extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TUPLES,
            description = "The tuples" +
                    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String IN_TUPLES = Names.PORT_TUPLES;

    @ComponentInput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for the tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
	        name = Names.PORT_XML,
	        description = "The XML document" +
	                "<br>TYPE: org.w3c.dom.Document"
	)
	protected static final String OUT_XML = Names.PORT_XML;

    //--------------------------------------------------------------------------------------------


    //--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Strings inMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
        SimpleTuplePeer inPeer  = new SimpleTuplePeer(inMeta);

        Object input = cc.getDataComponentFromInput(IN_TUPLES);
        Strings[] tuples;

        if (input instanceof StringsArray)
            tuples = BasicDataTypesTools.stringsArrayToJavaArray((StringsArray) input);

        else

        if (input instanceof Strings) {
            Strings inTuple = (Strings) input;
            tuples = new Strings[] { inTuple };
        }

        else
            throw new ComponentExecutionException("Don't know how to handle input of type: " + input.getClass().getName());

	    Document xml = DOMUtils.createNewDocument();
	    Element root = xml.createElement("tuples");
	    xml.appendChild(root);

		for (Strings t : tuples) {
		    SimpleTuple tuple = inPeer.createTuple();
		    tuple.setValues(t);

		    Element xmlTuple = xml.createElement("tuple");
		    for (String fieldName : inPeer.getFieldNames())
		        xmlTuple.setAttribute(fieldName, tuple.getValue(fieldName));

		    root.appendChild(xmlTuple);
		}

		cc.pushDataComponentToOutput(OUT_XML, xml);
	}

	@Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
