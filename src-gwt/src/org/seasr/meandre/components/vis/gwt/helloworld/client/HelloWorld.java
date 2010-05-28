package org.seasr.meandre.components.vis.gwt.helloworld.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class HelloWorld implements EntryPoint {

    public void onModuleLoad() {
        final String contextPath = DOM.getElementById("__component_contextPath").getAttribute("content");
        final Person person = getData();

        Button btnShowDebugInfo = new Button("Show debug info");
        btnShowDebugInfo.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                final DialogBox dialogBox = new DialogBox(false, true);
                dialogBox.setText("Debug info");
                dialogBox.setAnimationEnabled(true);

                Button closeBtn = new Button("Close");
                closeBtn.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        dialogBox.hide();
                    }
                });

                VerticalPanel vp = new VerticalPanel();
                vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
                vp.add(new Label("GWT.getModuleBaseURL(): " + GWT.getModuleBaseURL()));
                vp.add(new Label("GWT.getHostPageBaseURL(): " + GWT.getHostPageBaseURL()));
                vp.add(new Label("GWT.getModuleName(): " + GWT.getModuleName()));
                vp.add(new Label("GWT.getVersion(): " + GWT.getVersion()));
                vp.add(new Label("Location.getHref(): " + Location.getHref()));
                vp.add(new Label("Location.getQueryString(): " + Location.getQueryString()));
                vp.add(new Label("executionInstanceId: " + contextPath));


                vp.add(closeBtn);
                vp.setCellHorizontalAlignment(closeBtn, HasHorizontalAlignment.ALIGN_RIGHT);

                dialogBox.setWidget(vp);
                dialogBox.center();
                dialogBox.show();
            }
        });


        Button btnSayHello = new Button("Say <b>Hello</b>!");
        btnSayHello.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                final DialogBox dialogBox = new DialogBox(false, true);
                dialogBox.setText("Greetings!");
                dialogBox.setAnimationEnabled(true);

                VerticalPanel vp = new VerticalPanel();
                vp.setSpacing(4);
                dialogBox.setWidget(vp);

                vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
                vp.add(new Label("Hello " + person.getFirstName() + " " + person.getLastName()));
                vp.add(new Label("You are " + person.getAge() + " years old"));

                Button closeBtn = new Button("Close");
                closeBtn.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        dialogBox.hide();
                    }
                });

                vp.add(closeBtn);
                vp.setCellHorizontalAlignment(closeBtn, HasHorizontalAlignment.ALIGN_RIGHT);

                dialogBox.center();
                dialogBox.show();
            }
        });

        Button btnGetMessage = new Button("Get message");
        btnGetMessage.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, contextPath + "?action=getMessage");

                try {
                    @SuppressWarnings("unused")
                    Request request = builder.sendRequest(null, new RequestCallback() {
                        public void onError(Request request, Throwable exception) {
                            Window.alert("Fail: " + exception.getMessage());
                        }

                        public void onResponseReceived(Request request, Response response) {
                            if (response.getStatusCode() == 200) {
                                Window.alert("Message: " + response.getText());
                            } else
                                Window.alert("Error: " + response.getStatusText());
                        }
                    });
                } catch (RequestException e) {
                    Window.alert(e.getMessage());
                }
            }
        });

        HorizontalPanel hp = new HorizontalPanel();
        hp.setWidth("100%");
        hp.setSpacing(10);
        hp.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        hp.add(btnSayHello);
        hp.add(btnGetMessage);
        hp.add(btnShowDebugInfo);


        RootPanel.get().add(hp);
    }

    private final native Person getData() /*-{
        for (win = window.parent; win != top; win = win.parent) {
            if (typeof(win.__data) != "undefined")
              return win.__data;
        }

        if (typeof(top.__data) == "undefined") {
            alert("Could not find the data");
            return null;
        }

        return top.__data;
    }-*/;
}
