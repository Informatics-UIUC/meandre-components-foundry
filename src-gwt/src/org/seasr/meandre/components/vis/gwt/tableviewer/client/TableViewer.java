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

package org.seasr.meandre.components.vis.gwt.tableviewer.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.DOM;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.RestDataSource;
import com.smartgwt.client.data.fields.DataSourceBooleanField;
import com.smartgwt.client.data.fields.DataSourceFloatField;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.DSDataFormat;
import com.smartgwt.client.widgets.grid.ListGrid;

/**
 * @author Boris Capitanu
 */
public class TableViewer implements EntryPoint {

    public void onModuleLoad() {
        final String contextPath = DOM.getElementById("__component_contextPath").getAttribute("content");

        DataSource dataSource = new RestDataSource();
        dataSource.setDataFormat(DSDataFormat.JSON);
        dataSource.setDataURL(contextPath);

        JsArray<TableColumnFormat> f = getTableFormat();
        for (int i = 0, iMax = f.length(); i < iMax; i++) {
            TableColumnFormat columnFormat = f.get(i);
            // skip byte array columns because they cannot be displayed
            if (columnFormat.getType() == ColumnTypes.BYTE_ARRAY)
                continue;

            dataSource.addField(getFieldForColumn(columnFormat));
        }

        ListGrid listGrid = new ListGrid();
        listGrid.setDataSource(dataSource);
        listGrid.setAlternateRecordStyles(true);
        listGrid.setWidth100();
        listGrid.setHeight100();
        listGrid.setAutoFetchData(true);
        listGrid.setDataPageSize(getPageSize());
        listGrid.setCanAddFormulaFields(true);
        listGrid.setCanAddSummaryFields(true);
        listGrid.setShowFilterEditor(true);
        listGrid.setFilterOnKeypress(true);
        listGrid.setFetchDelay(500);

        listGrid.draw();
    }

    private DataSourceField getFieldForColumn(TableColumnFormat tableColumnFormat) {
        DataSourceField field = null;

        String id = tableColumnFormat.getId();
        String label = tableColumnFormat.getLabel();

        switch (tableColumnFormat.getType()) {
            case ColumnTypes.INTEGER:
                field = new DataSourceIntegerField(id, label);
                break;

            case ColumnTypes.FLOAT:
                field = new DataSourceFloatField(id, label);
                break;

            case ColumnTypes.DOUBLE:
                field = new DataSourceFloatField(id, label);
                break;

            case ColumnTypes.SHORT:
                field = new DataSourceIntegerField(id, label);
                break;

            case ColumnTypes.LONG:
                field = new DataSourceIntegerField(id, label);
                break;

            case ColumnTypes.STRING:
                field = new DataSourceTextField(id, label);
                break;

            case ColumnTypes.CHAR_ARRAY:
                field = new DataSourceTextField(id, label);
                break;

            case ColumnTypes.BYTE_ARRAY:
                // cannot display
                field = null;
                break;

            case ColumnTypes.BOOLEAN:
                field = new DataSourceBooleanField(id, label);
                break;

            case ColumnTypes.OBJECT:
                field = new DataSourceTextField(id, label);
                break;

            case ColumnTypes.BYTE:
                field = new DataSourceIntegerField(id, label);
                break;

            case ColumnTypes.CHAR:
                field = new DataSourceTextField(id, label);
                break;

            case ColumnTypes.NOMINAL:
                field = new DataSourceTextField(id, label);
                break;

            case ColumnTypes.UNSPECIFIED:
                field = new DataSourceTextField(id, label);
                break;
        }

        return field;
    }

    private final native JsArray<TableColumnFormat> getTableFormat() /*-{
        return window.parent.__columnFormatData;
    }-*/;

    private final native int getPageSize() /*-{
        return window.parent.__pageSize;
    }-*/;
}
