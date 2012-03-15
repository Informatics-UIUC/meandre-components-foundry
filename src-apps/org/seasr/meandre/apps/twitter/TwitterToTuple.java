package org.seasr.meandre.apps.twitter;


import java.util.ArrayList;
import java.util.List;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.apps.twitter.TwitterServices;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;

import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.StatusStream;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;


/**
 *
 * TESTING ONLY  (DO NOT USE for production ... yet)
 *
 * Data Server:   TwitterToTuple --> StanfordNETupleTagger --> TwitterTupleWebServer
 *
 * Vis:           URLReader -> TwitterCircleGraphVis -> GenericViewer
 *
 * @author Mike Haberman;
 *
 */


@Component(
		name = "Twitter to Tuple",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "#TRANSFORM, tuple, twitter",
		description = "This component reads a twitter stream and pushes out tuples (id,text,userid, followers, location)",
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar", "twitter4j-core-2.1.2.jar"}
)
public class TwitterToTuple extends AbstractExecutableComponent
implements Runnable, StatusListener {


    //------------------------------ INPUTS ------------------------------------------------------

	//------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TUPLES,
			description = "tuples (one tuple: title, location, tweet/content, text(cleaned)"
	)
	protected static final String OUT_TUPLES = Names.PORT_TUPLES;

	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for the tuple (title, location, content)"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;


	//----------------------------- PROPERTIES ---------------------------------------------------
	@ComponentProperty(
			name = "twitterUser",
			description = "user id used to sign on to twitter",
		    defaultValue = ""
		)
	protected static final String PROP_USER = "twitterUser";

	@ComponentProperty(
			name = "twitterPassword",
			description = "password used to sign on to twitter",
		    defaultValue = ""
		)
	protected static final String PROP_PASSWORD = "twitterPassword";


	//
	// how many tuples to buffer before
	// pushing them to the output
	//
	int WINDOW_SIZE = 5;

	//--------------------------------------------------------------------------------------------

	SimpleTuple outTuple;
	protected TwitterStream twitterStream = null;
	List<Strings> buffer = new ArrayList<Strings>();

	private synchronized void waitForStatus() {
        try {
            //this.wait(Integer.MAX_VALUE);
            this.wait();
            System.out.println("notified.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

   String userName;
   String passwd;
   public void run()
   {
	   try {
		Thread.currentThread().sleep(5000);
	    console.info("Start Twitter reader");

	    /*
	    Twitter tweet = new Twitter("seasrSalad" , "0penNlp");
	    Status status = tweet.updateStatus("Hello world2 {\"name\":\"mike\"}");
	    User me = status.getUser();
	    console.info("user is " + me.getId());
	    */


		// twitterStream = new TwitterStream("seasrSalad", "0penNlp", this);

	
		// this will force an exception if invalid
		TwitterStream tsTmp = new TwitterStreamFactory().getInstance(userName, passwd);
		StatusStream tmp = tsTmp.getSampleStream();
		tmp.close();
		tsTmp.cleanup();
		
		// if we got this far, we are good to go
		twitterStream = new TwitterStreamFactory().getInstance(userName, passwd);
		twitterStream.setStatusListener(this);

		// for a continuous unfiltered stream:
		twitterStream.sample();


		/*
		int count = 0; // last 100 messages, NOT supported
		int[] follow = new int[0]; // don't follow any specific users
		String[] tags = new String[]{"#apple", "Balloon Boy"};
		twitterStream.filter(count, follow, tags);
		*/


          // black forever
          waitForStatus();
         
	   }
	   catch(Exception e) {
		   console.info("unable to read twitter " + e.toString());
	   }
	   finally {
		   if (twitterStream != null)
		      twitterStream.cleanup();
	   }

	   console.info("Stop Twitter service");

	   noExceptions = false;   
	   synchronized(buffer) {
		   buffer.notifyAll();
	   }

   }


	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception
	{
	    String ID   = "id";
	    String TEXT = "text";
	    String TWEET = "tweet";
	    String USER = "userId";
	    String FOLL = "followers";
	    String LOC  = "location";


	    SimpleTuplePeer outPeer = new SimpleTuplePeer(new String[]{ID,TWEET,TEXT,USER,FOLL,LOC});
		outTuple = outPeer.createTuple();

		ID_IDX         = outPeer.getIndexForFieldName(ID);
		TEXT_IDX       = outPeer.getIndexForFieldName(TEXT);
		TWEET_IDX      = outPeer.getIndexForFieldName(TWEET);
		USER_IDX       = outPeer.getIndexForFieldName(USER);
		FOLLOWERS_IDX  = outPeer.getIndexForFieldName(FOLL);
		LOCATION_IDX   = outPeer.getIndexForFieldName(LOC);


		userName = ccp.getProperty(PROP_USER);
		passwd   = ccp.getProperty(PROP_PASSWORD);

	}

	int ID_IDX;
	int TEXT_IDX;
	int TWEET_IDX;
	int USER_IDX;
	int FOLLOWERS_IDX;
	int LOCATION_IDX;

	boolean noExceptions = true;
	
	@Override
	public void executeCallBack(ComponentContext cc) throws Exception
	{

		Thread t = new Thread(this);
		t.start();

		while(noExceptions) {

			Strings[] results;
			// console.info("acquire the lock");
			synchronized (buffer) {

				// block until I have some data
				while(buffer.isEmpty()) {
					try {
						buffer.wait();
					}
					catch (InterruptedException ie) {console.info(ie.toString());}
					
					if (!noExceptions) {
						break;
					}
				}

				results = new Strings[buffer.size()];
				buffer.toArray(results);
				buffer.clear();
			} // release the lock


			cc.pushDataComponentToOutput(OUT_META_TUPLE, outTuple.getPeer().convert());
			StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
			cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);
		}

		
		console.info("ending service");

	}

    @Override
	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {

    }


    // StatusListener interface
    static int ID = 1;
    public void onStatus(Status status)
    {
    	String text = status.getText();

    	//
    	// cull out as much as possible here
    	//
    	text = TwitterServices.convertToASCII(text);
    	if (text == null) {
    		// console.info("SKIP non-ascii " + status.getText());
    		return;
    	}

    	float pct = TwitterServices.parsingPercentage(text);
    	if (pct < 0.40) {
    		// console.info("SKIP " + pct + " " + text);
    		return;
    	}


    	User user = status.getUser();
    	String location = TwitterServices.getLocation(status);

    	/*
    	if (location == TwitterServices.NO_LOCATION) {
    		return;
    	}
    	*/

    	//console.info("Raw      " + status.getText());
    	if (status.isRetweet()) {
    		console.fine("YES RT " + status.getText());
    	}

    	String clean = clean(text);

    	/*
    	if (c.length() != text.length()) {
    		console.info(text);
    		console.info(c);
    	}
    	*/

    	outTuple.setValue(ID_IDX,        ID++);
    	outTuple.setValue(USER_IDX,      user.getId());
        outTuple.setValue(FOLLOWERS_IDX, user.getFavouritesCount());
        outTuple.setValue(TWEET_IDX,     text);
        outTuple.setValue(TEXT_IDX,      clean);
        outTuple.setValue(LOCATION_IDX,  location);


        // console.info("got data ");

        synchronized(buffer) {

        	buffer.add(outTuple.convert());

            if (buffer.size() > WINDOW_SIZE) {

            	// console.info("wake the waiter " + buffer.size());
            	buffer.notifyAll();

            }

        }
        // console.info("leaving");

    }
    public void onTrackLimitationNotice(int numberOfLimitedStatuses){}
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice){}
    public void onException(Exception ex) 
    {
    	noExceptions = false;
    	console.warning(ex.toString());
    	
    	
    	
    	synchronized(buffer) {
 		   buffer.notifyAll();
 	   }
    	
    	
    	// get a null pointer error from twitter4J
    	if (twitterStream != null) {
    	   twitterStream.cleanup();
    	}
    	
    	
    	
    	// throw new RuntimeException(ex.toString());
    }



    public String clean(String tweet)
    {
    	tweet = tweet.replaceAll("[)(]", "");

    	// based on heuristics, certainly not perfect
    	// trade off speed

    	// first split the tokens, consuming consecutive white space
    	String[] tokens = tweet.split("[\\s]+");
    	List<String> keep = new ArrayList<String>();
    	for (String token : tokens) {

    		// keep urls, twitter hashes, twitter users

    		if (token.indexOf("http") == 0 ||
    			token.indexOf("www")  == 0 ||
    			token.indexOf("@")    == 0 ||
    			token.indexOf("#")    == 0)
    		{
    			keep.add(token);
    			continue;
    		}

    		if (TwitterServices.containsDomain(token)) {
    			keep.add(token);
				// console.info("YES " + token);
				continue;
    		}
    		int len = token.length();


    		//
    	    // handle most basic contractions: it's what's I'm don't we've
    		// otherwise, you will end up with it ' s  and what ' s
    		//


    		if (len > 2 && token.charAt(len - 2) == '\'') {
    			// might want to strip off leading and trailing punct
    		    keep.add(token);
    			continue;
    		}
    		if (len > 3 && token.charAt(len - 3) == '\'') {
    			// might want to strip off leading and trailing punct
    		    keep.add(token);
    			continue;
    		}


    		//replace consecutive non alpha/numeric chars with a single
    		//??????????????? ==> ?
    		//shower?....I    ==> shower?.I

    		token = token.replaceAll("([^0-9A-Za-z])\\1+", "$1");

    		// GOAL: put spaces between punct and alpha/numeric
    		// separate with spaces those tokens with punct inside of them
    		// shower?.I ==> shower ?. I
    		// but don't do digits
    		// 20% ==> 20 %
    		// 10/10/2010 ==> 10 / 10 / 2010
    	    token = token.replaceAll("([A-Za-z])([^A-Za-z0-9])", "$1 $2");
    	    token = token.replaceAll("([^A-Za-z0-9])([A-Za-z])", "$1 $2");
    		keep.add(token);
    	}

    	StringBuilder sb = new StringBuilder();
    	int len = keep.size();
    	for (int i = 0; i < len; i++) {
    		sb.append(keep.get(i));
    		if (i + 1 < len)
    			sb.append(" ");
    	}
    	return sb.toString();

    	// this won't work: Named Entity depends on capitalization
    	//return sb.toString().toLowerCase();

    }
}
