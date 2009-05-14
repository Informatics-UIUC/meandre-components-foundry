/**
 * 
 */
package org.meandre.components.abstracts.html;

import java.util.Map;
import java.util.Set;

/**
 * @author bernie acs
 *
 */
public abstract class AbstractHtmlPage {

	private AbstractHtmlPageHead htmlPageHead = null; 
	private AbstractHtmlPageBody htmlPageBody = null;
	
	public void setHtmlPageHead( AbstractHtmlPageHead abstractHtmlPageHead){
		this.htmlPageHead = abstractHtmlPageHead;
	}
	
	public void setHtmlPageBody( AbstractHtmlPageBody abstractHtmlPageBody){
		this.htmlPageBody = abstractHtmlPageBody;
	}
	public  AbstractHtmlPageHead getHtmlPageHead(){
		return this.htmlPageHead;
	}
	
	public AbstractHtmlPageBody getHtmlPageBody( ){
		return this.htmlPageBody;
	}
	
	/* (non-Javadoc)
	 * @see org.meandre.components.abstracts.html.AbstractHtmlPageHead#initializeHead()
	 */
	public void initializeHead() {
		//
		this.htmlPageHead.initializeHead();
	}
	/* (non-Javadoc)
	 * @see org.meandre.components.abstracts.html.AbstractHtmlPageHead#setPageTitle(java.lang.String)
	 */
	public void setPageTitle(String title) {
		//
		this.htmlPageHead.setPageTitle(title);
	}
	
	/* (non-Javadoc)
	 * @see org.meandre.components.abstracts.html.AbstractHtmlPageHead#getHeadHtml()
	 */
	public String getHeadHtml() {
		//
		return this.htmlPageHead.getHeadHtml();
	}
	
	/* (non-Javadoc)
	 * @see org.meandre.components.abstracts.html.AbstractHtmlPageHead#getHeadHtml(java.lang.String)
	 */
	public String getHeadHtml(String key) {
		//
		return this.htmlPageHead.getHeadHtml(key);
	}

	/* (non-Javadoc)
	 * @see org.meandre.components.abstracts.html.AbstractHtmlPageHead#getHeadHtml(java.util.Set)
	 */
	public String getHeadHtml(Set<String> keys) {
		//
		return this.htmlPageHead.getHeadHtml(keys);
	}
	
	/* (non-Javadoc)
	 * @see org.meandre.components.abstracts.html.AbstractHtmlPageBody#getBodyHtml()
	 */
	public String getBodyHtml() {
		//
		return this.htmlPageBody.getBodyHtml();
	}

	/* (non-Javadoc)
	 * @see org.meandre.components.abstracts.html.AbstractHtmlPageBody#getBodyHtml(java.util.Map)
	 */
	public String getBodyHtml(Map<String, String> keys) {
		//
		return this.htmlPageBody.getBodyHtml(keys);
	}

	/* (non-Javadoc)
	 * @see org.meandre.components.abstracts.html.AbstractHtmlPageBody#getBodyHtml(java.util.Set)
	 */
	public String getBodyHtml(Set<String> keys) {
		//
		return this.htmlPageBody.getBodyHtml(keys);
	}

	/* (non-Javadoc)
	 * @see org.meandre.components.abstracts.html.AbstractHtmlPageBody#getBodyHtml(java.lang.String)
	 */
	public String getBodyHtml(String key) {
		//
		return this.htmlPageBody.getBodyHtml(key);
	}

	/* (non-Javadoc)
	 * @see org.meandre.components.abstracts.html.AbstractHtmlPageBody#getBodyHtml(java.lang.String[])
	 */
	public String getBodyHtml(String[] fragments) {
		//
		return this.htmlPageBody.getBodyHtml(fragments);
	}

	/* (non-Javadoc)
	 * @see org.meandre.components.abstracts.html.AbstractHtmlPageHead#setHeadDefaultHtmlCssTagsFragment(java.lang.String)
	 */
	public void setHeadDefaultHtmlCssTagsFragment(String fragment) {
		//
		this.htmlPageHead.setHeadDefaultHtmlCssTagsFragment(fragment);
	}

	/* (non-Javadoc)
	 * @see org.meandre.components.abstracts.html.AbstractHtmlPageBody#setBodyDefaultHtmlCssTagsFragment(java.lang.String)
	 */
	public void setBodyDefaultHtmlCssTagsFragment(String fragment) {
		//
		this.htmlPageBody.setBodyDefaultHtmlCssTagsFragment(fragment);
	}

	/* (non-Javadoc)
	 * @see org.meandre.components.abstracts.html.AbstractHtmlPageHead#setHeadDefaultHtmlScriptsFragment(java.lang.String)
	 */
	public void setHeadDefaultHtmlScriptsFragment(String fragment) {
		//
		this.htmlPageHead.setHeadDefaultHtmlScriptsFragment(fragment);
	}

	/* (non-Javadoc)
	 * @see org.meandre.components.abstracts.html.AbstractHtmlPageBody#setBodyDefaultHtmlScriptsFragment(java.lang.String)
	 */
	public void setBodyDefaultHtmlScriptsFragment(String fragment) {
		//
		this.htmlPageBody.setBodyDefaultHtmlScriptsFragment(fragment);
	}

	/* (non-Javadoc)
	 * @see org.meandre.components.abstracts.html.AbstractHtmlPageHead#setHeadDefaultHtmlMetaTagsFragment(java.lang.String)
	 */
	public void setHeadDefaultHtmlMetaTagsFragment(String fragment) {
		//
		this.htmlPageHead.setHeadDefaultHtmlMetaTagsFragment(fragment);
	}
	
	/* (non-Javadoc)
	 * @see org.meandre.components.abstracts.html.AbstractHtmlPageBody#setBodyInstanceId(java.lang.String)
	 */
	public void setBodyInstanceId(String instanceUrlId) {
		//
		this.htmlPageBody.setBodyInstanceId(instanceUrlId);
	}
	
	/* (non-Javadoc)
	 * @see org.meandre.components.abstracts.html.AbstractHtmlPageBody#setDefaultHtmlOpenBodyTag(java.lang.String)
	 */
	public void setDefaultHtmlOpenBodyTag(String fragment) {
		//
		this.htmlPageBody.setDefaultHtmlOpenBodyTag(fragment);
	}
	
	/* (non-Javadoc)
	 * @see org.meandre.components.abstracts.html.AbstractHtmlPageHead#setHeadSelectiveHtmlFragments(java.lang.String, java.lang.String)
	 */
	public void setHeadSelectiveHtmlFragments(String key, String fragment) {
		//
		this.htmlPageHead.setHeadSelectiveHtmlFragments(key, fragment);
	}
	
	/* (non-Javadoc)
	 * @see org.meandre.components.abstracts.html.AbstractHtmlPageBody#setBodySelectiveHtmlFragements(java.lang.String, java.lang.String)
	 */
	public void setBodySelectiveHtmlFragements(String key, String fragment) {
		//
		this.htmlPageBody.setBodySelectiveHtmlFragements(key, fragment);
	}

	/* (non-Javadoc)
	 * @see org.meandre.components.abstracts.html.AbstractHtmlPageBody#makeHtmlDismissLink()
	 */
	public String makeHtmlDismissLink() {
		//
		return this.htmlPageBody.makeHtmlDismissLink();
	}

	/* (non-Javadoc)
	 * @see org.meandre.components.abstracts.html.AbstractHtmlPageBody#makeHtmlDismissLink(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public String makeHtmlDismissLink(String parameterName,
			String parameterValue, String linkTitle, String toolTipHint,
			String linkDisplayText) {
		//
		return this.htmlPageBody.makeHtmlDismissLink(parameterName, parameterValue, linkTitle,
				toolTipHint, linkDisplayText);
	}


	
	
}
