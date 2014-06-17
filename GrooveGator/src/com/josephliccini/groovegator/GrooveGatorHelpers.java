package com.josephliccini.groovegator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;

import com.scilor.grooveshark.API.Base.GroovesharkClient;
import com.scilor.grooveshark.API.Base.Utilities;
import com.scilor.grooveshark.API.Functions.SearchArtist.SearchArtistResult;


public class GrooveGatorHelpers {

	private static GroovesharkClient client;
	
	public static void exportClientToXml() throws IOException 
	{
		String filename = new File(Utilities.GetAppPath(), "session.tmp").getPath();

		FileOutputStream out = new FileOutputStream(filename);
		ObjectOutputStream obj = new ObjectOutputStream(out);
		
		obj.writeObject(client);
		obj.close();
		out.flush();
		out.close();
	}
	public static void loadClientFromXml() throws Exception 
	{
		try {
			String filename = new File(Utilities.GetAppPath(), "session.tmp").getPath();
			FileInputStream in = new FileInputStream(filename);
			@SuppressWarnings("resource")
			ObjectInputStream obj = new ObjectInputStream(in);
			
			client = (GroovesharkClient)obj.readObject();
			
		} catch(Exception ex) {
			client = new GroovesharkClient(true);
		}
	}
	
	public static void tryConnect() throws Exception 
	{
		if (new File(Utilities.GetAppPath(), "session.tmp").exists()) 
		{
			loadClientFromXml();
			client.reloadGrooveFix();
		} 
		else 
		{
			client = new GroovesharkClient(true);
		}
	}
	
	public static void refreshSession() throws Exception 
	{
		client = new GroovesharkClient(true);
		exportClientToXml();
	}
	
	public static SearchArtistResult[] listSearch(String search) throws Exception 
	{
		SearchArtistResult[] results = client.SearchArtist(search).result.result;
		if (results.length == 0 ) 
			return null; // null indicates that we need to show a JOptionPane with a message
		return results;
	}
	
	public static String fixFilename(SearchArtistResult song) 
	{
		Preferences prefs = Preferences.userRoot().node(GrooveGator.class.getName());
		String prefString = prefs.get("OutputFormat", "<artist> - <song> - <album>");
		prefString = StringUtils.replace(prefString, "\\", "/");
		prefString = StringUtils.replace(prefString, "<artist>", song.ArtistName);
		prefString = StringUtils.replace(prefString, "<song>", song.Name);
		prefString = StringUtils.replace(prefString, "<album>", song.AlbumName);
		prefString = StringUtils.replace(prefString, "<year>", song.Year);
		prefString = StringUtils.replace(prefString, "<tracknum>", song.TrackNum);
		String filename, path = "";
		if (prefString.lastIndexOf("/") > 0)
		{
			path = prefString.substring(0, prefString.lastIndexOf("/"));
			filename = prefString.substring(prefString.lastIndexOf("/")+1, prefString.length());
			System.out.println("Filename: " + filename);
			System.out.println("Path: " + path);
		}
		else
		{
			filename = prefString; 
		}
		String tmpString = "";
		for (int i=0; i<filename.length(); i++) 
		{
			try {
				new File(filename.charAt(i) + "").getCanonicalFile();
				if (filename.charAt(i) != '/' && filename.charAt(i) != '\\') 
				{
					tmpString +=  filename.charAt(i);
				}
			} catch(Exception ex) {System.out.println("Exception"); }
		}
		String rootDownloadDirectoryPath = prefs.get("OutputDirectory", System.getProperty("user.dir"));
		File pathDirectory = new File(rootDownloadDirectoryPath + "/" + path);
		pathDirectory.mkdirs();
		return path + "/" +  tmpString.replace("\"", "");
	}
	
	public static GroovesharkClient getClient()
	{
		return client;
	}
}
