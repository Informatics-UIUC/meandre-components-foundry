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
        formUrls.setAction(contextPath + "?action=urls");
        final FormItem[] fiUrls = new FormItem[11];
        for (int i = 0, iMax = fiUrls.length-1; i < iMax; i++) {
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
        formText.setAction(contextPath + "?action=text");
        TextAreaItem taText = new TextAreaItem();
        taText.setShowTitle(false);
        formText.setFields(taText);

        final DynamicForm formFiles = new DynamicForm();
        formFiles.setEncoding(Encoding.MULTIPART);
        formFiles.setAction(contextPath + "?action=upload");
        final FormItem[] fiFiles = new FormItem[11];
        for (int i = 0, iMax = fiFiles.length-1; i < iMax; i++) {
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

}
