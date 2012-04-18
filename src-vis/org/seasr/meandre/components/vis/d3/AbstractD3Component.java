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

package org.seasr.meandre.components.vis.d3;

import java.io.File;

import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.seasr.meandre.components.abstracts.util.ComponentUtils;
import org.seasr.meandre.components.vis.html.VelocityTemplateToHTML;
import org.seasr.meandre.support.generic.io.JARInstaller.InstallStatus;

/**
 *
 * @author Boris Capitanu
 *
 * NOTE: If a component extending this class is used in a flow for the Zotero environment do not forget to
 *       set the 'd3_api_url' property to http://mbostock.github.com/d3/d3.js
 */
public abstract class AbstractD3Component extends VelocityTemplateToHTML {

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = "d3_api_url",
            description = "The URL to the D3 API, or leave empty to use the embedded one",
            defaultValue = ""
    )
    protected static final String PROP_D3_API_URL = "d3_api_url";

    //--------------------------------------------------------------------------------------------


    protected static final String D3_API_PATH = "d3-api"; // this path is assumed to be appended to the published_resources location
    protected static final String D3_JS = "d3.js";


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        String d3API = getPropertyOrDieTrying(PROP_D3_API_URL, true, false, ccp);
        if (d3API.length() == 0) {
            String d3APIDir = ccp.getPublicResourcesDirectory() + File.separator + D3_API_PATH;
            InstallStatus status = ComponentUtils.installJARContainingResource(getClass(), D3_JS, d3APIDir, false);
            switch (status) {
                case SKIPPED:
                    console.fine(String.format("Installation skipped - %s is already installed", D3_JS));
                    break;

                case FAILED:
                    throw new ComponentContextException(String.format("Failed to install %s at %s",
                            D3_JS, new File(d3APIDir).getAbsolutePath()));
            }

            d3API = "/public/resources/" + D3_API_PATH.replaceAll("\\\\", "/") + "/" + D3_JS;
        }

        console.fine("Using D3 API from: " + d3API);
        context.put("d3API", d3API);
    }
}
