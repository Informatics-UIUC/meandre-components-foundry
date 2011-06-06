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

package org.seasr.meandre.components.vis.multimedia;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.util.ComponentUtils;
import org.seasr.meandre.components.vis.html.VelocityTemplateToHTML;
import org.seasr.meandre.support.generic.io.FileUtils;
import org.seasr.meandre.support.generic.io.JARInstaller.InstallStatus;

/**
 * @author Boris Capitanu
 *
 * NOTE: If this component is used in a flow for the Zotero environment do not forget to
 *       set the 'jplayer_api_base_url' property to http://www.jplayer.org/2.0.0/
 */

//TODO: Need to figure out proper way to pass the media type (wav, mp3...etc) and to use input (URL? bytes? separate components?)

@Component(
        creator = "Boris Capitanu",
        description = "Audio / video player",
        name = "JPlayer",
        tags = "audio, video, player",
        rights = Licenses.UofINCSA,
        mode = Mode.webui,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = { "velocity-1.7-dep.jar", "jquery-1.4.4.jar", "jPlayer-2.0.0.jar" },
        resources = { "JPlayer.vm" }
)
public class JPlayer extends AbstractJQueryComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = "bytes",
            description = "The audio/video data" +
                          "<br>TYPE: java.net.URI" +
                          "<br>TYPE: java.net.URL" +
                          "<br>TYPE: byte[]" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes"
    )
    protected static final String IN_DATA = "bytes";

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = "has_video",
            description = "True if the input contains a video stream, False otherwise",
            defaultValue = "true"
    )
    protected static final String PROP_HAS_VIDEO = "has_video";

    @ComponentProperty(
            name = "css",
            description = "The stylesheet for the player, or leave empty to use embedded one",
            defaultValue = ""
    )
    protected static final String PROP_CSS = "css";

    @ComponentProperty(
            name = "jplayer_api_base_url",
            description = "The URL to the folder containing the JPlayer API, or leave empty to use the embedded one",
            defaultValue = ""
    )
    protected static final String PROP_JPLAYER_API_BASE_URL = "jplayer_api_base_url";

    @ComponentProperty(
            description = "The title for the page",
            name = Names.PROP_TITLE,
            defaultValue = "Multimedia Player"
    )
    protected static final String PROP_TITLE = Names.PROP_TITLE;

    private static final String DEFAULT_TEMPLATE = "org/seasr/meandre/components/vis/multimedia/JPlayer.vm";
    @ComponentProperty(
            description = "The template name",
            name = VelocityTemplateToHTML.PROP_TEMPLATE,
            defaultValue = DEFAULT_TEMPLATE
    )
    protected static final String PROP_TEMPLATE = VelocityTemplateToHTML.PROP_TEMPLATE;

    //--------------------------------------------------------------------------------------------


    protected static final String JPLAYER_API_PATH = "jplayer-api"; // this path is assumed to be appended to the published_resources location
    protected static final String JPLAYER_JS = "js/jquery.jplayer.min.js";
    protected static final String JPLAYER_SWF = "js";   // the folder where the .swf file is located
    protected static final String JPLAYER_CSS = "skin/jplayer.blue.monday.css";

    protected List<File> _tmpFiles = new ArrayList<File>();


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        String jPlayerBaseDir = getPropertyOrDieTrying(PROP_JPLAYER_API_BASE_URL, true, false, ccp);
        if (jPlayerBaseDir.length() == 0) {
            String jPlayerAPIDir = ccp.getPublicResourcesDirectory() + File.separator + JPLAYER_API_PATH;
            InstallStatus status = ComponentUtils.installJARContainingResource(getClass(), JPLAYER_JS, jPlayerAPIDir, false);
            switch (status) {
                case SKIPPED:
                    console.fine(String.format("Installation skipped - %s is already installed", JPLAYER_JS));
                    break;

                case FAILED:
                    throw new ComponentContextException(String.format("Failed to install %s at %s",
                            JPLAYER_JS, new File(jPlayerAPIDir).getAbsolutePath()));
            }

            jPlayerBaseDir = "/public/resources/" + JPLAYER_API_PATH.replaceAll("\\\\", "/") + "/";
        }

        console.fine("Using jPlayer API from: " + jPlayerBaseDir);
        context.put("jPlayerJS", jPlayerBaseDir + JPLAYER_JS);
        context.put("jPlayerSWF", jPlayerBaseDir + JPLAYER_SWF);

        String jPlayerCSS = getPropertyOrDieTrying(PROP_CSS, true, false, ccp);
        if (jPlayerCSS.length() == 0)
            jPlayerCSS = jPlayerBaseDir + JPLAYER_CSS;

        console.fine("Using jPlayer CSS from: " + jPlayerCSS);
        context.put("jPlayerCSS", jPlayerCSS);

        context.put("has_video", Boolean.parseBoolean(getPropertyOrDieTrying(PROP_HAS_VIDEO, ccp)));
        context.put("title", getPropertyOrDieTrying(PROP_TITLE, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Object input = cc.getDataComponentFromInput(IN_DATA);

        try {
            // Try parsing as a URL first
            URI uri = DataTypeParser.parseAsURI(input);
            context.put("src", uri.toString());
        }
        catch (Exception e) {
            byte[] data = DataTypeParser.parseAsByteArray(input);

            File tmpFile = File.createTempFile("jplayer_", null, new File(cc.getPublicResourcesDirectory()));
            FileOutputStream fos = new FileOutputStream(tmpFile);
            fos.write(data);
            fos.close();
            _tmpFiles.add(tmpFile);

            context.put("src", "/public/resources/" + tmpFile.getName());
        }

        super.executeCallBack(cc);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        super.disposeCallBack(ccp);

        if (_tmpFiles.size() > 0) {
            for (File tmpFile : _tmpFiles)
                FileUtils.deleteFileOrDirectory(tmpFile);
            _tmpFiles.clear();
        }
    }
}
