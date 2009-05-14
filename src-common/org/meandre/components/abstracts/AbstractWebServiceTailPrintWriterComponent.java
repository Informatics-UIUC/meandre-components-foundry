/**
 *
 */
package org.meandre.components.abstracts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.ComponentInput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;



/**
 * @author bernie acs
 * General purpose WebServiceComponent that is designed to end a Flow that
 * is functioning in the capacity of a presistent running service. The General
 * purpose case assumes that some Input Object will be recieved on ComponentInput
 * Port identified as httpServletResponsePayload and will attempt to push the
 * Data through using HttpServletResponse.getPrintWriter() by default. Setting
 * ComponentProperty
 */

public abstract class AbstractWebServiceTailPrintWriterComponent extends AbstractExecutableComponent {

	/**
	 * HttpServletResponse object that will be blindly forwarded to the client "
	 */
	@ComponentInput(
	description="HttpServletResponse object that will be blindly forwarded to the client. This Object is expected to be fully cooked and ready for delievery (HttpHeader, MimeType, and Size Should be handled externally)" ,
	name="HttpServletResponse"
    )
	public static final String httpServletResponse = "HttpServletResponse";

	/**
	 * "Semaphore object is blindly forwarded; normally would go to httpServletResponse writter to be released when client message is completed"
	 */
	@ComponentInput(
	description="Semaphore object that will be used to execute Semaphore.release() when the ResponsePayload is sent away" ,
	name="Semaphore"
    )
	public static final String semaphore = "Semaphore";

	/**
	 * "The Data Object recieved on this ComponentInput Port will be blindly passed \"PrintWriter\" to transmit to the web-based client"
	 */
	@ComponentInput(
	description="The Data Object recieved on this ComponentInput Port will be blindly passed \"PrintWriter\" to transmit to the web-based client" ,
	name="httpServletResponsePayload"
    )
	public static final String httpServletResponsePayload = "httpServletResponsePayload";

	/* (non-Javadoc)
	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
	 */
	/**
	 * General purpose execute method which will be executed only once and enters a
	 * forever loop that tests the ComponentContext.isFlowAborting(), if this should
	 * become true the logical service will be Shutdown immediately.  The ComponentProperty
	 *
	 */
	public void execute(ComponentContext cc)
			throws ComponentExecutionException, ComponentContextException {
		// logger = getLogger();
		//
		getConsoleLogger().entering(getClass().getName(), "execute", cc);

        Object sResponse = (Object)cc.getDataComponentFromInput(httpServletResponsePayload);
        Semaphore sem = (Semaphore) cc.getDataComponentFromInput(semaphore);
        HttpServletResponse response = (HttpServletResponse) cc.getDataComponentFromInput(httpServletResponse);

        try {
        	getConsoleLogger().fine("Attempting to send using PrintWriter");

            PrintWriter pw = response.getWriter();
            pw.println( sResponse );
            response.getWriter().flush();
            sem.release();

       } catch (IOException e) {

	       	getConsoleLogger().throwing(getClass().getName(), "execute", e);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            e.printStackTrace(ps);

       }
       //
       // we need our stuff executed before user executeCallback() is preformed.
       super.execute(cc);
	}

}
