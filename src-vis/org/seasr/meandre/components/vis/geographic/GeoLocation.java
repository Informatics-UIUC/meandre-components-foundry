package org.seasr.meandre.components.vis.geographic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class GeoLocation {
	
	float latitude;
	float longitude;
	
	public GeoLocation()
	{ 
		this(-1.0f, -1.0f);
	}
	
	public GeoLocation(float lat, float lng) {
		
		this.latitude  = lat;
		this.longitude = lng;
		
	}
	
	public float getLatitude()  {return latitude;}
	public float getLongitude() {return longitude;}
	
	public void setLatitude(float l) {
		this.latitude = l;
	}
	public void setLongitude(float l) {
		this.longitude = l;
	}
	
	

    static final String defaultAPIKey = "yFUeASDV34FRJWiaM8pxF0eJ7d2MizbUNVB2K6in0Ybwji5YB0D4ZODR2y3LqQ--";
    

    public static GeoLocation getLocation(String location) 
    throws IOException 
    {
    	return getLocation(location, defaultAPIKey);
    }
    
    public static GeoLocation getLocation(String location, String yahooAPIKey) 
    throws IOException 
    
    {
    	String param = URLEncoder.encode("location", "UTF-8") + "=" + URLEncoder.encode(location, "UTF-8");

    	StringBuffer sb = new StringBuffer();
        sb.append("http://local.yahooapis.com/MapsService/V1/geocode?appid=");
        sb.append(yahooAPIKey).append("&");
        sb.append(param);
        
        
        // read response from server
        URL url = new URL(sb.toString());
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        
        InputStream is;
        
        try {
        	is = conn.getInputStream();
        }
        catch (IOException e) {
        	// HttpURLConnection http = (HttpURLConnection)conn;
        	// http.getResponseCode();
        	return new GeoLocation();
        }
        
        BufferedReader in = new BufferedReader(new InputStreamReader(is));

		String inputLine;
		sb = new StringBuffer();
		while ((inputLine = in.readLine()) != null)
			sb.append(inputLine);
		in.close();
		
		// parse the response
		String xml = sb.toString();
		String lat = simpleXMLParse(xml, "Latitude");
		String lng = simpleXMLParse(xml, "Longitude");
		
		
		GeoLocation geo = 
			new GeoLocation(Float.parseFloat(lat), Float.parseFloat(lng));
		

        return geo;

    }
    
    public static String simpleXMLParse(String xml, String token)
    {
    	String sToken = "<"  + token + ">";
		String eToken = "</" + token + ">";
		
		int sIdx = xml.indexOf(sToken) + sToken.length();
		int eIdx = xml.indexOf(eToken, sIdx);
		return xml.substring(sIdx, eIdx);
    	
    }
    

}
