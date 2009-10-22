package org.seasr.meandre.components.vis.geographic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


/*
37.843075,-122.27787
37.843075,-122.277869
*/


public class GeoLocation {
	
	double latitude;
	double longitude;
	
	public GeoLocation()
	{ 
		this(-1.0f, -1.0f);
	}
	
	public GeoLocation(double lat, double lng) {
		
		this.latitude  = lat;
		this.longitude = lng;
		
	}
	
	public double getLatitude()  {return latitude;}
	public double getLongitude() {return longitude;}
	
	public void setLatitude(double l) {
		this.latitude = l;
	}
	public void setLongitude(double l) {
		this.longitude = l;
	}
	
	public boolean isValid()
	{ 
		return (this.latitude != -1 || this.longitude != -1);
	}
	
	

    public static final String defaultAPIKey = "yFUeASDV34FRJWiaM8pxF0eJ7d2MizbUNVB2K6in0Ybwji5YB0D4ZODR2y3LqQ--";
    

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
			new GeoLocation(Double.parseDouble(lat), Double.parseDouble(lng));
		

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
