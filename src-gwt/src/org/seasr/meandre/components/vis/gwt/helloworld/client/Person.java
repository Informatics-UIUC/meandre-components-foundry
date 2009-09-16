package org.seasr.meandre.components.vis.gwt.helloworld.client;

import com.google.gwt.core.client.JavaScriptObject;

public class Person extends JavaScriptObject {
    // Overlay types always have protected, zero argument constructors.
    protected Person() {}

    // JSNI methods to get person info
    public final native String getFirstName() /*-{ return this.firstName; }-*/;
    public final native String getLastName() /*-{ return this.lastName; }-*/;
    public final native int getAge() /*-{ return this.age; }-*/;

    // Non-JSNI method
    public final String getFullName() {
        return getLastName() + ", " + getFirstName();
    }
}
