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

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.generic.text.TagCloudImage;

/**
 * @author Lily Dong
 * @author Boris Capitanu
 */

@Component(
        creator = "Lily Dong",
        description = "Creates a tag cloud image from a word count table. " +
                      "If there are many tags to be displayed, " +
                      "reduce the maximum size of the font or increase the size of the canvas " +
                      "to accommodate all of tags.",
        name = "Tag Cloud Image Maker",
        rights = Licenses.UofINCSA,
        tags = "tag cloud, visualization",
        baseURL="meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TagCloudImageMaker extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TOKEN_COUNTS,
            description = "Tags to be analyzed." +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap" +
                "<br>TYPE: java.util.Map<java.lang.String, java.lang.Integer>"
    )
    protected static final String IN_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_RAW_DATA,
            description = "The image." +
                          "<br>TYPE: byte[]"
    )
    protected final static String OUT_IMAGE_RAW = Names.PORT_RAW_DATA;

    //------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
	        defaultValue = "1000",
            description = "This property sets the width of the canvas.",
            name = Names.PROP_WIDTH
	)
    protected static final String PROP_CANVAS_WIDTH = Names.PROP_WIDTH;

	@ComponentProperty(
	        defaultValue = "1000",
  		   	description = "This property sets the height of the canvas.",
  		   	name = Names.PROP_HEIGHT
	)
    protected static final String PROP_CANVAS_HEIGHT = Names.PROP_HEIGHT;

	@ComponentProperty(
	        defaultValue = "Courier",
	        description = "This property sets the name of the font to be used for the words in the tag cloud.",
	        name = Names.PROP_FONT_NAME
	)
    protected static final String PROP_FONT_NAME = Names.PROP_FONT_NAME;

	@ComponentProperty(
	        defaultValue = "150",
  			description = "This property sets the maximum size of the font.",
  			name = Names.PROP_MAX_SIZE
	)
    protected static final String PROP_FONT_MAX_SIZE = Names.PROP_MAX_SIZE;

	@ComponentProperty(
	        defaultValue = "20",
	        description = "This property sets the minimum size of the font to be used for the words in the tag cloud.",
	        name = Names.PROP_MIN_SIZE
	)
    protected static final String PROP_FONT_MIN_SIZE = Names.PROP_MIN_SIZE;

	@ComponentProperty(
	        defaultValue = "false",
    		description = "Set to 'true' to show the count for each tag. " +
    		              "Ensure there is enough space for showing the count. In doing so, " +
    		              "you could increase the canvas width or height or both, or reduce the font size.",
    		name = Names.PROP_SHOW_COUNT
	)
    protected static final String PROP_SHOW_COUNT = Names.PROP_SHOW_COUNT;

	@ComponentProperty(
	        defaultValue = "50",
    		description = "The number of pixels to increase the width by in the event all the words " +
    				      "cannot be displayed in the tag cloud",
    		name = "width_adjustment"
	)
    protected static final String PROP_WIDTH_ADJ = "width_adjustment";
	
	@ComponentProperty(
            defaultValue = "50",
            description = "The number of pixels to increase the height by in the event all the words " +
                          "cannot be displayed in the tag cloud",
            name = "height_adjustment"
    )
    protected static final String PROP_HEIGHT_ADJ = "height_adjustment";
	
	@ComponentProperty(
            defaultValue = "5.0",
            description = "The value to decrease the maximum font size by in the event all the words " +
                          "cannot be displayed in the tag cloud",
            name = "max_font_size_adjustment"
    )
    protected static final String PROP_FONT_SIZE_ADJ = "max_font_size_adjustment";
	
	@ComponentProperty(
            defaultValue = "",
            description = "Set a random seed value to be used to " +
                          "initiate randomness for each tag cloud generated. "+
                          "If no value is given, then the current time will be used.",
            name = Names.PROP_SEED
    )
    protected static final String PROP_SEED = Names.PROP_SEED;

    //--------------------------------------------------------------------------------------------


	int canvasWidth, canvasHeight;
	int widthAdj, heightAdj;
	float fontSizeMin, fontSizeMax;
	float fontSizeMaxAdj;
	String fontName;
	boolean showCounts;
	long seed;

	
    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        canvasWidth = Integer.parseInt(getPropertyOrDieTrying(PROP_CANVAS_WIDTH, true, true, ccp));
        canvasHeight = Integer.parseInt(getPropertyOrDieTrying(PROP_CANVAS_HEIGHT, true, true, ccp));
        
        widthAdj = Integer.parseInt(getPropertyOrDieTrying(PROP_WIDTH_ADJ, true, true, ccp));
        heightAdj = Integer.parseInt(getPropertyOrDieTrying(PROP_HEIGHT_ADJ, true, true, ccp));
        

        String fontName = getPropertyOrDieTrying(PROP_FONT_NAME, true, false, ccp);
        if (fontName.length() == 0)
            fontName = null;

        fontSizeMin = Float.parseFloat(getPropertyOrDieTrying(PROP_FONT_MIN_SIZE, true, true, ccp));
        fontSizeMax = Float.parseFloat(getPropertyOrDieTrying(PROP_FONT_MAX_SIZE, true, true, ccp));
        
        fontSizeMaxAdj = Float.parseFloat(getPropertyOrDieTrying(PROP_FONT_SIZE_ADJ, true, true, ccp));

        showCounts = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_SHOW_COUNT, true, true, ccp));

        String seedString = getPropertyOrDieTrying(PROP_CANVAS_WIDTH, true, false, ccp);
        seed = seedString.equals("") ? System.currentTimeMillis() : Long.parseLong(seedString);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Map<String, Integer> table = DataTypeParser.parseAsStringIntegerMap(cc.getDataComponentFromInput(IN_TOKEN_COUNTS));

        org.seasr.meandre.support.generic.text.TagCloudImageMaker tagCloudImageMaker;
        TagCloudImage image;
        int nTries = 1;
        
        do {
            tagCloudImageMaker = new org.seasr.meandre.support.generic.text.TagCloudImageMaker(seed,
                    canvasWidth, canvasHeight, fontName, fontSizeMin, fontSizeMax, showCounts);
            
            console.fine(String.format("Attempt %s: Creating the tag cloud image", nTries));
            image = tagCloudImageMaker.createTagCloudImage(table);
    
            if (image == null) {
                outputError("The tag cloud image cannot be created - no word counts found", Level.WARNING);
                return;
            }
    
            console.fine(String.format("Attempt %s: Tag cloud image created", nTries));
            
            canvasWidth += widthAdj;
            canvasHeight += heightAdj;
            fontSizeMax -= fontSizeMaxAdj;
            nTries++;
            
            if (fontSizeMax - fontSizeMin < 20 && !image.hasAllWords()) {
                console.warning("Minimum acceptable font size reached - adjusting only canvas size...");
                fontSizeMax = fontSizeMin + 20;
                fontSizeMaxAdj = 0;
            }
            
        } while (!image.hasAllWords());

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ImageIO.write(image, "png", baos);
	    baos.flush();
	    
 		cc.pushDataComponentToOutput(OUT_IMAGE_RAW, baos.toByteArray());
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
