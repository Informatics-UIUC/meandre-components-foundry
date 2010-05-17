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

package org.seasr.meandre.components.vis.clustering;

import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Material;
import javax.media.j3d.Screen3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.Licenses;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.datamining.table.ExampleTable;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.discovery.cluster.ClusterModel;
import org.seasr.meandre.support.components.discovery.cluster.TableCluster;

import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;


@Component(
        creator = "Lily Dong",
        description = "Clustering visualization.",
        name = "Clustering Viz",
        rights = Licenses.UofINCSA,
        tags = "clustering",
        dependency = {"vecmath-1.3.1.jar", "j3d-core-1.3.1.jar", "j3d-core-utils-1.3.1.jar", "protobuf-java-2.2.0.jar"},
        baseURL = "meandre://seasr.org/components/foundry/"
)
public class ClusteringViz extends AbstractExecutableComponent {

	//------------------------------ INPUTS ------------------------------

	@ComponentInput(
	        name = Names.PORT_CLUSTER_MODEL,
			description = "The input model" +
			    "<br>TYPE: org.seasr.meandre.support.components.discovery.cluster.ClusterModel"
	)
	protected static final String IN_CLUSTER_MODEL = Names.PORT_CLUSTER_MODEL;

	//------------------------------ OUTPUTS ------------------------------

    @ComponentOutput(
            name = Names.PORT_RAW_DATA,
            description = "The image." +
                          "<br>TYPE: byte[]"
    )
    protected final static String OUT_IMAGE_RAW = Names.PORT_RAW_DATA;

    //--------------------------------------------------------------------------------------------


	//color pattern
	private static final boolean[][] rgb = {
			{true,  false, false},  //1, 0, 0
			{false, false, true},   //0, 1, 0
			{false, true,  false},  //0, 0, 1
			{true,  false, true},   //1, 0, 1
			{false, true,  true} ,  //0, 1, 1
			{true,  true,  false}}; //1, 1, 0

	//x and y coordinates
	double[] x, y;

	//distance between root and child
	double[] dist;

	//maximum distance between root and child
 	double[] maxDist;

	//color values
	float[] red, green, blue;

	//root centroids
	double[] rootCentroids;

	//color pattern selected
	int colorIndex;

	//features selected
	int[] features;


    //--------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	}

	@SuppressWarnings("unchecked")
    @Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		Object input = cc.getDataComponentFromInput(IN_CLUSTER_MODEL);
		ClusterModel model = (ClusterModel)input;

		double minX = Double.MAX_VALUE,
			   minY = Double.MAX_VALUE,
		       maxX = Double.MIN_VALUE,
		       maxY = Double.MIN_VALUE;

		ExampleTable et = (ExampleTable)model.getTable();
		features = et.getInputFeatures();

		if(features.length<2)
			throw new Exception("At least two input features must be present to visualizate clustering process.");

		for(int row=0; row<model.getNumRows(); row++) {
			double value = model.getDouble(row, features[0]);
			minX = (minX > value)? value: minX;
			maxX = (maxX > value)? maxX: value;
			value = model.getDouble(row, features[1]);
			minY = (minY > value)? value: minY;
			maxY = (maxY > value)? maxY: value;
		}

		x = new double[model.getNumRows()];
		y = new double[model.getNumRows()];

		red   = new float[model.getNumRows()];
		green = new float[model.getNumRows()];
		blue  = new float[model.getNumRows()];

		dist = new double[model.getNumRows()];

		ArrayList clusters = model.getClusters();

		maxDist = new double[clusters.size()];
		for(int i=0; i<clusters.size(); i++) {
			TableCluster root = (TableCluster)clusters.get(i);
			maxDist[i] = Double.MIN_VALUE;
			rootCentroids = root.getCentroid();
			traverse(root, i);
		}

		for(int i=0; i<clusters.size(); i++) {
			TableCluster root = (TableCluster)clusters.get(i);
			colorIndex = i%rgb.length;
			traverse(root, maxDist[i]);
		}

		double diffX = maxX - minX,
			   diffY = maxY - minY;
		double transX = ((minX+maxX)/2-minX)/diffX,
			   transY = ((minY+maxY)/2-minY)/diffY;
		for(int row=0; row<model.getNumRows(); row++) {
			x[row] = (model.getDouble(row, features[0])-minX)/diffX-transX;
			y[row] = (model.getDouble(row, features[1])-minY)/diffY-transY;
 		}

		clustering();
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

	/**
	 *
	 * @param x coordinate
	 * @param y coordinate
	 * @return transform
	 */
	private TransformGroup createTG(float x, float y){
		float z = 0;
		Vector3f position = new Vector3f(x, y, z);
        Transform3D translate = new Transform3D();
        translate.set(position);
        TransformGroup trans = new TransformGroup(translate);
        return trans;
    }

	/**
	 *
	 * @param dColor diffuse color
	 * @param aColor ambient color
	 * @param sColor specular color
	 * @param shine  material's shininess
	 * @return appearance
	 */
    private Appearance createMatAppear(
    		Color3f dColor, Color3f aColor, Color3f sColor,
    		float shine) {
    	Appearance appear = new Appearance();

        Material material = new Material();

		material.setDiffuseColor(dColor);
		material.setAmbientColor(aColor);
        material.setSpecularColor(sColor);
        material.setShininess(shine);

		TransparencyAttributes attributes =
			new TransparencyAttributes(TransparencyAttributes.BLENDED, 0.4f);

		appear.setTransparencyAttributes(attributes);
		appear.setMaterial(material);

        return appear;
    }

    /**
     * visualize clustering process
     */
  	private void clustering() {
    	int width  = 700,
    	    height = 700;

  		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();

		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		ImageComponent2D ic = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, bi);
		ic.setCapability(ImageComponent2D.ALLOW_IMAGE_WRITE);

    	Canvas3D canvas3D = new Canvas3D(config, true);

		canvas3D.setOffScreenBuffer(ic);
		Screen3D screen3D = canvas3D.getScreen3D();
		screen3D.setSize(width, height);
		screen3D.setPhysicalScreenWidth(0.0254/90.0*width);
		screen3D.setPhysicalScreenHeight(0.0254/90.0*height);

    	BranchGroup scene = new BranchGroup();

		for(int row=0; row<x.length; row++) {
			float r = red[row],
			      g = green[row],
			      b = blue[row];

			Color3f specularColor = new Color3f(r, g, b);
	    	Color3f diffuseColor  = new Color3f(r, g, b);
	    	Color3f ambientColor  = new Color3f(r, g, b);

			TransformGroup trans = createTG((float)x[row], (float)y[row]);
			scene.addChild(trans);
			trans.addChild(new Sphere(0.04f, Sphere.GENERATE_NORMALS, 20,
                    	createMatAppear(diffuseColor, ambientColor, specularColor, 1.0f)));
		}

    	AmbientLight lightA = new AmbientLight();
    	lightA.setInfluencingBounds(new BoundingSphere(new Point3d(0,0,0), Math.sqrt(2)));
    	scene.addChild(lightA);

    	DirectionalLight lightD = new DirectionalLight();
    	lightD.setInfluencingBounds(new BoundingSphere());
    	Vector3f direction = new Vector3f(0f, 0f, -1.0f);
    	direction.normalize();
    	lightD.setDirection(direction);
    	lightD.setColor(new Color3f(0.0f, 0.0f, 0.0f));
    	scene.addChild(lightD);

    	Background background = new Background();
    	background.setApplicationBounds(new BoundingSphere());
    	background.setColor(0.0f, 0.0f, 0.0f);
    	scene.addChild(background);

    	SimpleUniverse u = new SimpleUniverse(canvas3D);
    	u.getViewingPlatform().setNominalViewingTransform();
    	u.getViewer().getView().setLocalEyeLightingEnable(true);

   		u.addBranchGraph(scene);

		try {
			canvas3D.renderOffScreenBuffer();
			canvas3D.waitForOffScreenRendering();

			BufferedImage image = canvas3D.getOffScreenBuffer().getImage();
			/*FileOutputStream fos = new FileOutputStream(new File("result.png"));
	    		ImageIO.write(image, "png", fos);
			fos.flush();
			fos.close();*/

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    ImageIO.write(image, "png", baos);
		    baos.flush();
		    baos.close();

	 		componentContext.pushDataComponentToOutput(OUT_IMAGE_RAW, baos.toByteArray());
	    }catch(Exception e) {}
	}

  	/**
  	 *
  	 * @param root cluster root
  	 * @param k the kth cluster
  	 */
  	private void traverse(TableCluster root, int k) {
  		TableCluster lc = root.getLC();
		TableCluster rc = root.getRC();

		if(lc != null) {
			if(lc.isLeaf()) {
				double[] childCentroids = lc.getCentroid();
				double theDist = 0;
				for(int i=0; i<2; i++) {//the first features are used to calculate distance
					theDist += Math.pow(rootCentroids[i]-childCentroids[i], 2);
				}
				theDist = Math.sqrt(theDist);
				maxDist[k] = (theDist>maxDist[k])? theDist: maxDist[k];
				dist[lc.getMemberIndices()[0]] = theDist;
			} else
				traverse(lc, k);
		}

		if(rc != null) {
			if(rc.isLeaf()) {
				double[] childCentroids = rc.getCentroid();
				double theDist = 0;
				for(int i=0; i<2; i++) { //the first features are used to calculate distance
					theDist += Math.pow(rootCentroids[i]-childCentroids[i], 2);
				}
				theDist = Math.sqrt(theDist);
				maxDist[k] = (theDist>maxDist[k])? theDist: maxDist[k];
				dist[rc.getMemberIndices()[0]] = theDist;
			} else
				traverse(rc, k);
		}
  	}

  	/**
  	 *
  	 * @param root cluster root
  	 * @param max maximum distance between root and child
  	 */
  	private void traverse(TableCluster root, double max) {
		TableCluster lc = root.getLC();
		TableCluster rc = root.getRC();

		if(lc != null) {
			if (lc.isLeaf()) {
				int pos = lc.getMemberIndices()[0];
				float colorValue = (float)(dist[pos]/max);
				red[pos]   = (rgb[colorIndex][0])? 255f: colorValue;
				green[pos] = (rgb[colorIndex][1])? 255f: colorValue;
				blue[pos]  = (rgb[colorIndex][2])? 255f: colorValue;
			} else
				traverse(lc, max);
		}

		if(rc != null) {
			if (rc.isLeaf()) {
				int pos = rc.getMemberIndices()[0];
				float colorValue = (float)(dist[pos]/max);
				red[pos]   = (rgb[colorIndex][0])? 255f: colorValue;
				green[pos] = (rgb[colorIndex][1])? 255f: colorValue;
				blue[pos]  = (rgb[colorIndex][2])? 255f: colorValue;
			} else
				traverse(rc, max);
		}
  	}
}
