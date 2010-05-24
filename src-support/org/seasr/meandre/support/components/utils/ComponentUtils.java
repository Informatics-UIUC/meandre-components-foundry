/**
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * All rights reserved.
 *
 * Developed by:
 *
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */

package org.seasr.meandre.support.components.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.seasr.meandre.support.generic.io.StreamUtils;

/**
 * This class provides utility functions for components
 *
 * @author Boris Capitanu
 *
 */
public class ComponentUtils {
    /**
     * Clones a StreamDelimiter
     *
     * @param orig The original StreamDelimiter to be cloned
     * @return The clone
     * @throws Exception Thrown if the given delimiter cannot be cloned
     */
    public static StreamDelimiter cloneStreamDelimiter(final StreamDelimiter orig) throws Exception {
        try {
            StreamDelimiter clone = orig.getClass().newInstance();
            for (String sKey : clone.keySet())
                clone.put(sKey, orig.get(sKey));

            return clone;
        }
        catch (Exception e) {
            throw new Exception("The StreamDelimiter supplied cannot be cloned");
        }
    }

    /**
     * Writes a class resource accessible via the specified class to a location in the published_resources folder
     *
     * @param clazz The class used to resolve the resource
     * @param resourceName The resource name
     * @param relativeFolderPath The relative folder (relative to the public resources directory) where to write the resource
     * @param ccp The ComponentContextProperties
     * @param overwrite True to overwrite the resource if it exists, False otherwise
     * @return The file reference to the resource
     * @throws IOException
     */
    public static File writePublicResource(Class<?> clazz, String resourceName, String relativeFolderPath,
            ComponentContextProperties ccp, boolean overwrite) throws IOException {

        File resPath = new File(ccp.getPublicResourcesDirectory() + File.separator + relativeFolderPath, resourceName);
        if (!resPath.exists() || overwrite) {
            // Ensure that the output folder exists
            resPath.getParentFile().mkdirs();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(resPath);
                StreamUtils.writeClassResourceToStream(clazz, resourceName, fos);
            }
            finally {
                if (fos != null)
                    fos.close();
            }
        }

        return resPath;
    }
}
