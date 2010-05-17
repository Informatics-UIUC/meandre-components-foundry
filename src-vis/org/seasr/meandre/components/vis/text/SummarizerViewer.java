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

package org.seasr.meandre.components.vis.text;

import java.util.ArrayList;

import org.apache.velocity.VelocityContext;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.generic.html.VelocityTemplateService;

/**
 * Visualize tokens and sentences.
 *
 * @author Lily Dong
 *
 */

@Component(
        name = "Summarizer Viewer",
        creator = "Lily Dong",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.webui,
        rights = Licenses.UofINCSA,
        tags = "token, sentence, viewer",
        description = "Visualizes tokens and sentences from HitsSummarizer. " +
        "Its inputs should be connected directly to HitsSummarizer.",
        dependency = {"protobuf-java-2.2.0.jar"},
        resources = {"SummarizerViewer.vm"}
)

public class SummarizerViewer extends AbstractExecutableComponent {
	//------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TOKENS,
            description = "Tokens" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_TOKENS = Names.PORT_TOKENS;

    @ComponentInput(
            name = Names.PORT_SENTENCES,
            description = "Sentences" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_SENTENCES = Names.PORT_SENTENCES;

    //-------------------------- OUTPUTS --------------------------

	@ComponentOutput(
	        name = Names.PORT_HTML,
	        description = "The HTML for the summarizer viewer" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_HTML = Names.PORT_HTML;

    //--------------------------------------------------------------------------------------------

    private static final String DEFAULT_TEMPLATE = "org/seasr/meandre/components/vis/text/SummarizerViewer.vm";
    private static final VelocityTemplateService velocity = VelocityTemplateService.getInstance();

    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        String[] inputs1 = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TOKENS));
        String[] inputs2 = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_SENTENCES));

        ArrayList<String> tokens = new ArrayList<String>(),
        				  sentences = new ArrayList<String>();

        for(String input: inputs1) {
        	input = input.replaceAll("\t|\r|\n", " ");
        	tokens.add(input);
        }

        for(String input: inputs2) {
        	input = input.replaceAll("\t|\r|\n", " ");
        	/* " inside sentences are not allowed in javascript */
        	input = input.replaceAll("\"", "'");
        	sentences.add(input);
        }

        VelocityContext context = velocity.getNewContext();
        context.put("tokens_list", tokens);
        context.put("sentences_list", sentences);

        VelocityTemplateService velocity = VelocityTemplateService.getInstance();
        String html = velocity.generateOutput(context, DEFAULT_TEMPLATE);

        cc.pushDataComponentToOutput(OUT_HTML, BasicDataTypesTools.stringToStrings(html));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    @Override
    protected void handleStreamInitiators() throws Exception {
        StreamInitiator si = (StreamInitiator)componentContext.getDataComponentFromInput(IN_TOKENS);
        componentContext.pushDataComponentToOutput(OUT_HTML, si);
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        StreamTerminator st = (StreamTerminator)componentContext.getDataComponentFromInput(IN_TOKENS);
        componentContext.pushDataComponentToOutput(OUT_HTML, st);
    }
}
