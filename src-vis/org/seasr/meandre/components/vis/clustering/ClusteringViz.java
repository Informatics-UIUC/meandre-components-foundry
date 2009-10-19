package org.seasr.meandre.components.vis.clustering;

import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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

import org.json.JSONObject;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.Licenses;
import org.meandre.components.abstracts.AbstractExecutableComponent;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.meandre.components.tools.Names;

import org.meandre.components.datatype.table.ExampleTable;
import org.meandre.components.discovery.cluster.support.ClusterModel;
import org.meandre.components.discovery.cluster.support.TableCluster;

import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;


@Component(
        creator = "Lily Dong",
        description = "Clustering visualization.",
        name = "Clustering Viz",
        rights = Licenses.UofINCSA,
        tags = "clustering",
        dependency = {"protobuf-java-2.2.0.jar"},
        baseURL = "meandre://seasr.org/components/tools/"
)

public class ClusteringViz extends AbstractExecutableComponent {
	//------------------------------ INPUTS ------------------------------

	@ComponentInput(
			description = "The input model",
			name = Names.PORT_OBJECT
	)
	protected static final String IN_OBJECT = Names.PORT_OBJECT;

	//------------------------------ OUTPUTS ------------------------------

    @ComponentOutput(
            description = "The image." +
                          "<br>TYPE: Bytes",
            name = Names.PORT_RAW_DATA
    )
    protected final static String OUT_IMAGE_RAW = Names.PORT_RAW_DATA;


	private ClusterModel _cmodel = null;

	private Map<Integer, ClusteringViz.ClusterNode> _clustMap =
		new HashMap<Integer, ClusteringViz.ClusterNode>();

	//color pattern
	private static final boolean[][] rgb = {
			{true,  false, false}, //1, 0, 0
			{false, false,  true}, //0, 1, 0
			{false, true, false} , //0, 0, 1
			{true,  true,  false}, //1, 1, 0
			{false, true,  true} , //0, 1, 1
			{true,  false, true}}; //1, 0, 1

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

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	}

	@Override
	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		Object input = cc.getDataComponentFromInput(IN_OBJECT);
		_cmodel = (ClusterModel)input;

		TableCluster root = _cmodel.getRoot();
		TreeSet<ClusteringViz.ClusterNode> ranked = new TreeSet<ClusteringViz.ClusterNode>(
				new cRank_Comparator());
		clusterWalk(root, ranked);

		Iterator<ClusteringViz.ClusterNode> itty = ranked.iterator();
		while (itty.hasNext()) {
			ClusteringViz.ClusterNode cn = itty.next();

			JSONObject obj = new JSONObject(cn.getMap());
			//console.info("jsonobject = " + obj.toString());
			try {
				_clustMap.put(Integer.valueOf(obj.getInt("rid")), cn);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		double minX = Double.MAX_VALUE,
			   minY = Double.MAX_VALUE,
		       maxX = Double.MIN_VALUE,
		       maxY = Double.MIN_VALUE;

		ExampleTable et = (ExampleTable)_cmodel.getTable();
		int[] features = et.getInputFeatures();

		for(int row=0; row<_cmodel.getNumRows(); row++) {
			double value = _cmodel.getDouble(row, features[0]);
			minX = (minX > value)? value: minX;
			maxX = (maxX > value)? maxX: value;
			value = _cmodel.getDouble(row, features[1]);
			minY = (minY > value)? value: minY;
			maxY = (maxY > value)? maxY: value;
		}

		x = new double[_cmodel.getNumRows()];
		y = new double[_cmodel.getNumRows()];

		red   = new float[_cmodel.getNumRows()];
		green = new float[_cmodel.getNumRows()];
		blue  = new float[_cmodel.getNumRows()];

		dist = new double[_cmodel.getNumRows()];

		ArrayList clusters = _cmodel.getClusters();

		maxDist = new double[clusters.size()];

		for(int i=0; i<clusters.size(); i++) {
			TableCluster tc = (TableCluster)clusters.get(i);
			maxDist[i] = Double.MIN_VALUE;
			rootCentroids = tc.getCentroid();
			traverse(tc, i);
		}

		for(int i=0; i<clusters.size(); i++) {
			TableCluster tc = (TableCluster)clusters.get(i);
			colorIndex = i%rgb.length;
			traverse(tc, maxDist[i]);
		}

		double diffX = maxX - minX,
			   diffY = maxY - minY;
		double transX = ((minX+maxX)/2-minX)/diffX,
			   transY = ((minY+maxY)/2-minY)/diffY;
		for(int row=0; row<_cmodel.getNumRows(); row++) {
			x[row] = (_cmodel.getDouble(row, features[0])-minX)/diffX-transX;
			y[row] = (_cmodel.getDouble(row, features[1])-minY)/diffY-transY;
 		}

		clustering();
	}

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

    	// This will move the ViewPlatform back a bit so the
   		// objects in the scene can be viewed.
    	u.getViewingPlatform().setNominalViewingTransform();

    	// setLocalEyeViewing
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

		if(lc.isLeaf()) {
			double[] childCentroids = lc.getCentroid();
			int len = childCentroids.length;
			if(len > 2) //features at 0 and 1 are used to calculate distance
				len = 2;
			double theDist = 0;
			for(int i=0; i<len; i++) {
				theDist += Math.pow(rootCentroids[i]-childCentroids[i], 2);
			}
			theDist = Math.sqrt(theDist);
			maxDist[k] = (theDist>maxDist[k])? theDist: maxDist[k];
			dist[lc.getMemberIndices()[0]] = theDist;
		} else {
			traverse(lc, k);
		}

		if(rc.isLeaf()) {
			double[] childCentroids = rc.getCentroid();
			int len = childCentroids.length;
			if(len > 2) //features at 0 and 1 are used to calculate distance
				len = 2;
			double theDist = 0;
			for(int i=0; i<len; i++) {
				theDist += Math.pow(rootCentroids[i]-childCentroids[i], 2);
			}
			theDist = Math.sqrt(theDist);
			maxDist[k] = (theDist>maxDist[k])? theDist: maxDist[k];
			dist[rc.getMemberIndices()[0]] = theDist;
		} else {
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

		if (lc.isLeaf()) {
			int pos = lc.getMemberIndices()[0];
			float colorValue = (float)(dist[pos]/max);
			red[pos]   = (rgb[colorIndex][0])? 255f: colorValue;
			green[pos] = (rgb[colorIndex][1])? 255f: colorValue;
			blue[pos]  = (rgb[colorIndex][2])? 255f: colorValue;
		} else
			traverse(lc, max);

		if (rc.isLeaf()) {
			int pos = rc.getMemberIndices()[0];
			float colorValue = (float)(dist[pos]/max);
			red[pos]   = (rgb[colorIndex][0])? 255f: colorValue;
			green[pos] = (rgb[colorIndex][1])? 255f: colorValue;
			blue[pos]  = (rgb[colorIndex][2])? 255f: colorValue;
		} else
			traverse(rc, max);
  	}

	private ClusteringViz.ClusterNode clusterWalk(
			TableCluster root,
			Set<ClusteringViz.ClusterNode> hold) {

			TableCluster lc = root.getLC();
			TableCluster rc = root.getRC();

			ClusteringViz.ClusterNode lcNode = null;
			ClusteringViz.ClusterNode rcNode = null;
			if (lc.isLeaf()) {
				lcNode = new ClusteringViz.ClusterNode(lc, null, null, lc
						.getChildDistance());
				hold.add(lcNode);
			} else {
				lcNode = clusterWalk(lc, hold);
			}
			if (rc.isLeaf()) {
				rcNode = new ClusteringViz.ClusterNode(rc, null, null, rc
						.getChildDistance());
				hold.add(rcNode);
			} else {
				rcNode = clusterWalk(rc, hold);
			}
			ClusterNode ret = new ClusterNode(root, lcNode, rcNode, root
					.getChildDistance());
			hold.add(ret);
			return ret;
	}


	// ===============
	// Inner Classes
	// ===============

	private class ClusterNode {
		private TableCluster _root = null;

		private ClusterNode _lc = null;

		private ClusterNode _rc = null;

		private double _cdist = 0;

		private Map<String, Object> _map = null;

		public ClusterNode(TableCluster root, ClusterNode lc, ClusterNode rc,
				double cdist) {
			super();
			_map = new HashMap<String, Object>();
			_root = root;
			_map.put("rid", Integer.valueOf(_root.hashCode()));
			_lc = lc;
			if (lc != null) {
				_map.put("lc", Double.valueOf(lc.getRoot().hashCode()));
			}
			_rc = rc;
			if (rc != null) {
				_map.put("rc", Double.valueOf(rc.getRoot().hashCode()));
			}
			_cdist = cdist;
			_map.put("cd", Double.valueOf(_cdist));
		}

		public Map<String, Object> getMap() {
			return _map;
		}

		public boolean isLeaf() {
			return _root.isLeaf();
		}

		public TableCluster getRoot() {
			return _root;
		}

		public double getChildDistance() {
			return _cdist;
		}

		public ClusterNode getLC() {
			return _lc;
		}

		public ClusterNode getRC() {
			return _rc;
		}
	}

	private class Centroid_Comparator implements Comparator<Object[]> {

		/**
		 * The small deviation allowed in double comparisons.
		 */
		public Centroid_Comparator() {
		}

		public int compare(Object[] objarr1, Object[] objarr2) {
			double d1 = ((Double) objarr1[1]).doubleValue();
			double d2 = ((Double) objarr2[1]).doubleValue();

			if (d1 < d2) {
				return 1;
			} else {
				return -1;
			}
		} // end method compare

		public boolean equals(Object o) {
			return false;
		}
	} // end class cRank_Comparator

	private class cRank_Comparator implements
			Comparator<ClusteringViz.ClusterNode> {

		/**
		 * The small deviation allowed in double comparisons.
		 */
		public cRank_Comparator() {
		}

		public int compare(ClusteringViz.ClusterNode objarr1,
				ClusteringViz.ClusterNode objarr2) {

			if (objarr1.getChildDistance() > objarr2.getChildDistance()) {
				return 1;
			} else {
				return -1;
			}
		} // end method compare

		public boolean equals(Object o) {
			return false;
		}
	} // end class cRank_Comparator

}
