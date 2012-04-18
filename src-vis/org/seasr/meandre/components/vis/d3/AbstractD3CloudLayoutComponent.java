package org.seasr.meandre.components.vis.d3;

import java.io.File;

import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.seasr.meandre.components.abstracts.util.ComponentUtils;
import org.seasr.meandre.support.generic.io.JARInstaller.InstallStatus;

public abstract class AbstractD3CloudLayoutComponent extends AbstractD3Component {

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = "d3_cloud_api_url",
            description = "The URL to the D3 Cloud Layout API, or leave empty to use the embedded one",
            defaultValue = ""
    )
    protected static final String PROP_D3_CLOUD_API_URL = "d3_cloud_api_url";

    //--------------------------------------------------------------------------------------------


    protected static final String D3_CLOUD_API_PATH = "d3-cloud-api"; // this path is assumed to be appended to the published_resources location
    protected static final String D3_CLOUD_JS = "d3.layout.cloud.js";


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        String d3CloudAPI = getPropertyOrDieTrying(PROP_D3_CLOUD_API_URL, true, false, ccp);
        if (d3CloudAPI.length() == 0) {
            String d3CloudAPIDir = ccp.getPublicResourcesDirectory() + File.separator + D3_CLOUD_API_PATH;
            InstallStatus status = ComponentUtils.installJARContainingResource(getClass(), D3_CLOUD_JS, d3CloudAPIDir, false);
            switch (status) {
                case SKIPPED:
                    console.fine(String.format("Installation skipped - %s is already installed", D3_CLOUD_JS));
                    break;

                case FAILED:
                    throw new ComponentContextException(String.format("Failed to install %s at %s",
                            D3_CLOUD_JS, new File(d3CloudAPIDir).getAbsolutePath()));
            }

            d3CloudAPI = "/public/resources/" + D3_CLOUD_API_PATH.replaceAll("\\\\", "/") + "/" + D3_CLOUD_JS;
        }

        console.fine("Using D3 Cloud Layout API from: " + d3CloudAPI);
        context.put("d3CloudAPI", d3CloudAPI);
    }
}
