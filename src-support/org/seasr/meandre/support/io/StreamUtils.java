/**
*
* University of Illinois/NCSA
* Open Source License
*
* Copyright (c) 2008, NCSA.  All rights reserved.
*
* Developed by:
* The Automated Learning Group
* University of Illinois at Urbana-Champaign
* http://www.seasr.org
*
* Permission is hereby granted, free of charge, to any person obtaining
* a copy of this software and associated documentation files (the
* "Software"), to deal with the Software without restriction, including
* without limitation the rights to use, copy, modify, merge, publish,
* distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject
* to the following conditions:
*
* Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimers.
*
* Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimers in
* the documentation and/or other materials provided with the distribution.
*
* Neither the names of The Automated Learning Group, University of
* Illinois at Urbana-Champaign, nor the names of its contributors may
* be used to endorse or promote products derived from this Software
* without specific prior written permission.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
* CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
*
*/

package org.seasr.meandre.support.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

/**
 * @author Boris Capitanu
 */
public class StreamUtils {

    /**
     * Reads the content of an InputStream into a byte array
     *
     * @param dataStream The data stream
     * @return A byte array containing the data from the data stream
     * @throws IOException Thrown if a problem occurred while reading from the stream
     */
    public static byte[] getBytesFromStream(InputStream dataStream) throws IOException {
        BufferedInputStream bufStream = new BufferedInputStream(dataStream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[4096];
        int nRead;

        do {
            nRead = bufStream.read(buffer, 0, buffer.length);
            if (nRead > 0)
                baos.write(buffer, 0, nRead);
        } while (nRead > 0);

        return baos.toByteArray();
    }

    /**
     * Returns an InputStream for the specified resource.
     *
     * @param uri The resource location (can be a URL or a local file)
     * @return The InputStream to use to read from the resource
     * @throws IOException Thrown if the resource is invalid, does not exist, or cannot be opened
     */
    public static InputStream getInputStreamForResource(URI uri) throws IOException {
        try {
            return uri.toURL().openStream();
        }
        catch (IllegalArgumentException e) {
            // URI not absolute - trying as local file
            return new FileInputStream(uri.toString());
        }
    }

    /**
     * Returns an OutputStream for the specified resource
     *
     * @param uri The resource location (specified as either file:// or local path)
     * @return The OutputStream to use to write to the resource
     * @throws IOException Thrown if the resource is invalid
     */
    public static OutputStream getOutputStreamForResource(URI uri) throws IOException {
        try {
            URL url = uri.toURL();
            if (url.getProtocol().equalsIgnoreCase("file"))
                return new FileOutputStream(url.getFile());
            else
                // TODO: add webdav support
                throw new UnsupportedOperationException("Can only write to file:// or local resources");
        }
        catch (IllegalArgumentException e) {
            // URI not absolute - trying as local file
            return new FileOutputStream(uri.toString());
        }
    }
}
