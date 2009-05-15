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

package org.seasr.meandre.support.parsers;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.IntegersMap;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.meandre.support.exceptions.UnsupportedDataTypeException;

/**
 * @author Boris Capitanu
 */
public class DataTypeParser {
    /**
     * Attempts to convert the given data to a String
     *
     * @param data The data
     * @return The String
     * @throws UnsupportedDataTypeException Thrown if the data is in an unsupported format
     */
    public static String parseAsString(Object data) throws UnsupportedDataTypeException {
        String text;

        if (data == null)
            text = null;

        else

        if (data instanceof Strings)
            text = BasicDataTypesTools.stringsToStringArray((Strings)data)[0];

        else

        if (data instanceof String)
            text = (String)data;

        else

        if (data instanceof byte[])
            text = new String((byte[])data);

        else
            throw new UnsupportedDataTypeException(data.getClass().toString());

        return text;
    }

    /**
     * Attempts to convert the given data to a URI
     *
     * @param data The data
     * @return The URI
     * @throws URISyntaxException Thrown if a URI cannot be created due to syntax error in the input data
     * @throws UnsupportedDataTypeException Thrown if the data is in an unsupported format
     */
    public static URI parseAsURI(Object data) throws URISyntaxException, UnsupportedDataTypeException {
        URI uri;

        if (data == null)
            uri = null;

        else

        if (data instanceof URI)
            uri = (URI)data;

        else

        if (data instanceof URL)
            uri = ((URL)data).toURI();

        else

        if (data instanceof Strings)
            uri = new URI(BasicDataTypesTools.stringsToStringArray((Strings)data)[0]);

        else

        if (data instanceof String)
            uri = new URI((String)data);

        else
            throw new UnsupportedDataTypeException(data.getClass().toString());

        return uri;
    }

    /**
     * Attempts to convert the given data to a Map<String, Integer>
     *
     * @param data The data
     * @return The Map<String, Integer>
     * @throws UnsupportedDataTypeException Thrown if the data is in an unsupported format
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Integer> parseAsStringIntegerMap(Object data) throws UnsupportedDataTypeException {
        Map<String, Integer> map;

        if (data == null)
            map = null;

        else

        if (data instanceof IntegersMap)
            map = BasicDataTypesTools.IntegerMapToMap((IntegersMap)data);

        else

        if (data instanceof Map)
            map = (Map<String, Integer>)data;

        else
            throw new UnsupportedDataTypeException(data.getClass().toString());

        return map;
    }
}
