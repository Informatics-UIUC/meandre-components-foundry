/**
 * 
 */
package org.seasr.meandre.component.opennlp;

import java.io.File;
import java.io.FileOutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;



/** This class provides basic mechanics to install OpenNLP
 *  models in the environment for the component to reach.
 *  
 * @author Xavier Llor&agrave;
 *
 */
public class ModelInstaller {

	/** Chunk size */
	private static final int READ_WRITE_CHUNK_SIZE = 65536;

	/** Install the contents of the jar at the given location. If location
	 * exists no installation is performed, unless forced.
	 * 
	 * @param sRootDir The location of the root directory where to install the stuff
	 * @param sJarName The name of the jar to expand
	 * @param bForce Force the installation by deleting the folder
	 * @return True is the process finished correctly, false otherwhise.
	 */
	public static boolean installJar ( String sRootDir, String sJarName, boolean bForce ) {
		File fRootDir = new File(sRootDir);
		// Basic checking
		if ( fRootDir.exists() ) {
			if ( bForce ) {
				boolean bOK = deleteDir(fRootDir);
				if ( !bOK ) return false;
			}
			else {
				return true;
			}
		}
		else
			fRootDir.mkdirs();
		
		// Unjar the contents 
		try {
			JarInputStream jar = new JarInputStream(ModelInstaller.class.getResourceAsStream(sJarName));
			JarEntry je = null; 
			while ( (je=jar.getNextJarEntry())!=null ) {
	            File fileTarget = new File(sRootDir+File.separator+je.getName().replaceAll("/", File.separator));
				if ( je.isDirectory() ) {
					fileTarget.mkdirs();
				} else {
					FileOutputStream fos = new FileOutputStream(fileTarget);
	                byte [] baBuf = new byte[READ_WRITE_CHUNK_SIZE];
	                int len;
	                while ((len = jar.read(baBuf)) > 0) {
	                    fos.write(baBuf, 0, len);
	                }
	                fos.close();
	            }
	        }
		} catch (Throwable t) {
			deleteDir(new File(sRootDir));
			return false;
		}
       
		return true;
	}
	
	
	/**  Deletes all files and subdirectories under dir.
	 *   Returns true if all deletions were successful. 
	 *   If a deletion fails, the method stops attempting to delete and returns false.
	 *   
	 * @param dir The directory to delete
	 * @return True if it was properly cleaned, false otherwise
	 */
    protected static boolean deleteDir(File dir) {
    	if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
    	if (dir.exists())
            return dir.delete();
    	else
    		return true;
    }
	
//    public static void main ( String [] sArgs ) {
//    	File run = new File("run/opennlp/models");
//    	run.mkdirs();
//    	installJar(run.toString(), "opennlp-english-models.jar", true);
//    	
//    }
}
