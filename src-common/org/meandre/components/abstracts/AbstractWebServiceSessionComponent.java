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

package org.meandre.components.abstracts;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Formatter;
import java.util.Hashtable;
import java.util.Random;
import java.util.concurrent.Semaphore;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.components.PackedDataComponents;
import org.meandre.components.abstracts.util.EmptyHttpServletRequest;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;

/**
 * @author bernie acs
 *
 */
public abstract class AbstractWebServiceSessionComponent extends
		AbstractExecutableComponent {
	//
	//
	public final static String HttpServletRequest = "HttpServletRequest";
	public final static String HttpServletResponse = "HttpServletResponse";
	public final static String Semaphore = "Semaphore";
	public final static String ExecutionInstanceId = "ExecutionInstanceId";

	//////////////////////// ComponentInputs ///////////////////////////

	/**
	 * "PackedInputCollection combines all output ports into a java.util.Map<String,Object> to simplify flow building"
	 */
	@ComponentInput(
	description="PackedInputCollection combined input in a java.util.Map<String,Object> to simplify flow building" ,
	name="PackedDataComponent"
    )
	public static final String InPackedDataComponent = "PackedDataComponent";

	////////////////////////// ComponentOutputs ///////////////////////////

	/**
	 * "HttpServletRequest Object to parse to determine what action to invoke."
	 */
	@ComponentOutput(
	description="HttpServletRequest Object to parse to determine what action to invoke.",
	name="HttpServletRequest")
	public final static String OutHttpServletRequest = "HttpServletRequest";

	/**
	 * "HttpServletResponse Object used to write defaultHomePage or forwarded to component that will act as writter."
	 */
	@ComponentOutput(
	description="HttpServletResponse Object used to write defaultHomePage or forwarded to component that will act as writter.",
	name="HttpServletResponse")
	public final static String OutHttpServletResponse = "HttpServletResponse";

	/**
	 * "Semaphore Object used to signal write action complete defaultHomePage or forwarded to component that will act as writter."
	 */
	@ComponentOutput(
	description="Semaphore Object used to signal write action complete defaultHomePage or forwarded to component that will act as writter.",
	name="Semaphore")
	public final static String OutSemaphore = "Semaphore";

	/**
	 * "ExecutionInstanceId String; used as Url reference."
	 */
	@ComponentOutput(
	description="ExecutionInstanceId String; used as Url reference.",
	name="ExecutionInstanceId")
	public final static String OutExecutionInstanceId = "ExecutionInstanceId";

	/**
	 * "UniqueSessionKey String; Identifies a logic web browser session."
	 */
	@ComponentOutput(
	description="UniqueSessionKey String; Idenitifies a logic web brwoser session.",
	name="UniqueSessionKey")
	public final static String OutUniqueSessionKey = "UniqueSessionKey";

	/**
	 * "PackedDataComponent combines all output ports into a java.util.Map<String,Object> to simplify flow building"
	 */
	@ComponentOutput(
	description="PackedOutputCollection combines all output ports into a java.util.Map<String,Object> to simplify flow building" ,
	name="PackedDataComponent"
    )
	public static final String OutPackedDataComponent = "PackedDataComponent";

	////////////////////////// ComponentProperties ///////////////////////////

	/**
	 * "HttpServletRequest ParameterName to evaluate for ClearSessionKey cache. default value = closeSession"
	 */
	@ComponentProperty(
	description="HttpServletRequest ParameterName to evaluate for ClearSessionKey cache. default value = closeSession" ,
	name="ClearSessionParameterName",
    defaultValue="CloseSession"
    )
	public static final String ClearSessionParameterName = "ClearSessionParameterName";

	/**
	 * "HttpServletRequest ParameterValue to evaluate for ClearSessionKey cache. default value = true"
	 */
	@ComponentProperty(
	description="HttpServletRequest ParameterValue to evaluate for ClearSessionKey cache. default value = true" ,
	name="ClearSessionParameterValue",
    defaultValue="true"
    )
	public static final String ClearSessionParameterValue = "ClearSessionParameterValue";

	@ComponentProperty(
	description="The Cookie Key Name to use for this instance must not contain the following characters: { [ ] ( ) = , \" / ? @ : }" ,
	name="CookieKeyNameValue",
    defaultValue="MeandreAbstractWebServiceCookieKey"
    )
	public static final String CookieKeyNameValue = "CookieKeyNameValue";

	//
	private String cookieKeyNameValue = null;

	private long sessionCounter = 0;
	private Hashtable<String, Cookie> sessionKeys =
		new Hashtable<String, Cookie>();

	private Hashtable<String, Object> sessionTime =
		new Hashtable<String, Object>();

	private String cookieValueEncoder(String inputValue){
		String encodedValue = null;
		try{
			encodedValue = java.net.URLEncoder.encode( inputValue, "UTF8" );
		} catch(UnsupportedEncodingException usf) {
			encodedValue = inputValue;
		}
		//
		// Now let be sure there is no conflicts for a cookie value
		// Disallowed characters for cookie = { [ ] ( ) = , " / ? @ : }
		encodedValue = encodedValue.replaceAll("\\{", "_");
		encodedValue = encodedValue.replaceAll("\\[", "_");
		encodedValue = encodedValue.replaceAll("\\]", "_");
		encodedValue = encodedValue.replaceAll("\\(", "_");
		encodedValue = encodedValue.replaceAll("\\)", "_");
		encodedValue = encodedValue.replaceAll("=", "_");
		encodedValue = encodedValue.replaceAll("\"", "_");
		encodedValue = encodedValue.replaceAll(",", "_");
		encodedValue = encodedValue.replaceAll("/", "_");
		encodedValue = encodedValue.replaceAll("\\?", "_");
		encodedValue = encodedValue.replaceAll("\\@", "_");
		encodedValue = encodedValue.replaceAll(":", "_");
		encodedValue = encodedValue.replaceAll("\\}", "_");

		return encodedValue;
	}
	/**
	 * General purpose RandomKey value generator that uses multiple variables
	 * to seed the java.util.Random() which would likely be unique even if
	 * concurrent execution of flows using this component logic were executed.
	 *
	 * @return
	 */
	private String getRandomKey(long timeKey, String instanceId){
		Random generator = new Random( System.nanoTime() );
		long   sesKey    = sessionCounterIncrement();
		String encodedInstanceId = cookieValueEncoder( instanceId );
		//
		// using the sum( session_counter, nanoTime) seed Random()
		generator.setSeed(sesKey + timeKey );
		//
		// build the big nasty "UniqueSessionKey" value.
		StringBuilder sb = new StringBuilder();
	    Formatter formatter = new Formatter(sb);
	    formatter.format(
	    		"%s~%0$020d~%0$020d",
	    		encodedInstanceId,
	    		Long.valueOf(generator.nextLong()),
	    		Long.valueOf(sesKey)
	    );
	    return sb.toString();
	}

	/**
	 *
	 * @return
	 */
	synchronized private long sessionCounterIncrement(){
		return sessionCounter++ ;
	}

	/**
	 *
	 * @return
	 */
	synchronized private long sessionCounterDecrement(){
		return sessionCounter-- ;
	}

	/**
	 *
	 * @param instanceId
	 * @return
	 */
	synchronized private Cookie setUpSession(String instanceId){
		long nanoTimeValue = System.nanoTime();
		String uniqueSessionKey = getRandomKey(nanoTimeValue, instanceId);

		Cookie cookie = new Cookie(cookieKeyNameValue, uniqueSessionKey);
		cookie.setComment("Use to maintain client application session");

		sessionKeys.put( uniqueSessionKey ,cookie );
		sessionTime.put( uniqueSessionKey, nanoTimeValue);

		return cookie;
	}

	/**
	 *
	 */
	private void purgeSessionKeys( String uniqueSessionKeyValue ){

		sessionTime.remove(uniqueSessionKeyValue);
		sessionKeys.remove(uniqueSessionKeyValue);
		sessionCounterDecrement();

	}

	/**
	 * @throws ComponentContextException
	 * @throws ComponentExecutionException
	 *
	 */
	public void execute(ComponentContext cc)
		throws ComponentExecutionException, ComponentContextException {
		super.execute(cc);
		try{
			HttpServletRequest request = null;
			HttpServletResponse response = null;
			Semaphore semaphore = null;
			String instanceId = null;
			String uniqueSessionKey = null;
			boolean pushDataOut = true;


			if( cookieKeyNameValue == null) {
				cookieKeyNameValue = cookieValueEncoder( cc.getProperty(CookieKeyNameValue) );

			}

			packedDataComponentsInput = (PackedDataComponents)cc.getDataComponentFromInput(InPackedDataComponent);
			//
			request = (HttpServletRequest)packedDataComponentsInput.get(HttpServletRequest);
			response = (HttpServletResponse)packedDataComponentsInput.get(HttpServletResponse);
			semaphore = (Semaphore)packedDataComponentsInput.get(Semaphore);
			instanceId = (String)packedDataComponentsInput.get(ExecutionInstanceId);

			//
			// Determine first if there is an emptyRequest OR parameterizedRequest.
			// Assuming some derivative of AbstractWebServiceHeadComponent is being used.
			if( request instanceof EmptyHttpServletRequest ){
				//
				// emptyRequest create new sessionKey.
				console.info(this.getClass().getName() +": Received Empty Request");

				Cookie cookie = setUpSession(instanceId);
				response.addCookie(cookie);
				uniqueSessionKey = cookie.getValue();
				//
				// enable hook for handling defaultHomePage request.
				pushDataOut=requestEmptyNewSessionKey(
						request,
						response,
						semaphore,
						instanceId,
						uniqueSessionKey
				);

			} else {
				//
				// parameterizedReuqest received, based on content send response.
				console.info(this.getClass().getName() +": Received Request With Parameters");

				Cookie cookies[] = request.getCookies();
				Cookie cookie = null;

				for(Cookie c: cookies){
					if(c.getName().equals(cookieKeyNameValue)){
						//
						// do we know this key value
						if(sessionKeys.containsKey(c.getValue())){
							//
							// compared the stored values.. now we know the uniqueKey
							uniqueSessionKey = c.getValue();
							long nanoTimeValue = Long.valueOf( (Long) sessionTime.get(uniqueSessionKey) );
							//
							console.fine( this.getClass().getName() +
													": Encountered a Parameterized Request with a valid UniqueSessionKey" +
													": Elapsed nanoTime since last seen = " +
													(System.nanoTime() - nanoTimeValue)
								);
							//
							// save the found value
							cookie = c;
							response.addCookie(cookie);
							//
							// enable hook for handling defaultHomePage reuqest.
							pushDataOut=requestParameterizedExistingSessionKeyFound(
									request,
									response,
									semaphore,
									instanceId,
									uniqueSessionKey
							);

						} else {
							//
							// Here is the case where we find a Cookie with the right name,
							// BUT it is not in the collection of cached active keys the simple answer
							// is to reset the cookie to a good one.
							console.fine( this.getClass().getName() +
													": Encountered a Parameterized Request with an invalid UniqueSessionKey"
								);
							//
							cookie = setUpSession(instanceId);
							response.addCookie(cookie);
							uniqueSessionKey = cookie.getValue();
							//
							// enable hook for handling defaultHomePage reuqest.
							pushDataOut=requestParameterizedExistingSessionKeyNotFound(
									request,
									response,
									semaphore,
									instanceId,
									uniqueSessionKey
							);

						}
					}
				}
				//
				//
				if(cookie == null){
					// could not locate existing session for this guy, start over.
					// This should mean that an authenticated session would have to be expired.
					// TODO: more work here .... required.
					console.fine( this.getClass().getName() +
											": Encountered a Parameterized Request with NO UniqueSessionKey"
						);
					//
					//
					cookie = setUpSession(instanceId);
					response.addCookie(cookie);
					uniqueSessionKey = cookie.getValue();
					//
					//
					pushDataOut=requestParameterizedNewSessionKey(
							request,
							response,
							semaphore,
							instanceId,
							uniqueSessionKey
					);
				}
			}
			//
			//
			console.fine(this.getClass().getName() +": Data event completed conditionally PushDataOutputs");

			if(pushDataOut){
				console.fine(this.getClass().getName() +": Data event completed, ALL Push DataOutputs");

	            // Build the packedOutputCollection.
				packedDataComponentsOutput.put(OutHttpServletRequest, request);
				packedDataComponentsOutput.put(OutHttpServletResponse, response);
				packedDataComponentsOutput.put(OutSemaphore, semaphore);
				packedDataComponentsOutput.put(OutExecutionInstanceId, instanceId);
				packedDataComponentsOutput.put(OutUniqueSessionKey, uniqueSessionKey );
	            //
	            cc.pushDataComponentToOutput(OutPackedDataComponent,  packedDataComponentsOutput);
	            //
	            // Push Individual ComponentOutput ports
				cc.pushDataComponentToOutput(OutHttpServletRequest, request);
				cc.pushDataComponentToOutput(OutHttpServletResponse, response);
				cc.pushDataComponentToOutput(OutSemaphore, semaphore);
				cc.pushDataComponentToOutput(OutExecutionInstanceId, instanceId);
				cc.pushDataComponentToOutput(OutUniqueSessionKey, uniqueSessionKey );

			} else {
				console.fine(this.getClass().getName() +": Data event completed, NO Push DataOutputs");
			}
		}catch(Exception e){
			console.throwing(getClass().getName(), "execute", e);
			throw new ComponentExecutionException(e);
		}

	}

	/** This method evaluates if the HttpRequest should be handled in this component
	 *
	 * @param request
	 * @param response
	 * @param sem
	 * @param instanceId
	 * @param uniqueSessionKey
	 * @return
	 */
	public boolean handleSessionRequest(
			HttpServletRequest request,
			HttpServletResponse response,
			Semaphore sem,
			String instanceId,
			String uniqueSessionKey
	){
		//
		// Do the ClearSession evaluation (second example)
		String clearSessionParameterValue = request.getParameter(
				componentContext.getProperty(ClearSessionParameterName));

		if( clearSessionParameterValue !=null
			&& clearSessionParameterValue.equalsIgnoreCase(
					componentContext.getProperty(ClearSessionParameterValue))
		) {
			//
			purgeSessionKeys(uniqueSessionKey);
			//
			Cookie cookie = new Cookie(CookieKeyNameValue, "sessionClosed" );
			response.addCookie(cookie);
			try {
				response.getWriter().println();
			} catch (IOException e) {
				// do nothing...
			}
			sem.release();
			return false;
		}
		return true;
	}

	/**
	 * Method Hook for condition where an Empty Request came
	 * this is normally a condition where the defaultHomePage
	 * or WelcomePgae should be sent. the general purpose function has
	 * generated a new UniqueSessionKey and embedded it into the
	 * Response Object.
	 *
	 * The boolean return value determines is the execute method
	 * should push Data to all ComponentOutput Ports
	 *
	 * An implementation can determine over ride this
	 * method to control behavior for this circumstance
	 *
	 * @param request
	 * @param response
	 * @param sem
	 * @param instanceId
	 * @param uniqueSessionKey
	 * @return boolean
	 */
	public boolean requestEmptyNewSessionKey(
			HttpServletRequest request,
			HttpServletResponse response,
			Semaphore sem,
			String instanceId,
			String uniqueSessionKey
	){
		return true;
	}

	/**
	 * Method Hook for condition where a Parameterized Request came
	 * with a keyed Cookie member which was Found in the active
	 * cache of UniqueSessionKeys. The general purpose function has
	 * embedded it into the Response Object.
	 *
	 * This method by default evaluates
	 * the RequestParameters for a parameterName that is defined in
	 * ComponentProperty ShutdownParameterName. IF it is found and
	 * the associated value of is equal to the value defined in
	 * ComponentProperty ShutdownParameterValue THEN this component
	 * will initiate an AbortFlowRequest.
	 *
	 * This method by default evaluates
	 * the RequestParameters for a parameterName that is defined in
	 * ComponentProperty ClearSessionParameterName. IF it is found and
	 * the associated value of is equal to the value defined in
	 * ComponentProperty ClearSessionParameterValue THEN this component
	 * will purge the UniqueSessionKey variables values cached AND
	 * cause an EmptyReponse to be returned to the client.
	 *
	 * The boolean return value determines is the execute method
	 * should push Data to all ComponentOutput Ports
	 *
	 * An implementation can determine over ride this
	 * method to control behavior for this circumstance
	 * >>
	 * >> consider calling super.thisMethod() before customized code
	 * >> to preserve general purpose functionality if it makes sense.
	 * >>
	 *
	 * @param request
	 * @param response
	 * @param sem
	 * @param instanceId
	 * @param uniqueSessionKey
	 * @return boolean
	 */
	public boolean requestParameterizedExistingSessionKeyFound(
			HttpServletRequest request,
			HttpServletResponse response,
			Semaphore sem,
			String instanceId,
			String uniqueSessionKey
	){
		return true;
	}

	/**
	 * Method Hook for condition where a Parameterized Request came
	 * with a keyed Cookie member which was NOT Found in the active
	 * cache of UniqueSessionKeys. The general purpose function has
	 * generated a new UniqueSessionKey and embedded it into the
	 * Response Object. A implementation can determine over ride this
	 * method to control behavior for this circumstance
	 *
	 * The boolean return value determines is the execute method
	 * should push Data to all ComponentOutput Ports
	 *
	 * An implementation can determine over ride this
	 * method to control behavior for this circumstance
	 *
	 * @param request
	 * @param response
	 * @param sem
	 * @param instanceId
	 * @param uniqueSessionKey
	 * @return boolean
	 */
	public boolean requestParameterizedExistingSessionKeyNotFound(
			HttpServletRequest request,
			HttpServletResponse response,
			Semaphore sem,
			String instanceId,
			String uniqueSessionKey
	){
		return true;
	}

	/**
	 * Method Hook for condition where a Parameterized Request came
	 * with no keyed Cookie member, the general purpose function has
	 * generated a new UniqueSessionKey and embedded it into the
	 * Response Object. A implementation can determine over ride this
	 * method to control behavior for this circumstance
	 *
	 * The boolean return value determines is the execute method
	 * should push Data to all ComponentOutput Ports
	 *
	 * An implementation can determine over ride this
	 * method to control behavior for this circumstance
	 *
	 * @param request
	 * @param response
	 * @param sem
	 * @param instanceId
	 * @param uniqueSessionKey
	 * @return boolean
	 */
	public boolean requestParameterizedNewSessionKey(
			HttpServletRequest request,
			HttpServletResponse response,
			Semaphore sem,
			String instanceId,
			String uniqueSessionKey
	){
		return true;
	}

}

