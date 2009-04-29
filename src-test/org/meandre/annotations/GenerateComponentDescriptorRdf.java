/**
 * 
 */
package org.meandre.annotations;

import org.meandre.core.repository.CorruptedDescriptionException;
import org.meandre.core.repository.DataPortDescription;
import org.meandre.core.repository.ExecutableComponentDescription;
import org.meandre.core.repository.PropertiesDescriptionDefinition;
import org.meandre.core.repository.TagsDescription;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author bernie acs
 *
 */
public class GenerateComponentDescriptorRdf {
	
	/**
	 * 
	 */
	public GenerateComponentDescriptorRdf() {
	}

	/**
	 * @param componentKeyValues the componentKeyValues to set
	 */
	public void setComponentKeyValues(
			java.util.Hashtable<String, Object> componentKeyValues) {
		GenerateComponentDescriptorRdf.componentKeyValues = componentKeyValues;
	}

	/**
	 * @param componentPropertyKeyValues the componentPropertyKeyValues to set
	 */
	public void setComponentPropertyKeyValues(
			java.util.Hashtable<String, Object> componentPropertyKeyValues) {
		GenerateComponentDescriptorRdf.componentPropertyKeyValues = componentPropertyKeyValues;
	}

	/**
	 * @param componentInputKeyValues the componentInputKeyValues to set
	 */
	public void setComponentInputKeyValues(
			java.util.Hashtable<String, Object> componentInputKeyValues) {
		GenerateComponentDescriptorRdf.componentInputKeyValues = componentInputKeyValues;
	}

	/**
	 * @param componentOutputKeyValues the componentOutputKeyValues to set
	 */
	public void setComponentOutputKeyValues(
			java.util.Hashtable<String, Object> componentOutputKeyValues) {
		GenerateComponentDescriptorRdf.componentOutputKeyValues = componentOutputKeyValues;
	}

	private static java.util.Hashtable<String,Object> componentKeyValues = null;
	private static java.util.Hashtable<String,Object> componentPropertyKeyValues = null;
	private static java.util.Hashtable<String,Object> componentInputKeyValues = null;
	private static java.util.Hashtable<String,Object> componentOutputKeyValues = null;

	public String  getRdfDescriptor() throws CorruptedDescriptionException {
        String sName = (String)componentKeyValues.get("name");
        String sBaseURL = (String)componentKeyValues.get("baseURL");
        String sDescription = (String)componentKeyValues.get("description");
        // String sRights = getRights((String)componentKeyValues.get("rights"));
        String sRights = (componentKeyValues.containsKey("rights"))?(String)componentKeyValues.get("rights").toString():"";
        String sRightsOther = (componentKeyValues.containsKey("rightsOther"))?(String)componentKeyValues.get("rightsOther"):"";
        String sCreator = (componentKeyValues.containsKey("creator"))?(String)componentKeyValues.get("creator"):"";
        java.util.Date dateCreation = new java.util.Date();
        String sTags = (componentKeyValues.containsKey("tags"))?(String)componentKeyValues.get("tags"):"";
        String sFormat = (componentKeyValues.containsKey("format"))?(String)componentKeyValues.get("format"):"";
        //String type= getType((String)componentKeyValues.get("mode"));
        String type= (componentKeyValues.containsKey("mode"))?(String)componentKeyValues.get("mode").toString():"";
       
        // Runnable runnable = (Runnable)componentKeyValues.get("runnable");
        String sRunnable = (componentKeyValues.containsKey("runnable"))?(String)componentKeyValues.get("runnable").toString():"";
        //String sFiringPolicy = getFiringPolicy((String)componentKeyValues.get("firingPolicy"));
        String sFiringPolicy = (componentKeyValues.containsKey("firingPolicy"))?(String)componentKeyValues.get("firingPolicy").toString():"";

        String sLocation = (componentKeyValues.containsKey("location"))?(String)componentKeyValues.get("location"):"";
        
        if (sBaseURL.charAt(sBaseURL.length() - 1) != '/') {
            sBaseURL += '/';
        }
        sRights = (sRights.equals("Other")) ? sRightsOther : sRights;
        String sComponentName = sName.toLowerCase().replaceAll("[ ]+", " ").
        replaceAll(" ", "-");
        
		String sClassName = "";
		String[] saTmp = sName.replaceAll("[ ]+", " ").split(" ");
		for (String s : saTmp) {
			char chars[] = s.trim().toCharArray();
			chars[0] = Character.toUpperCase(chars[0]);
			sClassName += new String(chars);
		}

        Model model = ModelFactory.createDefaultModel();
        Resource resExecutableComponent = model.createResource(sBaseURL +
                sComponentName);
        Resource resLocation = model.createResource(sBaseURL + sComponentName +
                "/implementation/" + sLocation);

        sTags = (sTags == null) ? "" : sTags;
        if (sTags.indexOf(',') < 0) {
            saTmp = sTags.toLowerCase().replaceAll("[ ]+", " ").split(" ");
        } else {
            saTmp = sTags.toLowerCase().replaceAll("[ ]+", " ").split(",");
        }
        java.util.Set<String> setTmp = new java.util.HashSet<String>();
        for (String s : saTmp) {
            setTmp.add(s.trim());
        }
        TagsDescription tagDesc = new TagsDescription(setTmp);

        saTmp = new String[] {sBaseURL + sComponentName + "/implementation/"};
        java.util.Set<RDFNode> setContext = new java.util.HashSet<RDFNode>();
        for (String s : saTmp) {
            setContext.add(model.createResource(s.trim()));
        }

        java.util.Set<DataPortDescription> setInputs = getDataPortDescription(
        		componentInputKeyValues, model, sBaseURL, sComponentName, "input"
    	);
        
        java.util.Set<DataPortDescription> setOutputs = getDataPortDescription(
        		componentOutputKeyValues, model, sBaseURL, sComponentName, "output"
    	);
        
        PropertiesDescriptionDefinition pddProperties = getPropertiesDescriptionDefinition(componentPropertyKeyValues);
        // Generating the description
        Resource resMode = ExecutableComponentDescription.COMPUTE_COMPONENT;
        if(type.equals("webui")){
        	resMode = ExecutableComponentDescription.WEBUI_COMPONENT;
        }else{
        	resMode = ExecutableComponentDescription.COMPUTE_COMPONENT;
        }
        ExecutableComponentDescription ecd = new ExecutableComponentDescription(
                resExecutableComponent,
                sName,
                sDescription,
                sRights,
                sCreator,
                dateCreation,
                sRunnable,
                sFiringPolicy,
                sFormat,
                setContext,
                resLocation,
                setInputs,
                setOutputs,
                pddProperties,
                tagDesc,resMode
                                );
        // Generate the descriptors
        java.io.ByteArrayOutputStream bosRDF = new java.io.ByteArrayOutputStream();
        ecd.getModel().write(bosRDF);
        String sRDFDescription = bosRDF.toString();
        
        return sRDFDescription;
	}

	@SuppressWarnings("unchecked")
	public static java.util.Set<DataPortDescription> getDataPortDescription(
			java.util.Hashtable dataPortKeyValues,
			Model model,String sBaseUrl, 
			String sComponentName, 
			String sDirectionKey) throws CorruptedDescriptionException
		{
        java.util.Set<DataPortDescription> setUpDataPort = new java.util.HashSet<DataPortDescription>();
        if (dataPortKeyValues != null) {
            for (int i = 0, iMax = dataPortKeyValues.size(); i < iMax; i++) {
            	java.util.Set<java.util.Map.Entry<String,Object>> keyValueSet = 
            		(java.util.Set<java.util.Map.Entry<String,Object>>)dataPortKeyValues.get(Integer.toString(i));
            	java.util.Iterator<java.util.Map.Entry<String,Object>> iter = 
            		(java.util.Iterator<java.util.Map.Entry<String,Object>>)keyValueSet.iterator();
            	java.util.Hashtable<String,Object> ht = new java.util.Hashtable<String,Object>();
            	while (iter.hasNext()){
            		java.util.Map.Entry<String,Object> e = iter.next();
            		ht.put(e.getKey(), e.getValue());
            	}

            	String name = (String)ht.get("name");
                String description = (String)ht.get("description");
                String sID = name.toLowerCase().replaceAll("[ ]+", " ").
                             replaceAll(" ", "-");
                setUpDataPort.add(new DataPortDescription(model.createResource(
                        sBaseUrl + sComponentName+ "/" + sDirectionKey+ "/" + sID),
                        sBaseUrl + sComponentName+ "/" + sDirectionKey+ "/" + sID, name,
                        description));
            }
        }

		return setUpDataPort;
	}
	
	@SuppressWarnings("unchecked")
	public static PropertiesDescriptionDefinition getPropertiesDescriptionDefinition(java.util.Hashtable propertyKeyValues){
        java.util.Hashtable<String, String> htValues = new java.util.Hashtable<String, String>();
        java.util.Hashtable<String, String> htDescriptions = new java.util.Hashtable<String, String>();
     
        if (propertyKeyValues != null) {
            for (int i = 0, iMax = propertyKeyValues.size(); i < iMax; i++) {
            	java.util.Set<java.util.Map.Entry<String,Object>> keyValueSet = 
            		(java.util.Set<java.util.Map.Entry<String,Object>>)propertyKeyValues.get(Integer.toString(i));
            	java.util.Iterator<java.util.Map.Entry<String,Object>> iter = 
            		(java.util.Iterator<java.util.Map.Entry<String,Object>>)keyValueSet.iterator();
            	java.util.Hashtable<String,Object> ht = new java.util.Hashtable<String,Object>();
            	while (iter.hasNext()){
            		java.util.Map.Entry<String,Object> e = iter.next();
            		ht.put(e.getKey(), e.getValue());
            	}

                String name = (String)ht.get("name");
                String description = (String)ht.get("description");
                String defaultValue = (String)ht.get("defaultValue");
                String sKey = name;
                htValues.put(name, defaultValue);
                htDescriptions.put(sKey, description);
            }
        }

        PropertiesDescriptionDefinition pddProperties = new
                PropertiesDescriptionDefinition(htValues, htDescriptions);

        return pddProperties;
	}
	
}