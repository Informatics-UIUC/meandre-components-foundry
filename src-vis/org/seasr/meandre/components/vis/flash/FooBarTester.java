package org.seasr.meandre.components.vis.flash;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.webui.ConfigurableWebUIFragmentCallback;
import org.meandre.webui.WebUIException;


@Component(
        creator = "Mike Haberman",
        description = "Generates and displays a webpage via a Velocity Template ",
        name = "Foo Bar Tester",
        tags = "string, visualization",
        rights = Licenses.UofINCSA,
        mode = Mode.webui,
        baseURL = "meandre://seasr.org/components/",
        resources = {},
        dependency = { }
)
public class FooBarTester extends AbstractExecutableComponent implements
		ConfigurableWebUIFragmentCallback {

	@Override
	public void disposeCallBack(ComponentContextProperties ccp)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		// TODO Auto-generated method stub

		

        cc.startWebUIFragment(this);

        while (!cc.isFlowAborting())
            Thread.sleep(1000);

        if (cc.isFlowAborting())
            console.info("Flow abort requested - terminating component execution...");

        console.finest("Stopping WebUI fragment");

        cc.stopWebUIFragment(this);
	}

	@Override
	public void initializeCallBack(ComponentContextProperties ccp)
			throws Exception {
		// TODO Auto-generated method stub

	}

	public String getContextPath() {
		// TODO Auto-generated method stub
		return "/service/test";
	}

	public void emptyRequest(HttpServletResponse arg0) throws WebUIException {
		// TODO Auto-generated method stub
		
		console.info("empty request");

	}

	public void handle(HttpServletRequest arg0, HttpServletResponse arg1)
			throws WebUIException {
		// TODO Auto-generated method stub
		console.info("handle request");


	}

}
