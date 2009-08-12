package org.seasr.meandre.support.sentiment;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class PathMetricFinder {

	// "http://localhost:8080/path/lovely/blue?format=json";
	static String host = "http://localhost:8080/";

    public static String encodeParameter(String name, List<String> list)
    throws UnsupportedEncodingException
    {
    	StringBuffer sb = new StringBuffer();
    	Iterator<String> it = list.iterator();
    	while(it.hasNext()) {
    		String value = it.next();
    		sb.append(value);
    		if (it.hasNext())
    			sb.append(",");

    	}
    	// System.out.println("values are " + sb.toString());
    	return URLEncoder.encode(name, "UTF-8") + "=" + URLEncoder.encode(sb.toString(), "UTF-8");

    	/*
        String data = URLEncoder.encode("sources", "UTF-8") + "=" + URLEncoder.encode("dead,white,deep", "UTF-8");
        data += "&" + URLEncoder.encode("sinks", "UTF-8")   + "=" + URLEncoder.encode("hateful,joyful,joyless", "UTF-8");
        */
    }

    public static PathMetric getBestMetric(List<PathMetric> list)
	{
		Iterator<PathMetric> it = list.iterator();
		PathMetric minMetric = null;
		while (it.hasNext()) {
			PathMetric pm = it.next();
			if (minMetric == null) {
				minMetric = pm;
			} else {
				minMetric = PathMetric.min(minMetric, pm);
			}
		}
		return minMetric;
	}

	public static List<PathMetric> getAllMetric(String word, List<String> targets)
	{
		List<String> words = new ArrayList<String>();
		words.add(word);
		return getAllMetrics(words, targets);
	}


	public static List<PathMetric> getAllMetrics(List<String> words, List<String> targets)
	{

		// /paths/

		try {

			// /paths/
			StringBuffer site = new StringBuffer();
			site.append(host).append("paths/");

			// set up the connection parameters/properties
			URL url = new URL(site.toString());
			URLConnection conn = url.openConnection();
	        conn.setDoOutput(true);
	        //conn.setDoInput (true);
	        conn.setUseCaches (false);
	        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

	        //
	        // the post data
	        //
	        String src    = encodeParameter("sources", words);
	        String sinks  = encodeParameter("sinks", targets);
	        String data = src + "&" + sinks;

	        /*
	        DataOutputStream out = new DataOutputStream (conn.getOutputStream ());
	        // Write out the bytes of the content string to the stream.
	        out.writeBytes(data);
	        out.flush ();
	        out.close ();
	        */

	        System.out.println("write params");
	        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	        wr.write(data);
	        wr.flush();
	        // wr.close();

			// read response from server
	        System.out.println("read back");
			BufferedReader in
			   = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String inputLine;
			StringBuffer sb = new StringBuffer();
			while ((inputLine = in.readLine()) != null)
				sb.append(inputLine);
			in.close();

			// System.out.println(sb.toString());

			List<PathMetric> list = parseJson(sb.toString());
			return list;

       }
	   catch (MalformedURLException e) {
		   e.printStackTrace();
	   }
	   catch(IOException ioe){
		   ioe.printStackTrace();
	   }
	   catch (Exception e) {
		   e.printStackTrace();
	   }

	   return null;

	}

	private static List<PathMetric> parseJson(String toParse) throws JSONException
	{
		JSONArray json = new JSONArray(toParse);
		int size = json.length();

		List<PathMetric> list = new ArrayList<PathMetric>();

		for (int i = 0; i < size;) {
			JSONObject jo = json.getJSONObject(i);
			JSONObject fields = jo.getJSONObject("fields");
			int count          = fields.getInt("count");
			String startWord   = fields.getString("startWord");
            String endWord     = fields.getString("endWord");
            int uniqueCount    = fields.getInt("unique");
            i++;

			int symCount = 0;
			int pathLength = 0;
			PathMetric metric = new PathMetric();
			metric.start  = startWord;
			metric.end    = endWord;
			metric.unique = uniqueCount;
			for (int j = 0; j < count; j++) {

				jo = json.getJSONObject(i + j);
			    fields = jo.getJSONObject("fields");

				boolean isSym  = fields.getBoolean("isSymmetric");
				pathLength     = fields.getInt("pathLength");
				//String csv     = fields.getString("csvPath");
				if (isSym) symCount++;
			}
			metric.setPaths(count, symCount);
			metric.depthFound = pathLength;

			if (count > 0) {
			   list.add(metric);
			}


			// System.out.println(metric);

			i += count;

		}

		return list;
	}

	public static PathMetric getMetric(String word1, String word2)
	{

		PathMetric metric = new PathMetric();

		try {

			StringBuffer site = new StringBuffer();
			site.append(host).append("path/").append(word1).append("/").append(word2).append("?format=json");
			URL url = new URL(site.toString());
			BufferedReader in =
				new BufferedReader(new InputStreamReader(url.openStream()));

			String inputLine;
			StringBuffer sb = new StringBuffer();
			while ((inputLine = in.readLine()) != null)
				sb.append(inputLine);

			in.close();

			// parse the response
			List<PathMetric> list = parseJson(sb.toString());
			if (list.size() == 0) {
				metric.start = word1;
				metric.end   = word2;
				metric.setPaths(0, 0);
				metric.depthFound = 0;
				return metric;
			}

			return list.get(0);

		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
        catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		return metric;
	}

}
