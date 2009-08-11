package org.seasr.meandre.support.sentiment;



public class PathMetric {
	
	public String start;
	public String end;
	
	public String concept = "";
	public int numberOfPaths;
	public int depthFound;
	public int unique;
	public float percentSymmetric;
	
	//public int commonWords;
	// public int depthOfCommonWords;
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("concept ").append(concept).append("\n");
		sb.append("start ").append(start).append("\n");
		sb.append("end ").append(end).append("\n");
		sb.append("# of paths ").append(numberOfPaths).append("\n");
		sb.append("depth ").append(depthFound).append("\n");
		sb.append("unique nodes ").append(unique).append("\n");
		sb.append("% sym ").append(percentSymmetric).append("\n");
		return sb.toString();
	}
	
	public void setPaths(int numberOfPaths, int numberSymmetric)
	{
		this.numberOfPaths = numberOfPaths;
		this.percentSymmetric = 0;
		
		if (numberOfPaths > 0) {
			this.percentSymmetric = (float)numberSymmetric/(float)numberOfPaths;
		}
	}
	
	public static PathMetric min(PathMetric a, PathMetric b) 
	{
		// edge cases
		if (a.numberOfPaths == 0 && b.numberOfPaths == 0) 
			return a;// either one is fine, both are empty
		if (a.numberOfPaths == 0 && b.numberOfPaths > 0)
			return b;
		if (b.numberOfPaths == 0 && a.numberOfPaths > 0)
			return a;
		
		// lower is better
		if (a.depthFound < b.depthFound) return a;
		if (b.depthFound < a.depthFound) return b;
		
		// higher is better
		if (a.numberOfPaths < b.numberOfPaths) return b;
		if (b.numberOfPaths < a.numberOfPaths) return a;
		
		// higher is better
		if (a.unique < b.unique) return b;
		if (b.unique < a.unique) return a;
		
		// higher is better
		if (a.percentSymmetric < b.percentSymmetric) return b;
		if (b.percentSymmetric < a.percentSymmetric) return a;
		
		// totally equal, look at common words next
		return a;
	}

}
