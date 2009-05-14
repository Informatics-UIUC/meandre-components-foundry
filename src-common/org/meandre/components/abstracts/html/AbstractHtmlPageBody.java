/**
 * 
 */
package org.meandre.components.abstracts.html;

import java.util.Hashtable;
import java.util.Set;
import java.util.Map;

/**
 * @author bernie acs
 *
 */
public abstract class AbstractHtmlPageBody {
	
	private static String sBodyInstanceId = null;
	private static StringBuffer defaultHtmlOpenBodyTag = null;
	private static StringBuffer defaultBodyHtmlScriptsFragment = null;	
	private static StringBuffer defaultBodyHtmlCssTagsFragment = null;
	
	private static Hashtable<String,String> selectiveBodyHtmlFragments = new Hashtable<String,String>();
	
	/**
	 * 
	 */
	public void initializeBody(){
		( defaultHtmlOpenBodyTag = new StringBuffer() ).append("<Body>\n" );
		( defaultBodyHtmlScriptsFragment = new StringBuffer() ).append("");
		( defaultBodyHtmlCssTagsFragment = new StringBuffer() ).append("");		
	}
	
	/**
	 * 
	 * @param instanceUrlId
	 */
	public void setBodyInstanceId(String instanceUrlId){
		sBodyInstanceId= instanceUrlId ;
	}
		
	/**
	 * 
	 * @param fragment
	 */
	public void setDefaultHtmlOpenBodyTag(String fragment){
		defaultHtmlOpenBodyTag.delete(0, (defaultHtmlOpenBodyTag.length()-1) );
		defaultHtmlOpenBodyTag.append(fragment);
	}

	/**
	 * 
	 * @param fragment
	 */
	public void setBodyDefaultHtmlScriptsFragment(String fragment){
		defaultBodyHtmlScriptsFragment.append(fragment);
	}

	/**
	 * 
	 * @param fragment
	 */
	public void setBodyDefaultHtmlCssTagsFragment(String fragment){
		defaultBodyHtmlCssTagsFragment.append(fragment);
	}
	
	/**
	 * 
	 * @param key
	 * @param fragment
	 */
	public void setBodySelectiveHtmlFragements(String key, String fragment){
		selectiveBodyHtmlFragments.put(key, fragment);
	}

	/**
	 * getHtml will return HtmlHead built in two
	 * segment concatenated together. The first 
	 * part is made-up of the following;
	 * 
	 *   <Body> or customize tag defined using set
	 *   defaultHtmlCssTagsFagment
	 *   defaultHtmlScriptsFragment
	 *   
	 * The second part is made up of the following;
	 * 
	 *   
	 *   </Body>
	 *  
	 * Selective fragment(s) can be called for and
	 * would be insert between the two parts described
	 *  
	 * @return
	 * 
	 */
	public String getBodyHtml(){
		StringBuffer s = new StringBuffer();
		s.append( getBodyHtmlOpen() );
		s.append( getBodyHtmlClose() );
		return s.toString();
	}

	/**
	 * getHtml includes fragment previously stored
	 * using setSelectiveHtmlFragments(); Includes
	 * the fragment between HtmlOpen and HtmlClose
	 * as described in getHtml(). 
	 *  
	 * @param keys
	 * @return
	 * 
	 */
	public String getBodyHtml(String key){
		StringBuffer s = new StringBuffer();
		s.append( getBodyHtmlOpen() );
		if( (selectiveBodyHtmlFragments != null)
		  &&(selectiveBodyHtmlFragments.containsKey(key)) 
		){
			s.append( selectiveBodyHtmlFragments.get(key));
		}
		s.append( getBodyHtmlClose() );
		return s.toString();
	}

	/**
	 * getHtml including multiple fragment previously stored
	 * using setSelectiveHtmlFragements(); The argument will
	 * be processed using for(key : keys) which will determine
	 * the order that fragments will be inserted. The inserts
	 * are the same as described in getHtml(String). 
	 *  
	 * @param keys
	 * @return
	 * 
	 */
	public String getBodyHtml(Set<String> keys){
		StringBuffer s = new StringBuffer();
		s.append( getBodyHtmlOpen() );
		if( (selectiveBodyHtmlFragments != null)
		  && (keys.size() > 0 ) 
		){
			for(String key: keys){
				if( (selectiveBodyHtmlFragments != null)
					&&(selectiveBodyHtmlFragments.containsKey(key)) 
				){
					String frag = selectiveBodyHtmlFragments.get(key);
					if( frag != null )
						s.append( frag );
				}
			}
		}
		s.append( getBodyHtmlClose() );
		return s.toString();
	}

	/**
	 * This method provides a means of sending non-persistent collection
	 * keyed Html Fragements that will be used to populate selectiveHtmlFragements
	 * and then using getHtml(Set<String>) to retrieve the resulting Html Page Body
	 * using the new content. The order of Output is the order that item exists 
	 * in the input Map.keySet().  When the Html Page Body is retrieved the original
	 * content of selectiveHtmlFragements is restored.
	 * 
	 * @param keys
	 * @return
	 */
	public String getBodyHtml(Map<String,String> keys){
		StringBuffer s = new StringBuffer();
		Hashtable<String,String> ht = new Hashtable<String,String>();
		ht.putAll(selectiveBodyHtmlFragments);
		selectiveBodyHtmlFragments.clear();
		Set<String> keySet = keys.keySet();
		for(String key: keySet){
			setBodySelectiveHtmlFragements(key, keys.get(key) );
		}
		s.append( getBodyHtml(keySet) );
		selectiveBodyHtmlFragments.clear();
		selectiveBodyHtmlFragments.putAll(ht);
		return s.toString();
	}

	/**
	 * This method provides a means of sending non-persistent collection
	 * Html Fragements that will be used to populate selectiveHtmlFragements
	 * and then using getHtml(Set<String>) to retrieve the resulting Html Page Body
	 * using the new content. The order of Output is maintained using the index of  
	 * the input String[].  When the Html Page Body is retrieved the original
	 * content of selectiveHtmlFragements is restored.
	 * 
	 * @param fragments[]
	 * @return
	 */
	public String getBodyHtml( String[] fragments ){
		StringBuffer s = new StringBuffer();
		s.append( this.getBodyHtmlOpen() );
		
		for( String f : fragments){
			s.append( f );
		}
		
		s.append(this.getBodyHtmlClose());
		return s.toString();
	}

	/**
	 * 
	 * @return
	 */
	private String getBodyHtmlOpen(){
		StringBuffer s = new StringBuffer();
		s.append("\n"+ defaultHtmlOpenBodyTag.toString() + "\n");
		s.append("\n"+ defaultBodyHtmlCssTagsFragment.toString() + "\n");
		s.append("\n"+ defaultBodyHtmlScriptsFragment.toString() + "\n");

		return s.toString();
	}
	
	/**
	 * 
	 * @return
	 */
	private String getBodyHtmlClose(){
		StringBuffer s = new StringBuffer();
		s.append("\n</Body>\n");
		s.append("\n</Html>\n");
		return s.toString();
	}

	/**
	 * 
	 * @return
	 */
	public String makeHtmlDismissLink(
	){
		//
		return makeHtmlDismissLink(
				null,
				null,
				null,
				null,
				null
		);
		//
	}
	/**
	 * 
	 * @param parameterName
	 * @param parameterValue
	 * @param linkTitle
	 * @param toolTipHint
	 * @param linkDisplayText
	 * @return
	 */
	public  String makeHtmlDismissLink(
			String parameterName,
			String parameterValue,
			String linkTitle,
			String toolTipHint,
			String linkDisplayText
	){
		//
		String actionUrl = "";
		String pName = "done";
		String pValue= "true";
		String lTitle= "Done with WebUI fragment";
		String ttHint= "Done with WebUI fragment";
		String ldText= "Dismiss WebUI fragment";
		
		//
		StringBuffer s = new StringBuffer();
		actionUrl = (sBodyInstanceId==null)? "/" : sBodyInstanceId ;
		pName     = (parameterName==null ) ? pName : parameterName ;
		pValue    = (parameterValue==null)? pValue : parameterValue ;
		lTitle    = (linkTitle==null)? lTitle : linkTitle ;
		ttHint    = (toolTipHint==null)? ttHint : toolTipHint ;
		ldText    = (linkDisplayText == null)?    ldText : linkDisplayText ;
		
		//
		s.append("<BR>\n");
		s.append("<a href="+ actionUrl );
		s.append("?" + pName );
		s.append("=" + pValue ); 
		s.append("&");
		s.append("title=\"" + lTitle +"\"&");
		s.append("tooltip=\""+ ttHint +"\" >");
		s.append( ldText + "</a>\n<BR>\n");
		
		//
		return s.toString();		
	}
	
}
