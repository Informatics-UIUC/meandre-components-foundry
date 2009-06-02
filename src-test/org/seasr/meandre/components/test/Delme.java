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


package org.seasr.meandre.components.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ContentHandler;
import java.net.ContentHandlerFactory;
import java.net.URI;
import java.net.URLConnection;
import java.security.InvalidParameterException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.seasr.meandre.support.io.handlers.GenericContentHandlerFactory;
import org.seasr.meandre.support.parsers.DataTypeParser;


/**
 * @author capitanu
 *
 */
public class Delme {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0)
            throw new InvalidParameterException();

        InputStream stream = Delme.class.getResourceAsStream("opennlp-english-models.jar");
        if (stream == null) throw new RuntimeException("stream null");
        JarInputStream jar = new JarInputStream(stream);
        JarEntry je = null;
        while ( (je=jar.getNextJarEntry())!=null ) {
            File fileTarget = new File("/tmp/bla/"+File.separator+je.getName().replaceAll("/", File.separator));
            if ( je.isDirectory() ) {
                fileTarget.mkdirs();
            } else {
                FileOutputStream fos = new FileOutputStream(fileTarget);
                byte [] baBuf = new byte[65535];
                int len;
                while ((len = jar.read(baBuf)) > 0) {
                    fos.write(baBuf, 0, len);
                }
                fos.close();
            }
        }

        System.out.println("stream created");

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        URLConnection.setContentHandlerFactory(new GenericContentHandlerFactory());

        while (true) {
            System.out.print("location: ");
            String location = in.readLine();
            if (location.trim().length() == 0) {
                System.out.println();
                continue;
            }

            URI uri = DataTypeParser.parseAsURI(location);

            URLConnection conn = uri.toURL().openConnection();
            Object result;

            try {
                result = conn.getContent();
            }
            catch (Exception e) {
                System.err.println(e.toString());
                System.err.flush();

                System.out.println();
                System.out.println();
                continue;
            }


            System.out.println(String.format("Content type: %s", conn.getContentType()));
            System.out.println(String.format("Content length: %s", conn.getContentLength()));
            System.out.println(String.format("Object type: %s", result.getClass().getName()));
        }
    }
}
