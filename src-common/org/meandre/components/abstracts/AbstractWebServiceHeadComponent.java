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

import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.components.abstracts.util.EmptyHttpServletRequest;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;

/**
 * @author bernie acs
 *
 * <p>
 * General purpose WebServiceComponent that is designed to forward
 * ExecutionInstanceId, HttpServletRequest, HttpServletResponses,
 * and a Semaphore Object. The assumed tasks that should happen with
 * the FLOW that this Component is heading up should consist of an
 * optional (Http) Session handler, optional HttpServletRequest handler
 * (or parser) followed by some set of logic that will formulate the
 * HttpServletResponse (that returns something to the web-client).
 * </p><p>
 * This Component Provides outputs in two Forms; first is the
 * PackedDataComponents Object which contains all of the individual
 * outputs that are pushed to the outputs of this component. This
 * PackedDataComponents Object is a java.util.HashMap<String,Object>
 * where the String is the name of the ComponentOutput and the Object
 * is the DataComponent payload. The net-effect of this construct is
 * that two references are pushed for data output produced by this
 * Component.
 * </p><p>
 * The critical mechanical obligation of the above set of mechanics is
 * send something back to the web-client and to execute the release()
 * method of the Semaphore Object output from this Component. Two general
 * purpose abstract objects are available to act as the tail of a WebService
 * Flow;
 * <p>
 * 1). AbstractWebServiceTailPrintWriterComponent;<br>
 * 2). AbstractWebServiceTailOutputStreamComponent;
 * </p><p>
 * A basic general purpose AbstractWebServiceSessionComponent is also
 * available.
 * <p>
 * GeneralPurpose Flow Example;
 * </p><p>
 * (BEGINS FLOW) extended AbstractWebServiceHeadComponent-><br>
 * (Optional) extended AbstractWebServiceSessionComponent-><br>
 * (Implimentation Defined) WebServiceRequestHandler-><br>
 * (Implimentation Defined) WebServiceWork-><br>
 * extended AbstractWebServiceTail[PrintWriter|OutputStream]Component (ENDS FLOW)<br>
 * </p>
 */
public abstract class AbstractWebServiceHeadComponent extends AbstractExecutableComponent implements
		/* Configurable */ WebUIFragmentCallback {

	////////////////////////// ComponentProperties ///////////////////////////

	/**
	 * "HttpServletRequest ParameterName to evaluate for FlowAbortRequest(). default value = shutdownService"
	 */
	@ComponentProperty(
	description="HttpServletRequest ParameterName to evaluate for FlowAbortRequest(). default value = shutdownService" ,
	name="ShutdownParameterName",
    defaultValue="ShutdownService"
    )
	public static final String ShutdownParameterName = "ShutdownParameterName";

	/**
	 * "HttpServletRequest ParameterValue to evaluate for FlowAbortRequest(). default value = true"
	 */
	@ComponentProperty(
	description="HttpServletRequest ParameterValue to evaluate for FlowAbortRequest(). default value = true" ,
	name="ShutdownParameterValue",
    defaultValue="true"
    )
	public static final String ShutdownParameterValue = "ShutdownParameterValue";

	/**
	 * "HttpServletRequest ParameterValue to evaluate for FlowAbortRequest(). default value = true"
	 */
	@ComponentProperty(
	description="Allow HttpServletRequest Parameter to be evaluated in this component (ShutdownService is default operation defined in this component). default value = true" ,
	name="AllowComponentRequestParser",
    defaultValue="true"
    )
	public static final String AllowComponentRequestParser = "AllowComponentRequestParser";


	/**
	 * "Controls the number of milliseconds of sleep between evaluations of ComponentContext.isFlowAborting(), a value of 1000 is approximately 1 second "
	 */
	@ComponentProperty(
	description="Controls the number of milliseconds of sleep between evaluations of ComponentContext.isFlowAborting(), a value of 1000 is approximately 1 second " ,
	name="isFlowAbortingSleep",
    defaultValue="1000"
    )
	public static final String isFlowAbortingSleep = "isFlowAbortingSleep";

	////////////////////////// ComponentOutput ///////////////////////////

	/**
	 * "HttpServletRequest object is blindly forwarded, assumes that parsing will happen elsewhere "
	 */
	@ComponentOutput(
	description="HttpServletRequest object is blindly forwarded, assumes that parsing will happen elsewhere " ,
	name="HttpServletRequest"
    )
	public static final String httpServletRequest = "HttpServletRequest";

	/**
	 * "HttpServletResponse object is blindly forwarded, assumes that client response will be written elsewhere "
	 */
	@ComponentOutput(
	description="HttpServletResponse object is blindly forwarded, assumes that client response will be written elsewhere " ,
	name="HttpServletResponse"
    )
	public static final String httpServletResponse = "HttpServletResponse";

	/**
	 * "Semaphore object is blindly forwarded; normally would go to httpServletResponse writter to be released when client message is completed"
	 */
	@ComponentOutput(
	description="Semaphore object is blindly forwarded; normally would go to httpServletResponse writter to be released when client message is completed" ,
	name="Semaphore"
    )
	public static final String semaphore = "Semaphore";

	/**
	 * "ExecutionInstanceId String object is blindly forwarded; normally would be used as a reference to generated URL that would resolve to this component"
	 */
	@ComponentOutput(
	description="ExecutionInstanceId String object is blindly forwarded; normally would be used as a reference to generated URL that would resolve to this component" ,
	name="ExecutionInstanceId"
    )
	public static final String executionInstanceId = "ExecutionInstanceId";

	/**
	 * ComponentOutput(
	description="PackedOutputCollection combines all output ports into a java.util.Map<String,Object> to simplify flow building" ,
	name="PackedDataComponent"
    )
	 */
	@ComponentOutput(
	description="PackedOutputCollection combines all output ports into a java.util.Map<String,Object> to simplify flow building" ,
	name="PackedDataComponent"
    )
	public static final String OutPackedDataComponent = "PackedDataComponent";

	/* (non-Javadoc)
	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
	 */
	/**
	 * General purpose execute method which will be executed only once and enters a
	 * forever loop that tests the ComponentContext.isFlowAborting(), if this should
	 * become true the logical service will be Shutdown immedately.  The ComponentProperty
	 *
	 */
	public void execute(ComponentContext cc)
			throws ComponentExecutionException, ComponentContextException {
		super.execute(cc);
		//
        try {
            cc.startWebUIFragment(this);
            getConsoleLogger().info("Starting " + this.getClass().getName()+ " at " + cc.getFlowID());

            long sleepTime = Long.parseLong(cc.getProperty("isFlowAbortingSleep"));
            while (!cc.isFlowAborting()) {
                Thread.sleep(sleepTime);
            }

            getConsoleLogger().info("Aborting " + this.getClass().getName()+ " at " + cc.getFlowID()+ " Requested");
            cc.stopWebUIFragment(this);
       } catch (Exception e) {
            throw new ComponentExecutionException(e);
       }
       //
	}

	/**
	 * empty HttpServletRequest may be received at any time and special provision has been
	 * made to provide a default behavior in that event. For the General purpose case this
	 * Component class creates a (org.meandre.components.abstract.util.EmptyHttpServletRequest)
	 * which is then sent as the HttpServletResponse to the this.handle() method to processing.
	 * Implicitly this means that the Component designated to receive the HttpServletRequest
	 * should be prepared to handle an input event the is an emptyRequest ( which will be empty )
	 */
	public void emptyRequest(HttpServletResponse response) throws WebUIException {
		getConsoleLogger().entering(getClass().getName(), "emptyRequest", response);

	    HttpServletRequest request = (HttpServletRequest) new EmptyHttpServletRequest();
		handle( request, response);
	}

	/**
	 * handle all HttpServletRequest (including a potentially emptyRequest), for the General
	 * Purpose case this method is blindly passing both the HttpServletRequest an the HttpServletResponse
	 * Object out for processing, in addition a Semaphore is used to hold this method busy (holding
	 * open the network connection to the client) which should be sent to the Component that will be
	 * Writing a response for this request, this lock does not prevent this method from receiving new
	 * request (which would come on a different logical thread controlled by the Infrastructure's
	 * embedded ApplicationServer). The Writer should execute the Semaphore.release() when it has completed
	 * the task of making the response which in turn will allow this component (thread) to continue closing
	 * the logical network connection with the originating client.
	 */
	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws WebUIException {
		getConsoleLogger().entering(getClass().getName(), "handle", new Object[] { request, response });

		try {
            Semaphore sem = new Semaphore(1, true);
            sem.acquire();
            //
            // Build a new PackedDataComponents Object to populate and Push Out.
            packedDataComponentsOutput.put(httpServletRequest,  request);
            packedDataComponentsOutput.put(httpServletResponse, response);
            packedDataComponentsOutput.put(semaphore, sem);
           	packedDataComponentsOutput.put(executionInstanceId, getComponentContext().getExecutionInstanceID());
            //
            // If the component property AllowRequestParser is true then Do
            if(Boolean.parseBoolean(getComponentContext().getProperty(AllowComponentRequestParser))){
	            boolean isRequestPropagated = handleServiceRequest(request,response,sem);
	            if(!isRequestPropagated)
	            	return;
            }
            //
            // The assumption is that the packedDataComponentOutput will be the only output.
            if(isComponentOutputConnected(OutPackedDataComponent))
            	getComponentContext().pushDataComponentToOutput(OutPackedDataComponent,  packedDataComponentsOutput);
            //
            // Support for individual optional output ports for each output type produced in this component.
            if(isComponentOutputConnected(httpServletRequest))
            	getComponentContext().pushDataComponentToOutput(httpServletRequest,  request);
            if(isComponentOutputConnected(httpServletResponse))
            	getComponentContext().pushDataComponentToOutput(httpServletResponse, response);
            if(isComponentOutputConnected(semaphore))
            	getComponentContext().pushDataComponentToOutput(semaphore, sem);
            if(isComponentOutputConnected(executionInstanceId))
            	getComponentContext().pushDataComponentToOutput(executionInstanceId, getComponentContext().getExecutionInstanceID());
            //
            // Causes our service Thread to be blocked until the semaphore is released somewhere in our flow.
            sem.acquire();
            sem.release();
       } catch (InterruptedException e) {
            throw new WebUIException(e);
       } catch (ComponentContextException e) {
            throw new WebUIException(e);
       }
	}

	/**
	 *
	 * @param sem
	 */
	public void forceServiceShutdown(Semaphore sem){
		//
		// Clear all variables..
		getComponentContext().requestFlowAbortion();
		//
		// wait for the server to inform us that our request was received.
		while (! getComponentContext().isFlowAborting()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// do nothing.
			}
		}
		//
		// Release the Service we are working for.
		sem.release();
		//
		// Sleep one more second before returning (this is an implicit termination)
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// do nothing.
		}
	}

	/** This method evaluates if the HttpRequest should be handled in this component
	 *  the return value will be evaluated to determine if the request should be propagated (when true)
	 *  or not (when false).
	 *
	 * @param request
	 * @param response
	 * @param sem
	 * @return
	 */
	public boolean handleServiceRequest(
			HttpServletRequest request,
			HttpServletResponse response,
			Semaphore sem
	){
		//
		// Do the Shutdown evaluation (an example)
		String shutdownParameterValue = request.getParameter(
				getComponentContext().getProperty(ShutdownParameterName));

		if( shutdownParameterValue !=null
			&& shutdownParameterValue.equalsIgnoreCase(
					getComponentContext().getProperty(ShutdownParameterValue))
		) {
			//
			forceServiceShutdown(sem);
			return false;
		}
		return true;
	}
}
