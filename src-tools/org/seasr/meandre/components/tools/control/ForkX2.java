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

package org.seasr.meandre.components.tools.control;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.components.utils.ComponentUtils;

/**
 * <p>Title: Fork Times Two</p>
 *
 * <p>Description: This component takes in any Java object and distributes
 * it (or copies of it) across multiple outputs.  The user can choose from five
 * object replication methods -- by reference, shallow copy by clone, deep copy
 * via serialization, copy via constructor, or copy via custom method.</p>
 *
 * <p>Copyright: UIUC Copyright (c) 2007</p>
 *
 * <p>Company: Automated Learning Group at NCSA, UIUC</p>
 *
 * @author Duane Searsmith
 * @author Lily Dong
 * @author Boris Capitanu
 */

@Component(
        creator = "Lily Dong",
        description = "<p>Description: This component takes in any Java object" +
                      "and distributes it (or copies of it) across multiple outputs.  " +
                      "The user can choose from five object replication methods -- by reference, " +
                      "shallow copy by clone, deep copy via serialization, copy via constructor, " +
                      "or copy via custom method.</p>",
        name = "Fork x2",
        tags = "clone, fork, copy",
        rights = Licenses.UofINCSA,
        baseURL="meandre://seasr.org/components/foundry/"
)
public class ForkX2 extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            description = "Object to replicate",
            name = Names.PORT_OBJECT
    )
    protected static final String IN_OBJECT = Names.PORT_OBJECT;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            description = "Replicated object",
            name = Names.PORT_OBJECT
    )
    protected static final String OUT_OBJECT = Names.PORT_OBJECT;

    @ComponentOutput(
            description = "Replicated object",
            name = Names.PORT_OBJECT_2
    )
    protected static final String OUT_OBJECT_2 = Names.PORT_OBJECT_2;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            defaultValue = "0",
            description = "Replication mode (0 = by reference, 1 = clone shallow, 2 = serialize deep, 3 = constructor, 4 = custom)",
            name = Names.PROP_REPLICATION_MODE
    )
    protected static final String PROP_REPLICATION_MODE = Names.PROP_REPLICATION_MODE;

    @ComponentProperty(
            defaultValue = "",
            description = "Custom replication method name",
            name = Names.PROP_REPLICATION_METHOD
    )
    protected static final String PROP_REPLICATION_METHOD = Names.PROP_REPLICATION_METHOD;

    //--------------------------------------------------------------------------------------------


    //Property values for "Replication Mode".
    static public final int s_REFERENCE = 0;
    static public final int s_CLONE_SHALLOW = 1;
    static public final int s_SERIALIZE_DEEP = 2;
    static public final int s_CONSTRUCTOR = 3;
    static public final int s_CUSTOM = 4;

    private String fn;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        fn = ccp.getProperty(PROP_REPLICATION_MODE);
        if (fn == null || fn.length() == 0)
            throw new ComponentExecutionException("No replication mode given.");
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        int repMode = Integer.parseInt(fn);
        Object data = cc.getDataComponentFromInput(IN_OBJECT);
        switch (repMode) {
            case 0: //REFERENCE
                cc.pushDataComponentToOutput(OUT_OBJECT, data);
                cc.pushDataComponentToOutput(OUT_OBJECT_2, data);
                break;

            case 1: //CLONE Shallow Copy

                Object obj = makeClone(data);
                cc.pushDataComponentToOutput(OUT_OBJECT, data);
                cc.pushDataComponentToOutput(OUT_OBJECT_2, obj);
                break;

            case 2: //SERIALIZE Deep Copy

                obj = null;
                obj = makeDeepCopy(data);
                cc.pushDataComponentToOutput(OUT_OBJECT, data);
                cc.pushDataComponentToOutput(OUT_OBJECT_2, obj);
                break;

            case 3: //CONSTRUCTOR

                obj = null;
                obj = copyViaConstructor(data);
                cc.pushDataComponentToOutput(OUT_OBJECT, data);
                cc.pushDataComponentToOutput(OUT_OBJECT_2, obj);
                break;

            case 4: //CUSTOM

                String meth = cc.getProperty(PROP_REPLICATION_METHOD);
                obj = null;
                obj = copyViaCustomMethod(data, meth);
                cc.pushDataComponentToOutput(OUT_OBJECT, data);
                cc.pushDataComponentToOutput(OUT_OBJECT_2, obj);
                break;

            default:
                throw new Exception("No anticipated replication mode matches requested value.");
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators() throws Exception {
        StreamInitiator si = (StreamInitiator)componentContext.getDataComponentFromInput(IN_OBJECT);
        componentContext.pushDataComponentToOutput(OUT_OBJECT, si);
        componentContext.pushDataComponentToOutput(OUT_OBJECT_2, ComponentUtils.cloneStreamDelimiter(si));
    };

    @Override
    protected void handleStreamTerminators() throws Exception {
        StreamTerminator st = (StreamTerminator)componentContext.getDataComponentFromInput(IN_OBJECT);
        componentContext.pushDataComponentToOutput(OUT_OBJECT, st);
        componentContext.pushDataComponentToOutput(OUT_OBJECT_2, ComponentUtils.cloneStreamDelimiter(st));
    };

    //--------------------------------------------------------------------------------------------

    /**
     * Need to use reflection here because "Cloneable" interface is
     * a marker interface (i.e. it does not define the method "clone").
     * The incoming objects must still implement the cloneable interface
     * however, otherwise a CloneNotSupported error will be thrown.
     *
     * Also, the clone method in Object (which is protected) must be
     * overriden and declared public, otherwise the reflection methods
     * will fail since they can only access public methods by default.
     *
     * @param dat Object Object to clone.
     * @return Object Cloned object.
     * @throws Exception Reflection exception.
     */
    Object makeClone(Object dat) throws Exception {
        return dat.getClass().getMethod("clone", (Class[])null).invoke(dat, (Object[])null);
    }

    /**
     * These objects must be serializable.  If they are not then
     * when we attempt serialization an error will be generated.
     *
     * @param dat Object Object to copy.
     * @return Object Copied object.
     * @throws Exception IO Exception
     */
    Object makeDeepCopy(Object dat) throws Exception {
        Object obj = null;
        // Write the object out to a byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(dat);
        out.flush();
        out.close();

        // Make an input stream from the byte array and read
        // a copy of the object back in.
        ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray()));
        obj = in.readObject();
        return obj;
    }

    /**
     * These objects must supply a publicly accessible constructor
     * that takes the same class type object as input.  Otherwise,
     * the reflection calls will generate an error.
     *
     * @param dat Object Object to copy.
     * @return Object Copied object.
     * @throws Exception Reflection exception.
     */
    @SuppressWarnings("unchecked")
	Object copyViaConstructor(Object dat) throws Exception{
        Object obj = null;
        Class cls = dat.getClass();
        Class[] clses = new Class[] {cls};
        Object[] objs = new Object[] {dat};
        obj = cls.getConstructor(clses).newInstance(objs);
        return obj;
    }

    /**
     * These objects must supply a publicly accessible method, the
     * name of which matches the supplied string via property
     * s_CUSTOM_COPY_METHOD_NAME, and returns an object (copy) of the same type.
     * Otherwise, the reflection calls will generate an error.
     *
     * @param dat Object Object being copied.
     * @param meth String Name of copy method.
     * @return Object Copy of object.
     * @throws Exception Reflection exception.
     */
    @SuppressWarnings("unchecked")
	Object copyViaCustomMethod(Object dat, String meth) throws Exception {
        Object obj = null;
        if (meth == null || meth.length() == 0) {
            throw new RuntimeException(
                    "No custom method name provided.");
        }
        Class cls = dat.getClass();
        obj = dat.getClass().getMethod(meth, (Class[])null).invoke(dat, (Object[])null);
        if (!(cls.isInstance(obj))) {
            throw new RuntimeException("Copy produced is not an " +
                                       "instance of the input object.");
        }
        return obj;
    }
}
