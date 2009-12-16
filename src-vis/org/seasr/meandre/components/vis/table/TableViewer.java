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

package org.seasr.meandre.components.vis.table;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractGWTWebUIComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.webui.WebUIException;
import org.seasr.datatypes.table.ColumnTypes;
import org.seasr.datatypes.table.MutableTable;
import org.seasr.datatypes.table.Column.SortMode;
import org.seasr.datatypes.table.basic.AbstractColumn;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.generic.html.VelocityTemplateService;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "This component provides a table viewer for a data set. " +
                      "The input can come from a Vector<Object[]> or a Table. " +
                      "If a vector is used, then each element of Vector is mapped into a row in table and " +
                      "each element of Object array is mapped into a cell in table." +
                      "This component can be used with the " +
                      "'CSV Reader' or Map2Table components.",
        name = "Table Viewer",
        tags = "table viewer",
        mode = Mode.webui,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar", "org.seasr.meandre.components.vis.gwt.tableviewer.TableViewer.jar"},
        resources = {"TableViewer.vm"}
)
public class TableViewer extends AbstractGWTWebUIComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TABLE,
            description = "This input contains the file content stored as a vector with "+
                          "each attribute (column) stored as an object array, or stored as a table." +
                          "<br>TYPE: org.seasr.datatypes.table.MutableTable"
    )
    protected static final String IN_TABLE = Names.PORT_TABLE;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_HTML,
            description = "The HTML to view" +
                "<br>TYPE: java.lang.String"
    )
    protected static final String OUT_HTML = Names.PORT_HTML;

    @ComponentOutput(
            name = Names.PORT_TABLE,
            description = "The original table, unchanged." +
                "<br>TYPE: org.seasr.datatypes.table.MutableTable"
    )
    protected static final String OUT_TABLE = Names.PORT_TABLE;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            defaultValue = "50",
            description = "The number of rows to be retrieved dynamically at once",
            name = Names.PROP_PAGE_SIZE
    )
    protected static final String PROP_PAGE_SIZE = Names.PROP_PAGE_SIZE;

    //--------------------------------------------------------------------------------------------


    protected static final String TEMPLATE = "org/seasr/meandre/components/vis/table/TableViewer.vm";
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static enum TextMatchStyle { EXACT, STARTSWITH, SUBSTRING };  // from com.smartgwt.client.types.TextMatchStyle

    private String _html;
    private boolean _done;
    private MutableTable _origTable;
    private MutableTable _table;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        _context.put("pageSize", Integer.parseInt(ccp.getProperty(PROP_PAGE_SIZE)));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        _origTable = (MutableTable)cc.getDataComponentFromInput(IN_TABLE);

        JSONArray jaColumnFormat = new JSONArray();

        for (int i = 0; i < _origTable.getNumColumns(); i++) {
            String label = _origTable.getColumnLabel(i);
            if (label.trim().length() == 0)
                label = ALPHABET.substring(i, i+1);
            int type = _origTable.getColumnType(i);
            JSONObject joColumnData = new JSONObject();
            joColumnData.put("id", "col" + i);
            joColumnData.put("label", label);
            joColumnData.put("type", type);
            jaColumnFormat.put(joColumnData);
        }

        VelocityTemplateService velocity = VelocityTemplateService.getInstance();
        _context.put("columnFormatData", jaColumnFormat.toString());

        console.finest("Applying the Velocity template");
        _html = velocity.generateOutput(_context, TEMPLATE);

        _done = false;

        cc.startWebUIFragment(this);

        cc.pushDataComponentToOutput(OUT_HTML, _html);

        while (!cc.isFlowAborting() && !_done)
            Thread.sleep(1000);

        if (cc.isFlowAborting())
            console.info("Flow abort requested - terminating component execution...");

        if (_done)
            cc.pushDataComponentToOutput(OUT_TABLE, _origTable);

        cc.stopWebUIFragment(this);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {

    }

    //--------------------------------------------------------------------------------------------

    @Override
    public void emptyRequest(HttpServletResponse response) throws WebUIException {
        console.entering(getClass().getName(), "emptyRequest", response);

// NOTE: Uncomment the following lines to make this component a standalone WebUI that does not need HTMLViewer to be displayed
//             try {
//                 response.getWriter().println(_html);
//             } catch (Exception e) {
//                 throw new WebUIException(e);
//             }

        console.exiting(getClass().getName(), "emptyRequest");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws WebUIException {
        console.entering(getClass().getName(), "handle", response);

        String reqPath = request.getPathInfo();
        console.fine("Request path: " + reqPath);
        console.fine("query string: " + request.getQueryString());

        if (request.getParameterMap().size() == 0) {
            console.exiting(getClass().getName(), "handle");
            return;
        }

        String operation;

        if (request.getParameter("done") != null) {
            _done = true;
            try {
                response.getWriter().println("<html><head><meta http-equiv='REFRESH' content='1;url=/'></head><body></body></html>");
            }
            catch (IOException e) {
                throw new WebUIException(e);
            }
        }

        else

        if ((operation = request.getParameter("_operationType")) != null) {
            console.fine("operation: " + operation);

            if (operation.equals("fetch")) {
                try {
                    _table = (MutableTable)_origTable.copy();

                    int startRow = Integer.parseInt(request.getParameter("_startRow"));
                    int endRow = Integer.parseInt(request.getParameter("_endRow"));
                    TextMatchStyle textMatchStyle = getTextMatchStyle(request.getParameter("_textMatchStyle"));

                    // retrieve any column search filters
                    Map<Integer, String> filterColumns = new HashMap<Integer, String>();
                    Enumeration e = request.getParameterNames();
                    while (e.hasMoreElements()) {
                        String param = (String)e.nextElement();
                        if (param.startsWith("col"))
                            filterColumns.put(Integer.parseInt(param.substring(3)), request.getParameter(param));
                    }

                    if (filterColumns.size() > 0) {
                        Stack<Integer> rowsToRemove = new Stack<Integer>();
                        for (int i = 0, iMax = _table.getNumRows(); i < iMax; i++) {
                            int matches = 0;
                            for (Entry<Integer, String> entry : filterColumns.entrySet())
                                if (matches(_table.getColumn(entry.getKey()).getRow(i), entry.getValue(), textMatchStyle))
                                    matches++;

                            if (matches != filterColumns.size())
                                rowsToRemove.push(i);
                        }

                        for (int i = 0, iMax = rowsToRemove.size(); i < iMax; i++)
                            _table.removeRow(rowsToRemove.pop());
                    }

                    int totalRows = _table.getNumRows();

                    String sortBy = request.getParameter("_sortBy");  // contains the name of the column to be sorted; if prefixed by "-" then descending sort is requested
                    if (sortBy != null) {
                        SortMode sortMode = SortMode.ASCENDING;
                        if (sortBy.startsWith("-")) {
                            sortMode = SortMode.DESCENDING;
                            sortBy = sortBy.substring(1);
                        }
                        int sortColumn = Integer.parseInt(sortBy.substring(3));  // sortBy = "col3" or "col2"...etc
                        ((AbstractColumn)_table.getColumn(sortColumn)).sort(_table, 0, _table.getNumRows(), sortMode);
                    }

                    endRow = Math.min(endRow, totalRows);

                    int status = 0;

                    JSONArray joData = new JSONArray();

                    for (int i = startRow, iMax = endRow; i < iMax; i++) {
                        JSONObject joRow = new JSONObject();
                        for (int j = 0, jMax = _table.getNumColumns(); j < jMax; j++) {
                            // skip byte array columns because they can't be displayed
                            if (_table.getColumnType(j) == ColumnTypes.BYTE_ARRAY)
                                continue;
                            joRow.put("col" + j, _table.getObject(i, j));
                        }
                        joData.put(joRow);
                    }

                    JSONObject joResponse = new JSONObject();
                    joResponse.put("status", status);
                    joResponse.put("startRow", startRow);
                    joResponse.put("endRow", endRow);
                    joResponse.put("totalRows", totalRows);
                    joResponse.put("data", joData);

                    JSONObject jo = new JSONObject();
                    jo.put("response", joResponse);

                    String jsonData = jo.toString();
                    console.finest(jsonData);

                    response.setContentType("application/json");
                    response.setContentLength(jsonData.length());
                    response.getWriter().write(jsonData);
                    response.flushBuffer();
                }
                catch (Exception e) {
                    throw new WebUIException(e);
                }
            }

            else {
                try {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    console.exiting(getClass().getName(), "handle");
                    return;
                }
                catch (IOException e) {
                    throw new WebUIException(e);
                }
            }
        }

        else
            emptyRequest(response);

        console.exiting(getClass().getName(), "handle");
    }

    //--------------------------------------------------------------------------------------------

    public String getContextPath() {
        return "/tableviewer";
    }

    @Override
    public String getGWTModuleJARName() {
        return "org.seasr.meandre.components.vis.gwt.tableviewer.TableViewer.jar";
    }

    //--------------------------------------------------------------------------------------------

    private TextMatchStyle getTextMatchStyle(String sTextMatchStyle) {
        TextMatchStyle textMatchStyle = TextMatchStyle.SUBSTRING;

        if (sTextMatchStyle.equalsIgnoreCase("exact"))
            textMatchStyle = TextMatchStyle.EXACT;

        else

        if (sTextMatchStyle.equalsIgnoreCase("startsWith"))
            textMatchStyle = TextMatchStyle.STARTSWITH;

        return textMatchStyle;
    }

    private boolean matches(Object data, String value, TextMatchStyle matchStyle) throws Exception {
        switch (matchStyle) {
            case SUBSTRING:
                return data.toString().toLowerCase().indexOf(value.toLowerCase()) > 0;

            case EXACT:
                if (data instanceof Number) {
                    if (data instanceof Float || data instanceof Double) {
                        Double val = Double.parseDouble(value);
                        return data.equals(val);
                    }

                    Long val = Long.parseLong(value);
                    return data.equals(val);
                }

                else

                if (data instanceof Boolean)
                    return data.equals(Boolean.parseBoolean(value));

                else

                return data.toString().equalsIgnoreCase(value);

            case STARTSWITH:
                return data.toString().toLowerCase().startsWith(value.toLowerCase());
        }

        throw new Exception("Unknown text match style specified: " + matchStyle);
    }
}
