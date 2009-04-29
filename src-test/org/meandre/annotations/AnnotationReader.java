/**
 * 
 */
package org.meandre.annotations;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.Map.Entry;

import org.meandre.core.repository.CorruptedDescriptionException;

/**
 * @author bernie acs 
 * 
 *
 */
public class AnnotationReader {

	private static boolean debugPrint = false;
	private static String theClassName = "";

	private static java.util.Hashtable<String,Object> componentKeyValues = null;
	private static java.util.Hashtable<String,Object> componentPropertyKeyValues = null;
	private static java.util.Hashtable<String,Object> componentInputKeyValues = null;
	private static java.util.Hashtable<String,Object> componentOutputKeyValues = null;
		
		public AnnotationReader() {
			
			componentKeyValues = new java.util.Hashtable<String,Object>();
			componentPropertyKeyValues = new java.util.Hashtable<String,Object>();
			componentInputKeyValues = new java.util.Hashtable<String,Object>();
			componentOutputKeyValues = new java.util.Hashtable<String,Object>();
						
		}
		
		void findAnnotations(String className) throws ClassNotFoundException, CorruptedDescriptionException {
			java.util.Hashtable<Integer,String> theClassList = new java.util.Hashtable<Integer,String>();
			
			theClassName = className;
			Class<?> classObject = Class.forName(className);
			try{
				
				while(true ){
					if(debugPrint){
						System.out.println("* Caching className : " + classObject.getName() );
					}
					theClassList.put( Integer.valueOf( theClassList.size()), classObject.getName());					
					classObject = classObject.getSuperclass();
					if(classObject == null) break;
				}
				

				for(int i=0; i<=theClassList.size() ; i++ ){
					theClassName = theClassList.get(Integer.valueOf(i));
					if(theClassName==null)break;
					
					classObject = Class.forName(theClassName);
					if(classObject==null)break;
					
					if(debugPrint){
						System.out.println("** Looking at: " + classObject.getName() );
					}
					parseClassObject(classObject);
				}
				
			} catch (Exception e){
				e.printStackTrace();
			}
			
			if(debugPrint){ 
				System.out.println("Collected all annotation details; processing ...");

				GenerateComponentDescriptorRdf comDescRdf = new GenerateComponentDescriptorRdf();
				comDescRdf.setComponentInputKeyValues(componentInputKeyValues);
				comDescRdf.setComponentOutputKeyValues(componentOutputKeyValues);
				comDescRdf.setComponentPropertyKeyValues(componentPropertyKeyValues);
				comDescRdf.setComponentKeyValues(componentKeyValues);
				
				String sRDFDescription = comDescRdf.getRdfDescriptor();
				
				System.out.println( sRDFDescription );
			}
			
		}

		public static void parseClassObject( Class<?> classObject ){
			readAnnotation(classObject);
			java.lang.reflect.Field[] fields = null;
			try {
				// fields = classObject.getDeclaredFields();
				//          returns only public
				fields = classObject.getDeclaredFields();
			} catch ( SecurityException se) {
				
			}
			for (java.lang.reflect.Field f: fields){
					readAnnotation(f);
			}			
		}
		
		public static void readAnnotation(AnnotatedElement element)
		{
			java.util.Hashtable<String,Object> annotationKeyValues = new java.util.Hashtable<String,Object>();
			
			try
			{

				Annotation[] classAnnotations = element.getAnnotations();
				if(classAnnotations == null) return; 
				
				for(Annotation annotation : classAnnotations)
				{
					if(debugPrint)System.out.println("*** Processing : " + annotation.annotationType().getCanonicalName());
					
					Method[] methods = null;
					Class<?> annotationClass = (Class<?>)annotation.annotationType();
					
					try {
						methods = (annotationClass).getDeclaredMethods();
					} catch ( SecurityException se) {
						se.printStackTrace();
					}
					for(Method m: methods){
						//
						// collect details and execute method to derive resultant
						Class<?> returnType = m.getReturnType();
						String methodName = m.getName();
						Object value = (Object)(m.invoke(annotation,(Object[])null));
						if(value == null)
							value = "";
						//
						// only two variations to be concerned with Array and String (there are potentially other types)
						if(returnType.isArray() ){
							if(debugPrint)System.out.print(">>>> " + methodName +" = { ");
							Object w[] = (Object[])value ;
							for(int i=0;i<w.length; i++){
								if(debugPrint)System.out.print(w[i].toString() + ((i<(w.length-1))? " , " : "") );
							}
							if(debugPrint)System.out.println(" }");
							annotationKeyValues.put(methodName, w);							
						} else {
							if(debugPrint)System.out.println(">>>> " + methodName +" = "+ value );
							annotationKeyValues.put(methodName, value);
						}
					}
					classifyResultant(annotation, annotationKeyValues);
				}
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
		
		public static void classifyResultant( Object annType, java.util.Hashtable<String,Object> ht){
			
			if(isDebugPrint())
				System.out.println(">>>>>>>>>>>>> Classify Resultant Event <<<<<<<<<<<");
			
			//
			// one per class bing processed (this is easy)
			if( annType instanceof Component ){
				if(componentKeyValues.size() > 0) return;
				ht.put("location",theClassName );
				componentKeyValues.putAll(ht) ;
			} else if( annType instanceof ComponentProperty){
				//
				// This type can have many to one per class begin processed
				int s = componentPropertyKeyValues.size();
				replacables(ht,componentPropertyKeyValues);
				if(ht.size()>0)
				componentPropertyKeyValues.put( Integer.toString(s++) ,ht.entrySet()) ;
			} else if( annType instanceof ComponentInput){
				int s = componentInputKeyValues.size();
				replacables(ht,componentPropertyKeyValues);
				if(ht.size()>0)			
				componentInputKeyValues.put( Integer.toString(s++) ,ht.entrySet()) ;
			} else if( annType instanceof ComponentOutput){
				int s = componentOutputKeyValues.size();
				replacables(ht,componentPropertyKeyValues);
				if(ht.size()>0)				
				componentOutputKeyValues.put( Integer.toString(s++) ,ht.entrySet()) ;
			} else {
				
				System.out.println("Unable to classify resultant; This should not be happening! simply ignoring for now..");
				
			}
			return;
		}

		@SuppressWarnings("unchecked")
		public static void replacables(
				java.util.Hashtable<String,Object> newEntries,
				java.util.Hashtable<String,Object> oldEntries
				){
			//
			// oldEntries look like Integer() stored as String() (KEY) , 
			//		the Value Object is (java.util.Map.Entry<String,Object>)
			// 1, collection of entries<?>
			//		name = , ...
			// 2, collection of entries<?>
			//		name = , ...
			java.util.Set<java.util.Map.Entry<String,Object>> newEntrySet = newEntries.entrySet();
			for(java.util.Map.Entry<String,Object> newItem : newEntrySet ){
				if(newItem.getKey().equalsIgnoreCase("name")){
					//
					// found an elementName to check for.
					if((oldEntries != null) && (oldEntries.size()>0))
					for(int i=0; i<=oldEntries.size(); i++){
						java.util.Set<java.util.Map.Entry<String,Object>> oldEntrySet =
							(Set<Entry<String, Object>>) oldEntries.get( Integer.toString(i) );
						boolean removeFlag = false;
						if((oldEntrySet != null) && ( oldEntrySet.size() > 0))
						for(java.util.Map.Entry<String,Object> oldItem : oldEntrySet ){
							if( (oldItem.getKey().equalsIgnoreCase("name")) && 
								(oldItem.getValue().equals(newItem.getValue()))
						      ){
								removeFlag=true;
							}
						}
						if(removeFlag){
							if(isDebugPrint())
								System.out.println(">>>> Retained existing entry set:\n>>>> " + oldEntrySet + "\n>>>> Discard in inherited entry set\n>>>> " + newEntries);
							newEntries.clear();
						}
					}
				}
			}
		}
		/*
		public static void reindexHashtable( java.util.Hashtable<String, Object> ht){
			java.util.Hashtable<String, Object> internalCopy = new java.util.Hashtable<String, Object>();
			java.util.Enumeration<String> e = ht.keys();
			while( e.hasMoreElements()){
				internalCopy.put( Integer.toString( internalCopy.size() ) , ht.get(e.nextElement())) ; 
			}
			ht.clear();
			ht.putAll(internalCopy);
		}
		*/
		public static void main(String args[]) throws Exception
		{
			
			AnnotationReader ar = new AnnotationReader();			
			if(args.length>1)
				ar.setDebugPrint(Boolean.parseBoolean(args[1]));
			
			ar.findAnnotations(args[0]);
						
		}


		/**
		 * @return the componentKeyValues
		 */
		public java.util.Hashtable<String, Object> getComponentKeyValues() {
			return componentKeyValues;
		}


		/**
		 * @return the componentPropertyKeyValues
		 */
		public java.util.Hashtable<String, Object> getComponentPropertyKeyValues() {
			return componentPropertyKeyValues;
		}


		/**
		 * @return the componentInputKeyValues
		 */
		public java.util.Hashtable<String, Object> getComponentInputKeyValues() {
			return componentInputKeyValues;
		}


		/**
		 * @return the componentOutputKeyValues
		 */
		public java.util.Hashtable<String, Object> getComponentOutputKeyValues() {
			return componentOutputKeyValues;
		}


		/**
		 * @return the debugPrint
		 */
		public static boolean isDebugPrint() {
			return debugPrint;
		}


		/**
		 * @param dp the debugPrint to set
		 */
		public void setDebugPrint(boolean dp) {
			debugPrint = dp;
		}

	
}