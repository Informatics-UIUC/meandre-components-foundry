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

package org.seasr.meandre.components.vis.protovis;

import java.io.File;

import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.seasr.meandre.components.abstracts.util.ComponentUtils;
import org.seasr.meandre.components.vis.html.StreamingVelocityTemplateToHTML;
import org.seasr.meandre.support.generic.io.JARInstaller.InstallStatus;

/**
 *
 * @author Boris Capitanu
 *
 * NOTE: If a component extending this class is used in a flow for the Zotero environment do not forget to
 *       set the 'protovis_api_url' property to http://vis.stanford.edu/protovis/protovis-r3.2.js
 */
public abstract class AbstractStreamingProtovisComponent extends StreamingVelocityTemplateToHTML {

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = "protovis_api_url",
            description = "The URL to the Protovis API, or leave empty to use the embedded one",
            defaultValue = ""
    )
    protected static final String PROP_PROTOVIS_API_URL = "protovis_api_url";

    //--------------------------------------------------------------------------------------------


    protected static final String PROTOVIS_API_PATH = "protovis-api"; // this path is assumed to be appended to the published_resources location
    protected static final String PROTOVIS_JS = "protovis-r3.2.js";


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        String protovisAPI = getPropertyOrDieTrying(PROP_PROTOVIS_API_URL, true, false, ccp);
        if (protovisAPI.length() == 0) {
            String protovisAPIDir = ccp.getPublicResourcesDirectory() + File.separator + PROTOVIS_API_PATH;
            InstallStatus status = ComponentUtils.installJARContainingResource(getClass(), PROTOVIS_JS, protovisAPIDir, false);
            switch (status) {
                case SKIPPED:
                    console.fine(String.format("Installation skipped - %s is already installed", PROTOVIS_JS));
                    break;

                case FAILED:
                    throw new ComponentContextException(String.format("Failed to install %s at %s",
                            PROTOVIS_JS, new File(protovisAPIDir).getAbsolutePath()));
            }

            protovisAPI = "/public/resources/" + PROTOVIS_API_PATH.replaceAll("\\\\", "/") + "/" + PROTOVIS_JS;
        }

        console.fine("Using Protovis API from: " + protovisAPI);
        context.put("protovisAPI", protovisAPI);
    }
}
