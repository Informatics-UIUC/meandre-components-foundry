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

package org.seasr.meandre.components.rdf.zotero;

import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.generic.zotero.ZoteroUtils;

import com.hp.hpl.jena.rdf.model.Model;

/**
 *  This class extracts the list of authors per entry from a Zotero RDF
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 */

@Component(
        creator = "Xavier Llor&agrave",
        description = "Extract the authors for each entry of a Zotero RDF",
        name = "Zotero Author Extractor",
        tags = "#TRANSFORM, zotero, author, information extraction",
        rights = Licenses.UofINCSA,
        mode = Mode.compute,
        firingPolicy = FiringPolicy.all,
        baseURL = "meandre://seasr.org/components/foundry/"
)
public class ZoteroAuthorExtractor extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = "rdf",
            description = "The Zotero RDF data " +
                "<br>TYPE: com.hp.hpl.jena.rdf.model.Model" +
                "<br>TYPE: org.seasr.datatypes.core.BasicDataTypes.Bytes" +
                "<br>TYPE: org.seasr.datatypes.core.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: java.net.URL" +
                "<br>TYPE: java.net.URI"
    )
    protected static final String IN_RDF = "rdf";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_AUTHOR_LIST,
            description = "A list of vectors containing the names of the authors. There is one vector for" +
                          "Zotero entry" +
                          "<br>TYPE: java.util.List<java.util.Vector<java.lang.String>>"
    )
    protected static final String OUT_AUTHOR_LIST = Names.PORT_AUTHOR_LIST;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Model model = DataTypeParser.parseAsModel(cc.getDataComponentFromInput(IN_RDF));
        List<Vector<String>> authorList = ZoteroUtils.extractAuthors(model);

        if (console.isLoggable(Level.FINE)) {
            for (Vector<String> authors : authorList) {
                for (String author : authors)
                    console.fine("author: " + author);
            }
        }

        if (authorList.isEmpty()) {
            outputError("No authors found for the items selected", Level.WARNING);
            return;
        }

        cc.pushDataComponentToOutput(OUT_AUTHOR_LIST, authorList);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
