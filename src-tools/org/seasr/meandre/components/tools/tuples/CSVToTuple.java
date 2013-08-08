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

package org.seasr.meandre.components.tools.tuples;

import java.io.StringReader;
import java.util.Map;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

/**
 *
 * @author Mike Haberman
 * @author Boris Capitanu
 *
 */

@Component(
        name = "CSV To Tuple",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#TRANSFORM, tools, text, tuple, csv",
        description = "This component converts CSV data to tuples. Each row in the CSV represents a new tuple. " +
                "It does not allow missing (null) values in the CSV data." ,
        dependency = { "protobuf-java-2.2.0.jar", "super-csv-2.1.0.jar" }
)
public class CSVToTuple extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TEXT,
            description = "The the text to be parsed into tuples.  Each line is a new tuple." +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_TEXT = Names.PORT_TEXT;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TUPLES,
            description = "The set of tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String OUT_TUPLES = Names.PORT_TUPLES;

    @ComponentOutput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            description = "The column names/labels to be used (comma separated - do not use a comma as part of the field name). " +
                    "The values set here override the labels read from the data header (if a data header exists). " +
                    "The labels are required if the CSV data has no header! The number of labels needs to match the number of columns in the CSV data.",
            name = "labels",
            defaultValue = ""
    )
    protected static final String PROP_LABELS = "labels";

    @ComponentProperty(
            description = "Does the CSV data contain a header? The header labels from the CSV will be replaced " +
                    "with the values from the 'labels' property if the value of that property is not empty",
            name = Names.PROP_HEADER,
            defaultValue = "true"
    )
    protected static final String PROP_HEADER = Names.PROP_HEADER;

       //--------------------------------------------------------------------------------------------


    protected String[] _fieldNames = null;
    protected boolean _hasHeader = false;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _hasHeader = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_HEADER, ccp));
        String labels = getPropertyOrDieTrying(PROP_LABELS, true, !_hasHeader, ccp);
        if (labels.length() > 0)
            _fieldNames = labels.split(",");
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        String text = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT))[0];

        final StringReader csvData = new StringReader(text);
        ICsvMapReader csvReader = null;
        try {
            csvReader = new CsvMapReader(csvData, CsvPreference.EXCEL_PREFERENCE);

            // the header columns are used as the keys to the Map
            String[] header;
            if (_hasHeader) {
                header = csvReader.getHeader(true);
                if (_fieldNames != null)
                    header = _fieldNames;
            } else
                header = _fieldNames;

            final CellProcessor[] processors = new CellProcessor[header.length];
            for (int i = 0; i < header.length; i++)
                processors[i] = new NotNull();  // do not allow null values in the CSV data  (TODO: we may have to revisit this decision)

            SimpleTuplePeer outPeer = new SimpleTuplePeer(header);
            StringsArray.Builder tuplesBuilder = StringsArray.newBuilder();

            Map<String, Object> csvEntry;
            while ((csvEntry = csvReader.read(header, processors)) != null) {
                SimpleTuple tuple = outPeer.createTuple();
                for (Map.Entry<String, Object> entry : csvEntry.entrySet())
                    tuple.setValue(entry.getKey(), entry.getValue().toString());

                tuplesBuilder.addValue(tuple.convert());
            }

            cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
            cc.pushDataComponentToOutput(OUT_TUPLES, tuplesBuilder.build());
        }
        finally {
            if (csvReader != null)
                csvReader.close();
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
