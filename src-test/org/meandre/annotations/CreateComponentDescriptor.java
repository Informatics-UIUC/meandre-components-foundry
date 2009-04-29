/**
 * 
 */
package org.meandre.annotations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;


import org.meandre.core.repository.CorruptedDescriptionException;

/**
 * @author bernie acs
 * 
 *
 */
public class CreateComponentDescriptor {

	private static String componentClassName= "";
	private static String componentDescriptorFolderName = "";
    private static boolean printOnly = false;
    private static boolean makeSubs  = false;
    private static File componentDescriptorFile = null;

	/**
	 * 
	 */
	public CreateComponentDescriptor() {
	}
    
	public static String getComponentDescriptorRdf() 
		throws ClassNotFoundException, CorruptedDescriptionException {
		
		AnnotationReader ar = new AnnotationReader();
		ar.findAnnotations(getComponentClassName());
		
		GenerateComponentDescriptorRdf comDescRdf = new GenerateComponentDescriptorRdf();
		comDescRdf.setComponentInputKeyValues(ar.getComponentInputKeyValues());
		comDescRdf.setComponentOutputKeyValues(ar.getComponentOutputKeyValues());
		comDescRdf.setComponentPropertyKeyValues(ar.getComponentPropertyKeyValues());
		comDescRdf.setComponentKeyValues(ar.getComponentKeyValues());
		
		String sRDFDescription = comDescRdf.getRdfDescriptor();
		return sRDFDescription;
	}
	
	private static String getAbsoluteOutputPathFileName(String cdfName, String cClassName, boolean noSubDirs){
        String dirPath = null;
        String clsName = null;
        
        if (cClassName.lastIndexOf(".") == -1) {
            dirPath = cdfName;
            clsName = cdfName;
        } else {
        	if(!noSubDirs){
	            dirPath = cdfName + File.separator +
	            cClassName.substring(0, (cClassName.lastIndexOf(".")+1) );
	            
	            dirPath = dirPath.replace('.', File.separatorChar);
	            if (!(new File(dirPath)).exists()) {
	                new File(dirPath).mkdirs();
	            }
	            
	            // dirPath = dirPath + File.separator;
	            
        	} else {
        		dirPath = cdfName;
        	}
            clsName = cClassName.substring( (cClassName.lastIndexOf(".") + 1) );
        }


/*
        String componentDescriptor = klazz.getSimpleName();
        // write the descriptor to a file
        writeToFile(sRDFDescription,
                    dirPath + File.separator + componentDescriptor + ".rdf");

        System.out.println("Descriptor written to: " + dirPath + File.separator +
                           componentDescriptor + ".rdf");
*/        
        
        return  dirPath + File.separator + clsName + ".rdf";
	}
	
    private static void writeToFile(String description, String absoluteFilePath, String encoding) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(
            			new OutputStreamWriter(
            					new FileOutputStream(
            							absoluteFilePath), encoding)
            			);
            out.write(description);
            out.flush();
            out.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

	/**
	 * @param args
	 */
    public static void main(String args[]) {
        if (args.length < 2) {
            System.out.println(
                    "Usage: java CreateComponentDescriptor org.foo.Component descriptor_directory [noSubDirectories (true||false)] [printOnly (true||false)]");
            System.exit(0);
        }
        
        String className = args[0];
        String componentDescriptorFolder = args[1];

        //
        // four arguments setPrintOnly to the desired setting
        if(args.length > 3){
        	setPrintOnly ( Boolean.parseBoolean(args[3]));
        }
        
        //
        // three arguments setMakeSubs to the desired setting        
        if(args.length > 2){
        	setMakeSubs ( Boolean.parseBoolean(args[2]) );
        }
   
        File cdf = new File(componentDescriptorFolder);
        
        if (!(cdf).exists()) {
            System.out.println("Cannot continue... " +
                               cdf.getAbsolutePath() + " does not exist.");
            System.exit(0);
        }
        
        String classesToDo[] = new String[args.length];
        int ai = 0;
        classesToDo[ai++] = className;
        for(int i=4; i< args.length ; i++){
        	classesToDo[ai++] = args[i];	
        }
        for(int i=0; i<ai; i++){
	        CreateComponentDescriptor ccd = new CreateComponentDescriptor();
	        ccd.setComponentClassName(classesToDo[i]);
	        ccd.setComponentDescriptorFolderName(componentDescriptorFolder);
	        ccd.setComponentDescriptorFile(cdf);
	        ccd.processComponentDescriptor();
        }
    }

    public void processComponentDescriptor(){
        try {
            if(! isPrintOnly() ){
            	
            	String theDerivedPathFileName = getAbsoluteOutputPathFileName(
            			getComponentDescriptorFile().getAbsolutePath(),
            			getComponentClassName(), 
            			isMakeSubs()		
            	);
            	
				writeToFile(
						getComponentDescriptorRdf(), 
						theDerivedPathFileName, 
						"UTF8"		
				);
				
            } else {
            	System.out.println(getComponentDescriptorRdf());
            }	
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (CorruptedDescriptionException e) {
			e.printStackTrace();
		}

    }
	/**
	 * @return the componentDescriptorFile
	 */
	public static File getComponentDescriptorFile() {
		return componentDescriptorFile;
	}

	/**
	 * @param cdf the componentDescriptorFile to set
	 */
	public void setComponentDescriptorFile(File cdf) {
		componentDescriptorFile = cdf;
	}    
    
	/**
	 * @return the componentClassName
	 */
	public static String getComponentClassName() {
		return componentClassName;
	}


	/**
	 * @param ccn the componentClassName to set
	 */
	public void setComponentClassName(String ccn) {
		componentClassName = ccn;
	}


	/**
	 * @return the componentDescriptorFolderName
	 */
	public static String getComponentDescriptorFolderName() {
		return componentDescriptorFolderName;
	}


	/**
	 * @param cdf the componentDescriptorFolderName to set
	 */
	public void setComponentDescriptorFolderName(
			String cdf) {
		componentDescriptorFolderName = cdf;
	}

	/**
	 * @return the printOnly
	 */
	public static boolean isPrintOnly() {
		return printOnly;
	}

	/**
	 * @param po the printOnly to set
	 */
	public static void setPrintOnly(boolean po) {
		printOnly = po;
	}

	/**
	 * @return the makeSubs
	 */
	public static boolean isMakeSubs() {
		return makeSubs;
	}

	/**
	 * @param ms the makeSubs to set
	 */
	public static void setMakeSubs(boolean ms) {
		makeSubs = ms;
	}


}