

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

package org.seasr.meandre.components.GoogleDocs;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;



/**
 * This component extracts Data from Google documents Directory and Creates a local copy of the Directory
 *
 * @author Surya Kallumadi;
 *
 */

//
// General Path:  Text -> SentenceDetector -> SentenceTokenizer -> Gazetteer
//

@Component(
		name = "Google Docs Downloader",
		creator = "Surya Kallumadi",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "google, google doc, download",
		description = "This component extracts data from a Google Docs directory and creates a local copy of the directory",
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar", "activation.jar","gdata-client-1.0.jar","gdata-client-meta-1.0.jar","gdata-core-1.0.jar","gdata-docs-3.0.jar","gdata-docs-meta-3.0.jar","gdata-media-1.0.jar","google-collect-1.0-rc1.jar","jsr305.jar","mail.jar","servlet-api-6.0.29.jar", "seasr-commons.jar"}
)
public class GDocsDirDownloader extends AbstractExecutableComponent {

	//------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = "GMailId",
            description = "The GMailId whose directory contents have to be accessed ",
            defaultValue = "gaz.meandre@gmail.com"
    )
    protected static final String PROP_GMAILID ="GMailId" ;

    @ComponentProperty(
            name = Names.PROP_PASSWORD,
            description = "Password of the GMailId ",
            defaultValue = "Seasr.Meandre"
    )
    protected static final String PROP_PASSWORD = Names.PROP_PASSWORD;

    @ComponentProperty(
            name = "folder_name",
            description = "The name of the folder whose content will be downloaded",
            defaultValue = "gaz"
    )
    protected static final String PROP_DIR = "folder_name";

    @ComponentProperty(
            name = Names.PROP_FILENAME,
            description = "The file that contains the list of gazeteer files",
            defaultValue = "lists.def"
    )
    protected static final String PROP_LISTS = Names.PROP_FILENAME;

    @ComponentProperty(
            name = "target_folder",
            description = "The local path where the gazeteers should be saved",
            defaultValue = "NewLists"
    )
    protected static final String PROP_PATH = "target_folder";

	//------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_LOCATION,
			description = "Location of Lists definition file" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
	)
	protected static final String OUT_LOCATION = Names.PORT_LOCATION;

	//--------------------------------------------------------------------------------------------

  //String userGMailId >> PROP_GMAILID,String password >> PROP_PASSWORD,String DirName >> PROP_DIR,String ListFileName >> PROP_LISTS,String TargetDir >> PROP_PATH

	GDocDownloadWrapper GdocHelper;

	protected String userGMailId;
	protected String password;
	protected String DirName;
	protected String ListFileName;
	protected String TargetDir;




    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception
    {

    	userGMailId = ccp.getProperty(PROP_GMAILID);
    	password = ccp.getProperty(PROP_PASSWORD);
    	DirName = ccp.getProperty(PROP_DIR);
    	ListFileName = ccp.getProperty(PROP_LISTS);
    	TargetDir = ccp.getProperty(PROP_PATH);
    	GdocHelper = new GDocDownloadWrapper();
	}


    @Override
    public void executeCallBack(ComponentContext cc) throws Exception
    {

    	String ListFileLocation=GDocDownloadWrapper.DownloadGDocDir(userGMailId,password,DirName,ListFileName,TargetDir);
    	console.info("List File available at --- "+ListFileLocation);
    	cc.pushDataComponentToOutput(OUT_LOCATION,ListFileLocation);
    }

    @Override

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {

    	userGMailId = null;
    	password = null;
    	DirName = null;
    	ListFileName = null;
    	TargetDir = null;
    }
    //--------------------------------------------------------------------------------------------

	@Override
	protected void handleStreamInitiators() throws Exception {
	}

	@Override
	protected void handleStreamTerminators() throws Exception {
	}
	//--------------------------------------------------------------------------------------------

}
