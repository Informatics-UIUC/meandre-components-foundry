package org.seasr.meandre.components;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.meandre.annotations.CreateComponentDescriptor;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.engine.Conductor;
import org.meandre.core.engine.Executor;
import org.meandre.core.engine.MrProbe;
import org.meandre.core.engine.probes.NullProbeImpl;
import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.repository.FlowDescription;
import org.meandre.core.repository.RepositoryImpl;
import org.meandre.webui.PortScroller;
import org.meandre.webui.WebUI;
import org.meandre.zigzag.console.NullOuputStream;
import org.meandre.zigzag.parser.ParseException;
import org.meandre.zigzag.parser.ZigZag;
import org.meandre.zigzag.semantic.FlowGenerator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/** The base class for performing component testing.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class ComponentTesterBase {
	
	/** The new line character */
	public final static String NEW_LINE = System.getProperty("line.separator");
	
	/** The test flows dir location */
	private String FLOWS_DIR = "."+File.separator+"test"+File.separator+"flows";
	
	/** The temporary folder location */
	private String TEMP_DIR = "."+File.separator+"tmp";
	
	/** The temporary folder for descriptors location */
	private String TEMP_DESC_DIR = "."+File.separator+"tmp"+File.separator+"desc";
	
	/** Source packages to process */
	private  String [] SRC_FOLDERS = { "src-tools" };
	
	/** The base test port */
	private int BASE_TEST_PORT = 60000;
	
	/** Creates a new component tester with defaults.
	 * 
	 */
	public ComponentTesterBase () {
		
	}
	
	/** Creates a new component tester with defaults.
	 * 
	 * @param fd The ZigZag flow's directory
	 * @param td The temporary folder
	 * @param tdd The temporary description folder
	 * @param src The array of source directories to explore
	 * @param port The base port for the tests
	 * 
	 */
	public ComponentTesterBase (String fd, String td, String tdd, String[] src, int port) {
		FLOWS_DIR = fd;
		TEMP_DIR = td;
		TEMP_DESC_DIR = tdd;
		SRC_FOLDERS = src;
		BASE_TEST_PORT = port;
	}
	
	/** Generates the descriptors for the specified folders */
	public void initialize () {
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("Initializing resources for tests...");
		// Clean the temp folder
		cleanTempFolder();
		// Get the list of java files to proces
		List<File> lst = getJavaFilesToProcess();
		// Generate the descriptors
		processJavaFiles(lst);
		System.out.println("... done");
		System.out.println("-------------------------------------------------------------------------------");
	}

	/** Process the Java files and generate the descriptors
	 * 
	 * @param lst The list of files to process
	 */
	private void processJavaFiles(List<File> lst) {
		System.out.println("\tProcessing "+lst.size()+" files...");
		CreateComponentDescriptor.setMakeSubs(true);
		new File(getTempDescriptorFolder()).mkdirs();
        for ( File file:lst ) {
			String sFile = file.toString();
			String sClassName = processFileName(sFile);
			System.out.print("\t\t"+sClassName+"..."); 
			try {
				CreateComponentDescriptor ccd = new CreateComponentDescriptor();
		        ccd.setComponentClassName(sClassName);
		        ccd.setComponentDescriptorFolderName(getTempDescriptorFolder());
		        ccd.setComponentDescriptorFile(new File(getTempDescriptorFolder()));
		        ccd.processComponentDescriptor();
		        System.out.println("\t\t done");
			}
			catch ( Exception e ) {
				System.out.println("\t\t not a component!");
			}			
		}
		System.out.println("\t... done");
	}

	/** Given a path file name return the name of the class.
	 * 
	 * @param sFile File name to process
	 * @return The class file name
	 */
	private String processFileName(String sFile) {
		for ( String src:SRC_FOLDERS )
			if ( sFile.startsWith(src) )
				sFile = sFile.substring(src.length());
		sFile = ( sFile.startsWith(File.separator)) ? sFile : File.separator+sFile;
		String sRes = sFile.substring(sFile.indexOf(File.separator)+1);
		sRes = sRes.replaceAll("\\"+".java$", "");
		sRes = sRes.replace(File.separatorChar, '.');
		return sRes;
	}

	/** Returns the list of java files contained in the source folders.
	 * 
	 * @return The list with the files
	 */
	private List<File> getJavaFilesToProcess() {
		System.out.print("\tExploring source directories [ ");
		List<File> lst = new LinkedList<File>();
		for ( String sFile:getSourceFolders() ) {
			System.out.print(sFile+" ");
			File file = new File(sFile);
			populateListWithJavaFiles(lst,file);
		}
		System.out.println("]... done");
		return lst;
	}

	/** Populates the list with the Java files.
	 * 
	 * @param lst The list to populate
	 * @param file The current file being explored
	 * @return True if succeeded, false otherwise
	 */
	private boolean populateListWithJavaFiles(List<File> lst, File file) {
		if (file.isDirectory()) {
            String[] children = file.list();
            for (int i=0; i<children.length; i++) {
                boolean success = populateListWithJavaFiles(lst, new File(file, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
		else 
			if ( file.toString().endsWith(".java"))
				lst.add(file);
		return true;		
	}

	/** Clean the temporary folder by deleting it
	 * 
	 */
	private void cleanTempFolder() {
		System.out.print("\tDeleting temp directory "+getTempFolder()+"... ");
		File fileTemp = new File(getTempFolder());
		if ( fileTemp.exists() ) 
			if ( !deleteDir(fileTemp) )
				fail("Failed to delete temporaty directory "+getTempFolder());
		fileTemp.mkdirs();
		System.out.println("done");
	}
	
	/**  Deletes all files and subdirectories under dir.
	 *   Returns true if all deletions were successful. 
	 *   If a deletion fails, the method stops attempting to delete and returns false.
	 *   
	 * @param dir The directory to delete
	 * @return True if it was properly cleaned, false otherwise
	 */
    private boolean deleteDir(File dir) {
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
        return dir.delete();
    }


    /** Create the list of descriptors available from the temp folder.
     * 
     * @return The string containing the imports
     */
    private List<String> getDescriptorFiles () {
    	List<String> lst = new LinkedList<String>();
    	File dir = new File(getTempDescriptorFolder());
    	for ( String sFile:dir.list() ) {
    		File here = new File("");
    		String sRDFFile = here.getAbsolutePath()+getTempDescriptorFolder().substring(1)+File.separator+sFile;
    		try {
				URL fileURL = new URL("file:////"+sRDFFile);
				lst.add(fileURL.toString().replaceAll(" ","%20"));
			} catch (MalformedURLException e) {
				fail(e.toString());
			}
    	}
    	return lst;
    }
    
    /** Create the list of imports available from the temp folder.
     * 
     * @return The string containing the imports
     */
    private String getImports () {
    	StringBuffer sb = new StringBuffer();
    	for ( String sURL:getDescriptorFiles() )
    		sb.append("import <"+sURL+">"+NEW_LINE);
    	sb.append(NEW_LINE);
    	return sb.toString();
    }

    /** Given a ZigZag name it returns the script with the proper imports added.
     * 
     * @param sFileName The ZigZag script name
     * @return The ZgZag script
     */
    public String getZigZag ( String sFileName ) {
    	String sImports = getImports();
    	
    	StringBuffer sb = new StringBuffer(sImports);
    	
    	String sFile = getFlowsFolder()+File.separator+sFileName;
    	try {
			LineNumberReader lnr = new LineNumberReader(new FileReader(sFile));
			String sLine = null;
			while ( (sLine=lnr.readLine())!=null )
				sb.append(sLine+NEW_LINE);
		} catch (FileNotFoundException e) {
			fail(e.toString());
		} catch (IOException e) {
			fail(e.toString());
		}
    	
    	return sb.toString();
    }
    
    /** Runs a ZigZag script trapping the output and error streams.
     * 
     * @param sZigZag The ZigZag script
     * @param out The output stream
     * @param err The error stream
     */
    public void runZigZag ( String sZigZag, ByteArrayOutputStream out, ByteArrayOutputStream err ) {
    	PrintStream psOut = new PrintStream(out);
    	PrintStream psErr = new PrintStream(err);
    	
    	// Parse the ZigZag
    	FlowDescription flow = null;
    	RepositoryImpl ri = null;
    	try {
    		flow = generateFlowDescriptor(sZigZag);
            ri = new RepositoryImpl(flow.getModel()); 
		} catch (Throwable t) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
        	t.printStackTrace(new PrintStream(baos));
			fail(baos.toString());
		} 
		
		// Add all the descriptor files
		Model modRep = ri.getModel();
		addComponentDescriptorsToModel(modRep);
		ri.refreshCache(modRep);
		
    	// Set the streams
    	PrintStream origOUT = System.out; PrintStream origERR = System.err;
    	System.setOut(psOut); System.setErr(psErr); 
    	
    	CoreConfiguration cnf = new CoreConfiguration(BASE_TEST_PORT,".");
    	Conductor conductor = new Conductor(Conductor.DEFAULT_QUEUE_SIZE,cnf);
    	MrProbe mrProbe = new MrProbe(KernelLoggerFactory.getCoreLogger(),new NullProbeImpl(),false,false);
        try {
			String sFUID = flow.getFlowComponent().toString()+"/"+System.currentTimeMillis();
			Executor exec = conductor.buildExecutor(ri, flow.getFlowComponent(), mrProbe,System.out,sFUID);
			int nextPort = PortScroller.getInstance(cnf).nextAvailablePort(exec.getFlowUniqueExecutionID());
            WebUI webui = exec.initWebUI(nextPort,sFUID);
			exec.execute(webui);
        } catch (Throwable t) {
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        	t.printStackTrace(new PrintStream(baos));
			fail(baos.toString());
		} 
    	
    	// Reset the streams
    	System.setOut(origOUT); System.setErr(origERR);    	
    }

	/** Add all the generated component descriptors to the model
	 * 
	 * @param modRep The model to populate
	 */
	private void addComponentDescriptorsToModel(Model modRep) {
		for ( String sFile:getDescriptorFiles() ) {
			Model model = ModelFactory.createDefaultModel();
			try {
				model.read(new URL(sFile).openStream(), null);
				modRep.add(model);
			} catch (Throwable t) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        	t.printStackTrace(new PrintStream(baos));
				fail(baos.toString());
			} 
		}
	}

	/**
	 * @param sZigZag
	 * @return
	 * @throws ParseException
	 */
	private FlowDescription generateFlowDescriptor(String sZigZag)
			throws ParseException {
		FlowDescription flow;
		FlowGenerator fg = new FlowGenerator();
		fg.setPrintStream(new PrintStream(new NullOuputStream()));
		fg.init(null);
		ZigZag parser = new ZigZag(new StringReader(sZigZag));
		parser.setFlowGenerator(fg);
		parser.start();
		flow = fg.getFlowDescription(""+System.currentTimeMillis(),true);
		return flow;
	}
    
	/** Print the coverage reports */
	public void printCoverageReport () {
		// Clean the temp folder
		System.out.println("-------------------------------------------------------------------------------");
		runCoverageOfFlows();
		System.out.println("-------------------------------------------------------------------------------");
	}
	
    /** Generates the descriptors for the specified folders */
	public void destroy () {
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("Tearing down the resources used in tests");
		cleanTempFolder();
		System.out.println("-------------------------------------------------------------------------------");
	}

	/** Returns a list of the basic ZigZag files.
	 * 
	 * @return The list of ZigZag
	 */
	private List<File> getZigZagFiles () {
		List<File> lst = new LinkedList<File>();
		File dir = new File(FLOWS_DIR);
		String[] children = dir.list();
        for (int i=0; i<children.length; i++) {
            if ( children[i].toString().endsWith(".zz") )
            	lst.add(new File(children[i]));            	
        }
		
		return lst;
	}
	
	/** Computes basic component coverage by the test flows
	 * 
	 */
	private void runCoverageOfFlows() {
		System.out.println("Analysing component coverage...");
		Model model = createRepositoryForCoverageAnalays();
		
		// Get the components available
		String QUERY_COMPONENTS =
	             "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
	             "PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
	             "SELECT ?component  " +
	             "WHERE { " +
	             "     ?component rdf:type meandre:executable_component " +
	             "}" ;

         Map<Resource,Integer> hsRes = new Hashtable<Resource,Integer>();
         Query query = QueryFactory.create(QUERY_COMPONENTS) ;
         QueryExecution exec = QueryExecutionFactory.create(query, model, null);
         ResultSet results = exec.execSelect();

         while ( results.hasNext() )
              hsRes.put(results.nextSolution().getResource("component"),0);
         
         // Get components used in flows
 		String QUERY_COMPONENT_INSTANCES =
 	             "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
 	             "PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
 	             "SELECT ?component  " +
 	             "WHERE { " +
 	             "     ?eci meandre:instance_resource ?component " +
 	             "}" ;

 		Query queryIns = QueryFactory.create(QUERY_COMPONENT_INSTANCES) ;
	    QueryExecution execIns = QueryExecutionFactory.create(queryIns, model, null);
	    ResultSet resultsIns = execIns.execSelect();
	
	    int iTotal = 0;
	    while ( resultsIns.hasNext() ) {
	    	  Resource res = resultsIns.nextSolution().getResource("component");
	          hsRes.put(res,hsRes.get(res)+1);
	          iTotal++;
	    }

        int iUsed = 0;
        int iComps = 0;
        System.out.println("\tComponent usage");
        for ( Resource res:hsRes.keySet() ) {
			int iCount = hsRes.get(res);
			float fPercent = (iTotal>0) ? 100*((float)iCount)/iTotal : 0 ;
			String sMsg = (iCount==0) ? "(!!)" : "OK  ";
			iUsed = ( iCount==0 ) ? iUsed : iUsed+1;
			System.out.println("\t\t"+sMsg+" "+res+"\t\tused "+iCount+" times ("+fPercent+"%)");
			iComps++;
		}
		
        System.out.println("\tComponent coverage");
        if ( iUsed!=iComps )
			System.out.println("\t\t(!!) Only "+iUsed+"/"+iComps+" ("+(((float)iUsed)/iComps)+"%) components used on the provided test flows");
		else
			System.out.println("\t\tAll components used on the provided test flows");
		
        System.out.println("...done");
	     		
	}

	/** Creates a repository containing all the components and flows to allow
	 * coverage analysis
	 * 
	 * @return The generated model
	 * 
	 */
	private Model createRepositoryForCoverageAnalays() {
		Model model = ModelFactory.createDefaultModel();
		// Add the flows
		List<File> lst = getZigZagFiles();
		for ( File f:lst ) {
			String[] saFile = f.toString().split(File.separator);
			String sZigZag = getZigZag(saFile[saFile.length-1]);
			try {
				FlowDescription fd = generateFlowDescriptor(sZigZag);
				model.add(fd.getModel());
			} catch (Throwable t) {
	        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        	t.printStackTrace(new PrintStream(baos));
				fail(baos.toString());
			} 			
		}
		
		// Add the components
		addComponentDescriptorsToModel(model);
		
		return model;
	}

	/** Set the flows folder
	 * 
	 * @param fLOWS_DIR the fLOWS_DIR to set
	 */
	public void setFlowsFolder(String fLOWS_DIR) {
		FLOWS_DIR = fLOWS_DIR;
	}

	/** Gets the flows folder.
	 * 
	 * @return the fLOWS_DIR
	 */
	public String getFlowsFolder() {
		return FLOWS_DIR;
	}

	/** Set the temporary folder
	 * 
	 * @param tEMP_DIR the tEMP_DIR to set
	 */
	public void setTempFolder(String tEMP_DIR) {
		TEMP_DIR = tEMP_DIR;
	}

	/** Get the temporary folder
	 * 
	 * @return the tEMP_DIR
	 */
	public String getTempFolder() {
		return TEMP_DIR;
	}

	/** Set the temp descriptors folder
	 * 
	 * @param tEMP_DESC_DIR the tEMP_DESC_DIR to set
	 */
	public void setTempDescriptorFolder(String tEMP_DESC_DIR) {
		TEMP_DESC_DIR = tEMP_DESC_DIR;
	}

	/** Get the temp descriptors folder
	 * 
	 * @return the tEMP_DESC_DIR
	 */
	public String getTempDescriptorFolder() {
		return TEMP_DESC_DIR;
	}

	/** Set source folder.
	 * 
	 * @param sRC_FOLDERS the sRC_FOLDERS to set
	 */
	public void setSourceFolders(String [] sRC_FOLDERS) {
		SRC_FOLDERS = sRC_FOLDERS;
	}

	/** Get source folder.
	 * 
	 * @return the sRC_FOLDERS
	 */
	public String [] getSourceFolders() {
		return SRC_FOLDERS;
	}

	/** Set the base port for the execution
	 * 
	 * @param bASE_TEST_PORT the bASE_TEST_PORT to set
	 */
	public void setBaseTestPort(int bASE_TEST_PORT) {
		BASE_TEST_PORT = bASE_TEST_PORT;
	}

	/** Get the base port for the execution
	 * 
	 * @return the bASE_TEST_PORT
	 */
	public int getBaseTestPort() {
		return BASE_TEST_PORT;
	}
	
}
