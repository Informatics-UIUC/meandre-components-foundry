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

package org.seasr.meandre.components.tools.text.io;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.generic.io.IOUtils;
import org.seasr.meandre.support.generic.io.StreamUtils;

/**
 * Reads text from a local or remote location
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 */

@Component(
		name = "Read Text",
		creator = "Boris Capitanu",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "#INPUT, semantic, io, read, text",
		description = "This component reads text from a local or remote location. The text location is specified " +
				      "in the input. The component outputs the text " +
				      "read. A property controls the behavior of the component in " +
				      "the event of an IO error, allowing it to ignore the error and continue, or " +
				      "throw an exception, forcing the finalization of the flow execution.",
		dependency = {"protobuf-java-2.2.0.jar"}
)
public class ReadText extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_LOCATION,
			description = "The URL or file name containing the text to read" +
                "<br>TYPE: java.net.URI" +
                "<br>TYPE: java.net.URL" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String IN_LOCATION = Names.PORT_LOCATION;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_LOCATION,
			description = "The location that the text was read from" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_LOCATION = Names.PORT_LOCATION;

	@ComponentOutput(
			name = Names.PORT_TEXT,
			description = "The text read" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_TEXT = Names.PORT_TEXT;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            description = "The connection timeout in milliseconds " +
                    "(amount of time to wait for a connection to be established before giving up; 0 = wait forever)",
            name = Names.PROP_CONNECTION_TIMEOUT,
            defaultValue = "0"
    )
    protected static final String PROP_CONNECTION_TIMEOUT = Names.PROP_CONNECTION_TIMEOUT;

    @ComponentProperty(
            description = "The read timeout in milliseconds " +
                    "(amount of time to wait for a read operation to complete before giving up; 0 = wait forever)",
            name = Names.PROP_READ_TIMEOUT,
            defaultValue = "0"
    )
    protected static final String PROP_READ_TIMEOUT = Names.PROP_READ_TIMEOUT;

    @ComponentProperty(
            description = "Retry on connection or read timeout?",
            name = "retry_on_timeout",
            defaultValue = "true"
    )
    protected static final String PROP_RETRY_ON_TIMEOUT = "retry_on_timeout";

    @ComponentProperty(
            description = "Retry on HTTP error codes? (Use a comma-separated set of code(s) to retry on, or 0 to disable retry based on HTTP status codes). " +
                          "For example, if 'retry_on_http_error' is set to '503, 500' it means that if the HTTP connection " +
                          "fails with any of these errors, the operation will be retried up to the 'max_retry' times",
            name = "retry_on_http_error",
            defaultValue = "0"
    )
    protected static final String PROP_RETRY_HTTP_ERROR = "retry_on_http_error";

    @ComponentProperty(
            description = "Maximum number of attempts before giving up",
            name = "max_attempts",
            defaultValue = "1"
    )
    protected static final String PROP_MAX_ATTEMPTS = "max_attempts";

    @ComponentProperty(
            description = "The amount of time to wait between retries (in milliseconds)",
            name = "retry_delay",
            defaultValue = "1000"
    )
    protected static final String PROP_RETRY_DELAY = "retry_delay";

	//--------------------------------------------------------------------------------------------

    protected int connectionTimeout;
    protected int readTimeout;
    protected boolean retryOnTimeout;
    protected Set<Integer> retryHttpCodes = new HashSet<Integer>();
    protected int maxAttempts;
    protected int retryDelay;

    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        connectionTimeout = Integer.parseInt(getPropertyOrDieTrying(PROP_CONNECTION_TIMEOUT, ccp));
        readTimeout = Integer.parseInt(getPropertyOrDieTrying(PROP_READ_TIMEOUT, ccp));
        retryOnTimeout = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_RETRY_ON_TIMEOUT, ccp));
        maxAttempts = Integer.parseInt(getPropertyOrDieTrying(PROP_MAX_ATTEMPTS, ccp));
        retryDelay = Integer.parseInt(getPropertyOrDieTrying(PROP_RETRY_DELAY, ccp));

        String httpCodes = getPropertyOrDieTrying(PROP_RETRY_HTTP_ERROR, ccp);
        StringTokenizer st = new StringTokenizer(httpCodes, ",");
        while (st.hasMoreTokens())
            retryHttpCodes.add(Integer.parseInt(st.nextToken().toString().trim()));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        URI uri = DataTypeParser.parseAsURI(cc.getDataComponentFromInput(IN_LOCATION));
        URL url = StreamUtils.getURLforResource(uri);

        for (int retryCount = 1; retryCount <= maxAttempts; retryCount++) {
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(connectionTimeout);
            connection.setReadTimeout(readTimeout);

            try {
                console.fine("Connecting to " + connection.getURL());
                connection.connect();
                console.fine("Connection established, now reading data...");

                String sRes = IOUtils.getTextFromReader(new InputStreamReader(connection.getInputStream()));

                cc.pushDataComponentToOutput(OUT_LOCATION, BasicDataTypesTools.stringToStrings(uri.toString()));
                cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(sRes));

                break;
            }
            catch (ConnectException e) {
                // Connection refused - no retry
                console.severe(String.format("Could not connect to %s: %s", connection.getURL(), e.getMessage()));
                throw e;
            }
            catch (SocketTimeoutException e) {
                console.warning(String.format("Attempt %d: %s", retryCount, e.getMessage()));
                if (!retryOnTimeout || retryCount == maxAttempts)
                    throw e;
            }
            catch (IOException e) {
                if (connection instanceof HttpURLConnection) {
                    HttpURLConnection httpConnection = (HttpURLConnection)connection;
                    int respCode = httpConnection.getResponseCode();
                    console.warning(String.format("Attempt %d: Server returned HTTP %d error for %s", retryCount, respCode, connection.getURL()));
                    if (!retryHttpCodes.contains(respCode) || retryCount == maxAttempts)
                        throw e;
                }
            }
            finally {
                if (connection instanceof HttpURLConnection)
                    ((HttpURLConnection)connection).disconnect();
            }

            console.fine(String.format("Sleeping %,d ms...", retryDelay));
            Thread.sleep(retryDelay);
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
