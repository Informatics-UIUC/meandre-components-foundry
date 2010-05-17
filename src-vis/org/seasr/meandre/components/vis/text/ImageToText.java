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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

import javax.imageio.ImageIO;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.core.BasicDataTypes.Bytes;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.generic.io.IOUtils;
import org.seasr.meandre.support.generic.io.StreamUtils;

@Component(
        creator = "Boris Capitanu",
        description = "Converts an image to ASCII text",
        name = "Image To ASCII Text",
        rights = Licenses.UofINCSA,
        tags = "image, text, ascii art",
        baseURL="meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class ImageToText extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_LOCATION,
            description = "The image to convert (url or raw data)" +
                "<br>TYPE: java.net.URI" +
                "<br>TYPE: java.net.URL" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes"
    )
    protected static final String IN_LOCATION = Names.PORT_LOCATION;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_HTML,
            description = "The HTML containing the ASCII art of the image" +
                "<br>TYPE: java.lang.String"
    )
    protected final static String OUT_HTML = Names.PORT_HTML;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = "old_school",
            description = "Pure ASCII art",
            defaultValue = "false"
        )
    protected static final String PROP_OLD_SCHOOL = "old_school";

    @ComponentProperty(
            name = "width",
            description = "Width",
            defaultValue = "100"
        )
    protected static final String PROP_WIDTH = "width";

    @ComponentProperty(
            name = "letters",
            description = "The letters to use",
            defaultValue = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        )
    protected static final String PROP_LETTERS = "letters";

    @ComponentProperty(
            name = "random",
            description = "Random",
            defaultValue = "false"
        )
    protected static final String PROP_RANDOM = "random";

    @ComponentProperty(
            name = "invert",
            description = "Invert?",
            defaultValue = "false"
        )
    protected static final String PROP_INVERT = "invert";

    //--------------------------------------------------------------------------------------------


    private static final String SERVICE_URL = "http://www.degraeve.com/img2txt-yay.php?url=";
    private static final String WEB_RESOURCE = "public/resources/";

    private File imgFile;
    private String serviceParams;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        serviceParams = "&mode=" + (Boolean.parseBoolean(ccp.getProperty(PROP_OLD_SCHOOL)) ? "A" : "H");
        serviceParams += "&size=" + ccp.getProperty(PROP_WIDTH);
        serviceParams += "&charstr=" + ccp.getProperty(PROP_LETTERS);
        serviceParams += "&order=" + (Boolean.parseBoolean(ccp.getProperty(PROP_RANDOM)) ? "R" : "O");
        serviceParams += "&invert=" + (Boolean.parseBoolean(ccp.getProperty(PROP_INVERT)) ? "Y" : "N");
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Object input = cc.getDataComponentFromInput(IN_LOCATION);

        BufferedImage image;
        imgFile = null;

        if (input instanceof Bytes || input instanceof byte[])
            image = ImageIO.read(new ByteArrayInputStream(DataTypeParser.parseAsByteArray(input)));
        else {
            URI uri = DataTypeParser.parseAsURI(input);
            image = ImageIO.read(StreamUtils.getURLforResource(uri));
        }

        imgFile = File.createTempFile("image", ".png", new File(cc.getPublicResourcesDirectory()));
        console.fine("Creating image file: " + imgFile.toString());

        ImageIO.write(image, "png", imgFile);

        URL serviceURL = new URL(SERVICE_URL + cc.getWebUIUrl(true) + WEB_RESOURCE + imgFile.getName() + serviceParams);
        console.fine("Service URL: " + serviceURL);

        cc.pushDataComponentToOutput(OUT_HTML, IOUtils.getTextFromReader(new InputStreamReader(serviceURL.openStream())));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        if (imgFile != null) {
            imgFile.delete();
        }
    }
}
