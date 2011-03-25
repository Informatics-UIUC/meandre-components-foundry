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

package org.seasr.meandre.components.tools.tuples;

import java.io.BufferedWriter;
import java.io.Writer;
import java.net.URI;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
import org.seasr.meandre.support.generic.io.IOUtils;
import org.seasr.meandre.support.generic.io.PathUtils;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Update Tuple Cache",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "cache",
        description = "Adds entries to the cache",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class UpdateTupleCache extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TUPLE,
            description = "The tuple" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_TUPLE = Names.PORT_TUPLE;

    @ComponentInput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for the tuple(s)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TUPLE,
            description = "The original input tuple" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_TUPLE = Names.PORT_TUPLE;

    @ComponentOutput(
            name = Names.PORT_META_TUPLE,
            description = "The original meta data for the tuple" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            description = "The cache file where to look up attribute values",
            name = "attribute_cache_file",
            defaultValue = ""
    )
    protected static final String PROP_CACHE = "attribute_cache_file";

    @ComponentProperty(
            description = "The attribute to be used as key in the cache",
            name = "key",
            defaultValue = "token"
    )
    protected static final String PROP_KEY = "key";

    @ComponentProperty(
            description = "The attribute to be used as the value for the key",
            name = "value",
            defaultValue = "concept"
    )
    protected static final String PROP_VALUE = "value";

    //--------------------------------------------------------------------------------------------


    protected Writer _writer;
    protected String _key;
    protected String _value;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _key = getPropertyOrDieTrying(PROP_KEY, ccp);
        _value = getPropertyOrDieTrying(PROP_VALUE, ccp);

        String cacheFile = getPropertyOrDieTrying(PROP_CACHE, ccp);
        String defaultDir = ccp.getPublicResourcesDirectory();
        URI cacheUri = PathUtils.relativize(new URI(cacheFile), defaultDir);
        _writer = new BufferedWriter(IOUtils.getWriterForResource(cacheUri, true));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Strings inTuple = (Strings) cc.getDataComponentFromInput(IN_TUPLE);
        Strings inMetaTuple = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);

        SimpleTuplePeer inPeer = new SimpleTuplePeer(inMetaTuple);
        SimpleTuple tuple = inPeer.createTuple();

        tuple.setValues(inTuple);

        int KEY_IDX = inPeer.getIndexForFieldName(_key);
        int VALUE_IDX = inPeer.getIndexForFieldName(_value);
        if (KEY_IDX == -1 || VALUE_IDX == -1)
            throw new ComponentExecutionException("Tuple does not have the required attributes!");

        String key = tuple.getValue(KEY_IDX);
        String value = tuple.getValue(VALUE_IDX);

        console.finer(String.format("key_idx=%d, key='%s', value_idx=%d, value='%s'", KEY_IDX, key, VALUE_IDX, value));

        String data = String.format("%s\t%s", key, value);
        for (int i = 0; i < inPeer.size(); i++) {
            if (i == KEY_IDX || i == VALUE_IDX)
                continue;
            else
                data += String.format("\t%s", tuple.getValue(i));
        }
        data += "\n";

        console.finest("Writing to cache: " + data);

        _writer.write(data);
        _writer.flush();

        cc.pushDataComponentToOutput(OUT_TUPLE, inTuple);
        cc.pushDataComponentToOutput(OUT_META_TUPLE, inMetaTuple);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        _writer.close();
        _writer = null;
    }
}
