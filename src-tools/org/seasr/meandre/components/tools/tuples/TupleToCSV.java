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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.encoder.CsvEncoder;
import org.supercsv.encoder.DefaultCsvEncoder;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

/**
 *
 * @author Mike Haberman
 * @author Boris Capitanu
 *
 */

@Component(
        name = "Tuple To CSV",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#TRANSFORM, tuple, tools, text, filter",
        description = "This component writes the incoming set of tuples to CSV String" ,
        dependency = { "protobuf-java-2.2.0.jar", "super-csv-2.1.0.jar" }
)
public class TupleToCSV extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TUPLES,
            description = "The set of tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String IN_TUPLES = Names.PORT_TUPLES;

    @ComponentInput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TUPLES,
            description = "The set of tuples (same as input)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String OUT_TUPLES = Names.PORT_TUPLES;

    @ComponentOutput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for the tuples (same as input)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

    @ComponentOutput(
            name = Names.PORT_TEXT,
            description = "The CSV data" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_TEXT = Names.PORT_TEXT;

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            name = Names.PROP_HEADER,
            description = "Should the header be added to the CSV output? ",
            defaultValue = "true"
    )
    protected static final String PROP_HEADER = Names.PROP_HEADER;

    //--------------------------------------------------------------------------------------------


    protected boolean _addHeader;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _addHeader = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_HEADER, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
        SimpleTuplePeer tuplePeer = new SimpleTuplePeer(inputMeta);
        SimpleTuple tuple = tuplePeer.createTuple();

        StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
        Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);

        int size = tuplePeer.size();

        StringWriter csvData = new StringWriter();
        ICsvListWriter csvWriter = null;
        try {
            final CsvEncoder csvEncoder = new DefaultCsvEncoder();
            final CsvPreference csvPreference =
                    new CsvPreference.Builder(CsvPreference.EXCEL_PREFERENCE)
                            .useEncoder(csvEncoder).build();
            csvWriter = new CsvListWriter(csvData, csvPreference);

            String[] header = new String[size];
            CellProcessor[] processors = new CellProcessor[size];

            for (int i = 0; i < size; i++) {
                header[i] = tuplePeer.getFieldNameForIndex(i);
                processors[i] = new Optional();
            }

            if (_addHeader)
                csvWriter.writeHeader(header);

            List<String> rowData = new ArrayList<String>(size);
            for (int i = 0; i < in.length; i++) {
                tuple.setValues(in[i]);
                rowData.clear();

                for (int j = 0; j < size; j++)
                    rowData.add(tuple.getValue(j));

                csvWriter.write(rowData, processors);
            }
        }
        finally {
            if (csvWriter != null)
                csvWriter.close();
        }

        cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(csvData.toString()));
        cc.pushDataComponentToOutput(OUT_TUPLES, input);
        cc.pushDataComponentToOutput(OUT_META_TUPLE, inputMeta);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}

