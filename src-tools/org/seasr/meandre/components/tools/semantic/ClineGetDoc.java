package org.seasr.meandre.components.tools.semantic;

import java.util.Iterator;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;

import edu.illinois.ncsa.cline.aa.ArticleArchive;
import edu.illinois.ncsa.cline.aa.DocumentContent;
import edu.illinois.ncsa.cline.database.shared.SolrFields;
import edu.illinois.ncsa.cline.properties.ClineProperties;

@Component(
		name = "Cline Get Doc",
		creator = "Loretta Auvil",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "#INPUT, text, solr, mongo",
		description = "This component loads documents from Cline Center repostiory.",
		dependency = {"protobuf-java-2.2.0.jar",
				"c3p0-0.9.2.1.jar","httpcore-4.3.2.jar","org.dom4j.dom4j-1.6.1.jar","commons-io-2.3.jar",
				"httpmime-4.3.5.jar","slf4j-api-1.7.6.jar","core.jar","log4j-1.2.17.jar",
				"solr-solrj-4.9.0.jar","dom4j-1.6.1.jar","mchange-commons-java-0.2.3.4.jar",
				"trove4j-3.0.3.jar","gson-2.2.4.jar","mongo-java-driver-2.12.1.jar",
				"wstx-asl-3.2.7.jar","httpclient-4.3.5.jar","noggit-0.5.jar","zookeeper-3.4.6.jar"}
		)

public class ClineGetDoc extends AbstractStreamingExecutableComponent {

	//------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_QUERY,
			description = "The query to use against SOLR" +
					"<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
			)
	protected static final String IN_QUERY = Names.PORT_QUERY;

	// ------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TEXT,
			description = "The text of the document" +
					"<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
			)
	protected static final String OUT_TEXT = Names.PORT_TEXT;
	
	@ComponentOutput(
			name = "id",
			description = "The id (aid) of the document" +
					"<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
			)
	protected static final String OUT_ID = "id";

	//------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
			name = "SOLR_URL",
			description = "The url for the SOLR derived storage. ",
			defaultValue = "http://archive.clinecenter.illinois.edu:8983/solr/derived/")
	protected static final String PROP_SOLR = "SOLR_URL";


	@ComponentProperty(
			name = "MONGO_HOST",
			description = "The host name of the machine for Mongo. ",
			defaultValue = "archive.clinecenter.illinois.edu")
	protected static final String PROP_MONGO = "MONGO_HOST";
	
	@ComponentProperty(
            name = Names.PROP_WRAP_STREAM,
            description = "Should the output be wrapped as a stream?",
            defaultValue = "true"
    )
    protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;


	private ClineProperties _cp;
	private boolean _wrapStream;


	@Override
	public void initializeCallBack(ComponentContextProperties ccp)
			throws Exception {
		super.initializeCallBack(ccp);

		// Change the properties that point to the mongo database and the solr index. 
		// Defaults are archive.clinecenter.illinois.edu for both.
		_cp = ClineProperties.get();
		_cp.setProperty(ClineProperties.SORLDERIVED_URL,getPropertyOrDieTrying(PROP_SOLR, false, true, ccp));
		_cp.setProperty(ClineProperties.MONGODB_HOST,getPropertyOrDieTrying(PROP_MONGO, false, true, ccp));
		_wrapStream = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_WRAP_STREAM, ccp));
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		// construct the query.
		String[] input = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_QUERY));   
		String query = input[0];
		//String query = "source_name:SWB AND publication_date:[2020-01-01T06:00:00Z TO *]";
		
		if (_wrapStream) {
		    StreamDelimiter sd = new StreamInitiator(streamId);
		    cc.pushDataComponentToOutput(OUT_TEXT, sd);
		}
		
		// Issue a simple SOLR query, get an iterator to iterate over the documents, retrieve
		// the documents content.
		Iterator<DocumentContent> iter = ArticleArchive.get().getIterator(query);
		while (iter.hasNext()) {
			DocumentContent dc = iter.next();
			componentContext.pushDataComponentToOutput(OUT_TEXT, dc.get(SolrFields.content));
			componentContext.pushDataComponentToOutput(OUT_ID, dc.get(SolrFields.aid));
		}
		
		if (_wrapStream) {
		    StreamDelimiter sd = new StreamTerminator(streamId);
		    cc.pushDataComponentToOutput(OUT_TEXT, sd);
		}
	}	

	@Override
	public void disposeCallBack(ComponentContextProperties ccp)
			throws Exception {
		_cp = null;
	}

	@Override
	public boolean isAccumulator() {
		return false;
	}
}
