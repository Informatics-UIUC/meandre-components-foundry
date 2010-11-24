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

package org.seasr.meandre.components.abstracts;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.webui.ConfigurableWebUIFragmentCallback;
import org.meandre.webui.WebUIException;
import org.seasr.meandre.components.abstracts.util.ComponentUtils;
import org.seasr.meandre.support.generic.html.VelocityTemplateService;
import org.seasr.meandre.support.generic.io.JARInstaller.InstallStatus;

/**
 *
 * @author Boris Capitanu
 *
 */
public abstract class AbstractGWTWebUIComponent extends AbstractExecutableComponent implements ConfigurableWebUIFragmentCallback {

    protected VelocityContext _context;

    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        console.fine("Deploying GWT module: " + getGWTModuleName());
        String gwtDeployDir = ccp.getPublicResourcesDirectory() + File.separator + "gwt" + File.separator + getClass().getName();
        InstallStatus status = ComponentUtils.installJARContainingResource(getClass(), getGWTModuleName() + ".nocache.js", gwtDeployDir, false);

        switch (status) {
            case SUCCESS:
                console.fine("Deploy successfull");
                break;

            case SKIPPED:
                console.fine("Installation skipped - module already installed");
                break;

            case FAILED:
                throw new ComponentContextException("Failed to install GWT module at " + new File(gwtDeployDir).getAbsolutePath());
        }

        _context = VelocityTemplateService.getInstance().getNewContext();
        _context.put("ccp", ccp);
        _context.put("contextPath", getContextPath());
        _context.put("gwt", "/public/resources/gwt/" + getClass().getName());
    }

    //--------------------------------------------------------------------------------------------

    public abstract void emptyRequest(HttpServletResponse response) throws WebUIException;

    public abstract void handle(HttpServletRequest request, HttpServletResponse response) throws WebUIException;

    //--------------------------------------------------------------------------------------------

    public abstract String getGWTModuleName();
}
