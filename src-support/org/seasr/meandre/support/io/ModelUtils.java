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

package org.seasr.meandre.support.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.seasr.meandre.components.tools.ModelVocabulary;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;

/**
 * This class provides a set of utility functions for dealing with Models
 *
 * @author Boris Capitanu
 *
 */
public abstract class ModelUtils {

    /**
     * Creates one (or loads one or more existing) model(s) with the content specified
     *
     * @param sModel The model content
     * @param baseURI The base URI to use for the model, or null if none
     * @param modelArgs An optional set of models that should be populated with the specified content
     * @return The new model created (if no Model arguments were specified), or null if Model arguments were specified
     * @throws IOException Thrown if the content cannot be loaded into the model(s)
     */
    public static Model getModel(String sModel, String baseURI, Model... modelArgs)
        throws IOException {

        Model[] models = (modelArgs.length == 0) ?
                new Model[] { ModelFactory.createDefaultModel() } : modelArgs;

        for (Model model : models)
            readModelFromString(model, sModel, baseURI);

        return (modelArgs.length == 0) ? models[0] : null;
    }

    /**
     * Creates one (or loads one or more existing) model(s) with the content specified
     *
     * @param modelStream The stream to be used as content for the model
     * @param baseURI The base URI to use for the model, or null if none
     * @param modelArgs An optional set of models that should be populated with the specified content
     * @return The new model created (if no Model arguments were specified), or null if Model arguments were specified
     * @throws IOException Thrown if the content cannot be loaded into the model(s)
     */
    public static Model getModel(InputStream modelStream, String baseURI, Model... modelArgs)
        throws IOException {

        Model[] models = (modelArgs.length == 0) ?
                new Model[] { ModelFactory.createDefaultModel() } : modelArgs;

        for (Model model : models)
            readModelFromStream(model, modelStream, baseURI);

        return (modelArgs.length == 0) ? models[0] : null;
    }

    /**
     * Creates one (or loads one or more existing) model(s) with the content specified
     *
     * @param modelBytes The byte array carrying the model content
     * @param baseURI The base URI to use for the model, or null if none
     * @param modelArgs An optional set of models that should be populated with the specified content
     * @return The new model created (if no Model arguments were specified), or null if Model arguments were specified
     * @throws IOException Thrown if the content cannot be loaded into the model(s)
     */
    public static Model getModel(byte[] modelBytes, String baseURI, Model... modelArgs)
        throws IOException {

        return getModel(new ByteArrayInputStream(modelBytes), baseURI, modelArgs);
    }

    /**
     * Creates one (or loads one or more existing) model(s) with the content specified
     *
     * @param uri The location from where to pull the model content
     * @param baseURI The base URI to use for the model, or null if none
     * @param modelArgs An optional set of models that should be populated with the specified content
     * @return The new model created (if no Model arguments were specified), or null if Model arguments were specified
     * @throws IOException Thrown if the content cannot be loaded into the model(s)
     */
    public static Model getModel(URI uri, String baseURI, Model... modelArgs) throws IOException {
        return getModel(StreamUtils.getInputStreamForResource(uri), baseURI, modelArgs);
    }

    /**
     * Attempts to load textual content into a model
     *
     * @param model The model to be loaded
     * @param sModel The textual content
     * @param baseURI An optional single argument specifying the base URI
     * @throws IOException Thrown if the content cannot be loaded into the model
     */
    public static void readModelFromString(Model model, String sModel, String... baseURI)
        throws IOException {

        if (baseURI.length > 1)
            throw new IllegalArgumentException("baseURI can only be specified once");

        String uri = (baseURI.length == 1) ? baseURI[0] : null;

        try {
            model.read(sModel, uri, "RDF/XML");
        }
        catch (Exception eRDF) {
            try {
                model.read(sModel, uri, "TTL");
            }
            catch (Exception eTTL) {
                try {
                    model.read(sModel, uri, "N-TRIPLE");
                }
                catch (Exception eNT) {
                    IOException ioe = new IOException("Cannot read model in any dialect!");
                    ioe.setStackTrace(eRDF.getStackTrace());
                    throw ioe;
                }
            }
        }
    }

    /**
     * Attempts to load a model from an InputStream
     *
     * @param model The model to be loaded
     * @param modelStream The stream to be used as content for the model
     * @param baseURI An optional single argument specifying the base URI
     * @throws IOException Thrown if the content cannot be loaded into the model
     */
    public static void readModelFromStream(Model model, InputStream modelStream, String... baseURI)
        throws IOException {

        if (baseURI.length > 1)
            throw new IllegalArgumentException("baseURI can only be specified once");

        String uri = (baseURI != null && baseURI.length == 1) ? baseURI[0] : null;

        try {
            model.read(modelStream, uri, "RDF/XML");
        }
        catch (Exception eRDF) {
            try {
                model.read(modelStream, uri, "TTL");
            }
            catch (Exception eTTL) {
                try {
                    model.read(modelStream, uri, "N-TRIPLE");
                }
                catch (Exception eNT) {
                    IOException ioe = new IOException("Cannot read model in any dialect!");
                    ioe.setStackTrace(eRDF.getStackTrace());
                    throw ioe;
                }
            }
        }
    }

   /**
    * Converts the model to a string.
    *
    * @param model The model to read
    * @return The dialect version of the model
    */
   public static String modelToDialect(Model model, String dialect) {
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       model.write(baos, dialect);
       return baos.toString();
   }

   /**
    * Extract the text of a model.
    *
    * @param model The model to use
    * @return The text contained in the model
    */
   public static String extractTextFromModel(Model model) {
       StringBuffer sbBuffer = new StringBuffer();
       NodeIterator modelObjects = model.listObjectsOfProperty(ModelVocabulary.text);

       while ( modelObjects.hasNext() ) {
           Literal node = (Literal)modelObjects.nextNode();
           sbBuffer.append(node.getValue().toString());
           sbBuffer.append(" ");
       }

       return sbBuffer.toString();
   }
}
