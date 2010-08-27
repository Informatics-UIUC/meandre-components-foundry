package org.seasr.meandre.components.GoogleDocs;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.MediaContent;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.data.media.MediaSource;

public class GDocDownloadWrapper

{
	public static String ListDirPath="";
	public static void CreateFile(String path,String content) throws IOException
	{
		  File f;
	      f=new File(path);
	      if(!f.exists()){
	      try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		FileOutputStream fop;
		try {
			fop = new FileOutputStream(f);
			fop.write(content.getBytes());
			fop.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	      }

	}

	public static String DownloadGDocDir(String userGMailId,String password,String DirName,String ListFileName,String TargetDir)

{

	try{
		//System.out.println("starting");
		HashMap<String, String> hashMap = new HashMap<String, String>();
		DocsService service=new DocsService("Listing documents");
		service.setUserCredentials(userGMailId,password);
		//System.out.println("authentication complete");
		URL feedUri = new URL("http://docs.google.com/feeds/default/private/full/-/folder?showfolders=true");
		//System.out.println("feed ");
		DocumentListFeed feed=service.getFeed(feedUri, DocumentListFeed.class);
		//System.out.println("feed acquired");
		printDocuments(feed);
		boolean success = (new File(TargetDir)).mkdir();
	    if (success) {
	      //System.out.println("Directory: " + TargetDir + " created");

	    }
	    ListDirPath=(new File(TargetDir)).getAbsolutePath();

		for (DocumentListEntry Mainentry : feed.getEntries())
		  {
			//System.out.println("outside Dir feed");
			String Type=Mainentry.getType().toString();
			//System.out.println(Type);
			if(Type.equals("folder")&& Mainentry.getTitle().getPlainText().equals(DirName))
			{
				//System.out.println("Dir feed");
				URL Docurl = new URL("http://docs.google.com/feeds/default/private/full/-/" + Mainentry.getTitle().getPlainText());
				DocumentListFeed Dirfeed=service.getFeed(Docurl, DocumentListFeed.class);
				printDocuments(Dirfeed);

				for (DocumentListEntry entry : Dirfeed.getEntries())
				{
					hashMap.put(entry.getTitle().getPlainText(),entry.getResourceId());

				}
				//System.out.println(hashMap);
		  }
		  }
		String ListresourceId=hashMap.get(ListFileName);
		String ListdocType = ListresourceId.substring(0, ListresourceId.lastIndexOf(':'));
		String ListdocId = ListresourceId.substring(ListresourceId.lastIndexOf(':') + 1);
		URL Listurl = new URL("http://docs.google.com/feeds/download/" + ListdocType +"s/Export?docID=" + ListdocId + "&exportFormat=" + "txt");
		MediaContent mc = new MediaContent();
	    mc.setUri(Listurl.toString());
	    MediaSource ms = service.getMedia(mc);
	    InputStream inStream = null;
	    inStream = ms.getInputStream();
	    BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
		String line = "";
		StringBuffer sb = new StringBuffer();
		while ((line = br.readLine()) != null)
		{
		if(!line.contains(">"))
		{
		sb.append(line).append("\n");
		//System.out.println(line+"-----");
		String SubListresourceId="";

		if((SubListresourceId=hashMap.get(line.split(":")[0]))!=null)
		{

		//System.out.println("^^^^^"+SubListresourceId);

		String SubListdocType = SubListresourceId.substring(0, SubListresourceId.lastIndexOf(':'));

		String SubListdocId = SubListresourceId.substring(SubListresourceId.lastIndexOf(':') + 1);
		URL SubListurl = new URL("http://docs.google.com/feeds/download/" + SubListdocType +"s/Export?docID=" + SubListdocId + "&exportFormat=" + "txt");
		MediaContent submc = new MediaContent();
	    submc.setUri(SubListurl.toString());
	    MediaSource subms = service.getMedia(submc);
	    InputStream SubinStream = null;
	    SubinStream = subms.getInputStream();
	    BufferedReader subbr = new BufferedReader(new InputStreamReader(SubinStream));
		String subline = "";
		StringBuffer Subsb = new StringBuffer();
		while ((subline = subbr.readLine()) != null)
		{
		if(!subline.contains(">"))
		{
		Subsb.append(subline).append("\n");
		}
		}
		//System.out.println(Subsb.toString().toLowerCase());
		CreateFile(TargetDir+"/"+line.split(":")[0],Subsb.toString().toLowerCase());

		}

		}
		}
		CreateFile(TargetDir+"/"+ListFileName,sb.toString());


	}
	catch(Exception ex)
	{
		System.err.println("Exception:"+ex.getMessage());

	}
     if(!ListDirPath.contentEquals(""))
	 return ListDirPath+"/"+ListFileName;
     else
     return "Unable to create Directory";
}

public static void printDocuments(DocumentListFeed feed) {
	  for (DocumentListEntry entry : feed.getEntries()) {
	    String resourceId = entry.getResourceId();
	    //System.out.println(" -- Document(" + resourceId + "/" + entry.getTitle().getPlainText() + ")");
	  }
	}
public static void main(String[] args)

{
	System.out.println("List File availbale at ----"+DownloadGDocDir("gaz.meandre@gmail.com","Seasr.Meandre","gaz","lists.def","Nlists"));
}

}