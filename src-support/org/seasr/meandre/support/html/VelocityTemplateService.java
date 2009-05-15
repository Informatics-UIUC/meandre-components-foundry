package org.seasr.meandre.support.html;



import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;

/* 
 * 
 * template loading notes:
 * 
 *  templates are searched in
 *     1 local file system on the server: published_resources/templates (under the server install)
 *     2 local file system on the server: ./templates  where . is user.path
 *     3 on the classpath
 *     4 in any jars on the classpath
 *     
 *     NOTES:  if we want don't want to use the Singleton, we can use VelocityEngine 
 *     see http://velocity.apache.org/engine/releases/velocity-1.5/developer-guide.html#to_singleton_or_not_to_singleton...
 *     
 */


public class VelocityTemplateService {
	
	
	static private VelocityTemplateService instance = null;
	
	protected VelocityTemplateService() 
	   throws Exception
	{
		
		Properties p = new Properties();
		p.setProperty("resource.loader", "url,file,class" );
		p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );
		p.setProperty("url.resource.loader.class", "org.apache.velocity.runtime.resource.loader.URLResourceLoader" );
	    p.setProperty("file.resource.loader.path", "published_resources/templates, WEB-INF/templates, ./templates");
	                               
	    // p.setProperty("url.resource.loader.root", "http://3gne.com/mikeh/");
	    // you need an empty string for the root property to work
	    p.setProperty("url.resource.loader.root", "");
	    Velocity.init( p );
	    
	    //Velocity.init("WEB-INF/velocity.properties");
	}
	
	
	public static VelocityTemplateService getInstance()
	{
		if (instance == null) {
			
			try {
				instance = new VelocityTemplateService();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return instance;
	}
	
	public VelocityContext getNewContext()
	{
		return new VelocityContext();
	}
	
	public String generateOutput(VelocityContext context, String templateName)
	   throws Exception
	{
		Template template = null;
		try {
			template = Velocity.getTemplate(templateName);
		}
		catch (ResourceNotFoundException rnf) {
			throw new RuntimeException("Unable to find the template " + templateName);
		}
		catch (ParseErrorException pee) {
			throw new RuntimeException("Unable to parse the template " + templateName);
		}
        
        StringWriter sw = new StringWriter();
    	template.merge(context,sw);
    	return sw.toString();
	}
}


/*
formInputName = ccp.getProperty(DATA_PROPERTY_FORM);
		
Reader reader = 
    new InputStreamReader(getClass().getClassLoader().
                               getResourceAsStream("history.vm"));
  VelocityContext context = new VelocityContext();
  context.put("location", location );
  context.put("weathers", weathers );
  StringWriter writer = new StringWriter();
  Velocity.evaluate(context, writer, "", reader);
  
  */
