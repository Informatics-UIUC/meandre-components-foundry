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

package org.seasr.meandre.components.vis.gwt.inputdata.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.DOM;
import com.smartgwt.client.types.Encoding;
import com.smartgwt.client.types.Side;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ButtonItem;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.TextAreaItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.UploadItem;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tab.events.TabSelectedEvent;
import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;

/**
 *
 * @author Boris Capitanu
 *
 */

public class InputData implements EntryPoint {

    public void onModuleLoad() {
        final String contextPath = DOM.getElementById("__component_contextPath").getAttribute("content");

        final Tab[] selectedTabs = new Tab[1];

        final TabSet tabSet = new TabSet();
        tabSet.setTabBarPosition(Side.TOP);
        tabSet.addTabSelectedHandler(new TabSelectedHandler() {
            public void onTabSelected(TabSelectedEvent event) {
                selectedTabs[0] = event.getTab();
            }
        });

        final VLayout vLayout = new VLayout();
        vLayout.setWidth100();
        vLayout.setHeight100();

        final DynamicForm formUrls = new DynamicForm();
        formUrls.setWidth100();
        formUrls.setHeight100();
        formUrls.setAction(contextPath + "?action=urls");
        final FormItem[] fiUrls = new FormItem[getMaxUrlCount() + 1];
        for (int i = 0, iMax = getMaxUrlCount(); i < iMax; i++) {
            TextItem tiUrl = new TextItem();
            tiUrl.setName("url_" + (i+1));
            tiUrl.setTitle("URL");
            fiUrls[i] = tiUrl;
            if (i > 0)
                tiUrl.setVisible(false);
        }
        final ButtonItem biAddUrl = new ButtonItem();
        biAddUrl.setTitle("Add URL");
        biAddUrl.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler() {
            public void onClick(com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
                for (FormItem fi : fiUrls)
                    if (Boolean.FALSE.equals(fi.getVisible())) {
                        fi.show();
                        break;
                    }
            }
        });
        fiUrls[fiUrls.length-1] = biAddUrl;
        formUrls.setFields(fiUrls);

        final DynamicForm formText = new DynamicForm();
        formText.setNumCols(2);
        formText.setWidth100();
        formText.setHeight100();
        formText.setAction(contextPath + "?action=text");
        TextAreaItem taText = new TextAreaItem();
        taText.setName("text");
        if (getMaxTextLength() > 0)
            taText.setLength(getMaxTextLength());
        taText.setColSpan(2);
        taText.setWidth("*");
        taText.setHeight("*");
        taText.setShowTitle(false);
        formText.setFields(taText);

        final DynamicForm formFiles = new DynamicForm();
        formFiles.setWidth100();
        formFiles.setHeight100();
        formFiles.setEncoding(Encoding.MULTIPART);
        formFiles.setAction(contextPath + "?action=upload");
        final FormItem[] fiFiles = new FormItem[getMaxFileCount() + 1];
        for (int i = 0, iMax = getMaxFileCount(); i < iMax; i++) {
            UploadItem uiFile = new UploadItem();
            uiFile.setName("file_" + (i+1));
            uiFile.setTitle("File");
            fiFiles[i] = uiFile;
            if (i > 0)
                uiFile.setVisible(false);
        }
        final ButtonItem biAddFile = new ButtonItem();
        biAddFile.setTitle("Add file");
        biAddFile.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler() {
            public void onClick(com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
                for (FormItem fi : fiFiles)
                    if (Boolean.FALSE.equals(fi.getVisible())) {
                        fi.show();
                        break;
                    }
            }
        });
        fiFiles[fiFiles.length-1] = biAddFile;
        formFiles.setItems(fiFiles);

        IButton btnAddUrl = new IButton("Submit");
        btnAddUrl.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                DynamicForm selectedForm = (DynamicForm)selectedTabs[0].getPane();
                selectedForm.submitForm();
            }
        });

        Tab tabUrls = new Tab("URLs");
        tabUrls.setPane(formUrls);

        Tab tabText = new Tab("Text");
        tabText.setPane(formText);

        Tab tabFiles = new Tab("Files");
        tabFiles.setPane(formFiles);

        tabSet.setTabs(tabUrls, tabText, tabFiles);

        vLayout.addMember(tabSet);
        vLayout.addMember(btnAddUrl);

        vLayout.draw();
    }

    private final native int getMaxUrlCount() /*-{
        return window.parent.__maxUrlCount;
    }-*/;

    private final native int getMaxFileCount() /*-{
        return window.parent.__maxFileCount;
    }-*/;

    private final native int getMaxTextLength() /*-{
        return window.parent.__maxTextLength;
    }-*/;
}
