package org.seasr.meandre.support.generic.io;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public abstract class ClasspathUtils {

    /**
     * Finds a dependency
     *
     * @param depName The name of the dependency (i.e. "maxent-models.jar")
     * @param clazz The class whose class loader to search first, or null to just search in the classpath
     * @return A URL to the dependency, or null if not found
     * @throws Exception Thrown if something went wrong
     */
    @SuppressWarnings("unchecked")
    public static URL findDependencyInClasspath(String depName, Class clazz) throws Exception {
        URL depURL = null;

        if (clazz != null) {
            ClassLoader classLoader = clazz.getClassLoader();

            if (classLoader instanceof URLClassLoader) {
                URLClassLoader urlClassLoader = (URLClassLoader)classLoader;
                for (URL url : urlClassLoader.getURLs()) {
                    String jarName = url.toString().replaceAll("^jar:", "").replaceAll("!/$", "");
                    URL jarURL = new URL(jarName);
                    String fName = jarName.substring(jarName.lastIndexOf("/") + 1);
                    if (fName.equals(depName)) {
                        depURL = jarURL;
                        break;
                    }
                }
            }
        }

        if (depURL == null){
            for (String s : System.getProperty("java.class.path").split(File.pathSeparator)) {
                File f = new File(s);
                if (f.isDirectory()) continue;

                String fName = s.substring(s.lastIndexOf(File.separator) + 1);
                if (fName.equals(depName)) {
                    depURL = f.toURI().toURL();
                    break;
                }
            }
        }

        return depURL;
    }

}
