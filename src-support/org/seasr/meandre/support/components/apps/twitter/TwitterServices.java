package org.seasr.meandre.support.components.apps.twitter;


import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.seasr.meandre.support.components.opennlp.TextSpan;
import org.seasr.meandre.support.generic.gis.GeoLocation;

import twitter4j.Status;


public class TwitterServices {


	protected TwitterServices()
	{

	}

	public static String NO_LOCATION = "-1,-1";


	public static String getLocation(Status status)
	{
		//
		// location of the tweet
		//
		twitter4j.GeoLocation geo = status.getGeoLocation();
        if (geo == null) {
           return NO_LOCATION;
        }
        else {

        	double v1 = geo.getLatitude();
    		double v2 = geo.getLongitude();

    		if (v1 != -1.0 && v2 != -1.0) {
    			return v1 + "," + v2;
    		}
        }

        //
        // at this point there is no geo location (or it's -1,-1)
        // try to get user's location
        //


		String location = status.getUser().getLocation();
		if (location == null)                  location = NO_LOCATION;
		else if (location.equals("null"))      location = NO_LOCATION;
		else if (location.trim().length() < 2) location = NO_LOCATION;

		return location;
	}

	//
	// not a perfect reg ex for lat/long, but close enough
	// e.g would except 23...230
	static String NUMBER_REGEX = "(-?[0-9\\.]+)";
	static Pattern pattern = Pattern.compile(NUMBER_REGEX);
	public static GeoLocation findLocation(String location)
	{

		double v1 = -1.0;
		double v2 = -1.0;

		//
		// see if the location has any lat,long
		//
		// Location ?T: 39.058114,-94.594191
		// Location iPhone: 55.715157,37.615799
		// Location: everywhere ?
		//


		try {

			v1 = -1.0f;
			v2 = -1.0f;

			Matcher matcher = pattern.matcher(location);
			if (matcher.find()) {
				v1 = Double.parseDouble(matcher.group(0));
				if (matcher.find()){
					v2 = Double.parseDouble(matcher.group(0));
				}

			}

			if (v1 != -1.0 && v2 != -1.0) {
				return GeoLocation.createLocation(v1,v2);
			}

			// assume it's text
			// ask Yahoo
			GeoLocation[] locations = GeoLocation.geocode(location);
			return (locations != null) ? locations[0] : null;
		}catch (Exception e) {
			// probably a parsing error
			System.out.println("PARSE ERROR " + location);
		}


		// return a empty location
		return null;

	}


    public static float parsingPercentage(String test)
    {
    	CharSequence seq = test.subSequence(0, test.length());
    	int size = seq.length();
    	int count = 0;
    	int total = size;
    	for (int i = 0; i < size; i++) {
    		char ch = seq.charAt(i);
    		if (Character.isLetterOrDigit(ch)) {
    			count++;
    		}
    		if (Character.isWhitespace(ch)) {
    			total--;
    		}
    	}
    	if (count < 2) {return 0f;}

    	total = Math.abs(total);

    	return (float)count/(float)total;
    }

    public static boolean isASCII(String test) {
    	return (convertToASCII(test) != null);
    }

    public static String convertToASCII(String test) {

    	byte bytearray []  = test.getBytes();
        CharsetDecoder d = Charset.forName("US-ASCII").newDecoder();
        try {
          CharBuffer r = d.decode(ByteBuffer.wrap(bytearray));

          StringBuffer sb = new StringBuffer();
          // now test for only printable characters
          int size = r.length();
          for (int i = 0; i < size; i++) {
        	  char c = r.charAt(i);
        	  if ( c < 32 || c == 127) {
        		  sb.append(" ");
        	  }
        	  else {
        		  sb.append(c);
        	  }

          }
          return sb.toString();
        }
        catch(CharacterCodingException e) {
        	return null;
        }
    }

    static String[] domains = new String[] {"com", "org", "edu", "info", "biz", "tv", "gov", "mil"};
    public static boolean containsDomain(String token)
    {
    	// grab emails and domain names .. trying to avoid regex hell
		// .com .gov .info .org .tv .edu
		// mikeh@ncsa.uiuc.edu
		int idx = token.lastIndexOf(".");
		int len = token.length();

		// all this to avoid REG ex
		if (idx > 1 && len > 3){       // assume a.tv would be the shortest
			String suffix = token.substring(idx+1);
			int sLen = suffix.length();
			if (sLen > 1 && sLen < 5) {   // .tv, .com, .info
			   char b = token.charAt(idx-1);
			   char a = token.charAt(idx+1);
			   if (Character.isLetter(a) && Character.isLetter(b)){
				   for (String d : domains) {
					   if (suffix.equals(d)) {
						   return true;
					   }
				   }
			   }
			}
		}
		return false;
    }

   public static List<TextSpan> findToken(String token,
		                                  int minValueLength,
		                                  String sentence)
   {

	   List<TextSpan> list = new ArrayList<TextSpan>();

		StringTokenizer tokens = new StringTokenizer(sentence);
		int sIdx = 0;
		int eIdx = 0;

		while(tokens.hasMoreTokens()) {
			String s = tokens.nextToken();

			int idx = s.toLowerCase().indexOf(token);
			if ( idx >= 0) {

				String sub = s.substring(idx);
				if (sub.length() < minValueLength) {
					continue;
				}

				sIdx = sentence.indexOf(sub, eIdx);
				eIdx = sIdx + sub.length();

				TextSpan span = new TextSpan();
				span.setStart(sIdx);
				span.setEnd(eIdx);
				span.setSpan(sentence);

				list.add(span);

			}

		}
		return list;
   }

   public static List<TextSpan> findPattern(Pattern pattern, String sentence)
   {

	   List<TextSpan> list = new ArrayList<TextSpan>();

	   StringTokenizer tokens = new StringTokenizer(sentence);
	   int sIdx = 0;
	   int eIdx = 0;

	   while(tokens.hasMoreTokens()) {
		   String s = tokens.nextToken();

		   Matcher m = pattern.matcher(s);
		   if (m.find()) { // was m.matches()

               // String sub = s.substring(m.start(), m.end());
               String sub = m.group();

			   sIdx = sentence.indexOf(sub, eIdx);
			   eIdx = sIdx + sub.length();

			   TextSpan span = new TextSpan();
			   span.setStart(sIdx);
			   span.setEnd(eIdx);
			   span.setSpan(sentence);

			   list.add(span);

		   }
		   /*
		   else {
			   System.out.println("no match " + s);
		   }
		   */


	   }
	   return list;
   }


   static String TWITTER_HASH = "^#[A-Za-z0-9_]+";
   static String TWITTER_USER = "^@[A-Za-z0-9_]+";  // don't match the sometimes trailing :
   // user should strip out ending punctuation $10.20, $100.23:$20.20
   static String MONEY        = "^[-+]?\\$\\d+\\.?\\d*";     // $1 $2.23  $1,000.23
   static String NUMBER       = "^[-+]?[\\$]?\\d+\\.?\\d*";
   static String DATE         = "^[0-9]+[-//]+[0-9]+[-//]+[0-9]+"; // 12-12-2009
   public static Pattern patternTwitterHash = Pattern.compile(TWITTER_HASH);
   public static Pattern patternTwitterUser = Pattern.compile(TWITTER_USER);
   public static Pattern patternMoney       = Pattern.compile(MONEY);
   public static Pattern patternNumber      = Pattern.compile(NUMBER);
   public static Pattern patternDate        = Pattern.compile(DATE);

   public static List<TextSpan> findMoney(String sentence)
   {
	   return findPattern(patternMoney, sentence);
   }

   public static List<TextSpan> findTwitterHash(String sentence)
   {
	   return findPattern(patternTwitterHash, sentence);
   }

   public static List<TextSpan> findTwiterUsers(String sentence)
   {
	   return findPattern(patternTwitterUser, sentence);
   }

   public static List<TextSpan> findURLS(String sentence)
   {
	   String token = "http";
	   int length = 8;           //  http + ://  + 1
	   return findToken(token, length, sentence);
   }



	public static String getActualHost(String fullURL)
	{
		return getActualHost(fullURL, 3000);
	}

	public static String getActualHost(String fullURL, int timeout)
	{

		if (fullURL.length() <= "http://.com".length()) {
			return fullURL;
		}

		try {

			HttpURLConnection.setFollowRedirects(true);
			URL url = new URL(fullURL);

			//
			// HttpURLConnection http = url.openConnection();
			//
			URLConnection http = url.openConnection();
			http.setConnectTimeout(timeout);
			http.setReadTimeout(timeout);

			// http.setDoOutput(true);
			Map map = http.getHeaderFields(); // force to connect
			// http.getURL();
			return http.getURL().getHost();

		}
		catch (Exception e) {

			return fullURL;
		}

	}

}


/*
 *
		List<TextSpan> list = new ArrayList<TextSpan>();

		StringTokenizer tokens = new StringTokenizer(sentence);
		int sIdx = 0;
		int eIdx = 0;

		while(tokens.hasMoreTokens()) {
			String s = tokens.nextToken();

			int idx = s.indexOf("#");
			if ( idx != 0) {
				idx = s.indexOf("@");
			}

			if (idx == 0) {

				String sub = s.substring(idx);
				if (sub.length() <= 1) {
					continue;
				}

				sIdx = sentence.indexOf(sub, eIdx);
				eIdx = sIdx + sub.length();

				TextSpan span = new TextSpan();
				span.setStart(sIdx);
				span.setEnd(eIdx);
				span.setSpan(sentence);

				list.add(span);

			}
		}
		*/
