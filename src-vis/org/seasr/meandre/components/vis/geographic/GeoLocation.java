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
