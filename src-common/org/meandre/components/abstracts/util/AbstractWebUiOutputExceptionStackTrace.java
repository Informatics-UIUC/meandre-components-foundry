/**
 *
 */
package org.meandre.components.abstracts.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author bernie acs
 *
 */
public abstract class AbstractWebUiOutputExceptionStackTrace {

	private static Logger logger = null;

	/**
	 * Show Stack trace on console, in the log, and try to send it to the client as Response
	 *
	 * @param response
	 * @param e
	 * @param message
	 */
	public void htmlOutputStackTrace(
			javax.servlet.http.HttpServletResponse response,
			Exception e,
			String message
	){
	    logger.log(Level.SEVERE, message, e);
		//
		response.setContentType("html/text");
		//
		try {
			//
			e.printStackTrace( response.getWriter() );
			//
		} catch (IOException e1) {
			logger.log(Level.SEVERE, e1.getMessage(), e1);
		}
	}

	/**
	 * @return the logger
	 */
	public static Logger getLogger() {
		return logger;
	}

	/**
	 * @param inLogger the logger to set
	 */
	public void setLogger(Logger inLogger) {
		logger = inLogger;
	}
}
