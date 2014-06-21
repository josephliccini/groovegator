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
		int index;
		String filename,path = "";
		if ((index = prefString.lastIndexOf("/")) > 0)
		{
			filename = prefString.substring(index+1);
			filename = StringUtils.replace(filename, "<artist>", StringUtils.remove(StringUtils.remove(song.ArtistName, '/'), '\\'));
			filename = StringUtils.replace(filename, "<song>", StringUtils.remove(StringUtils.remove(song.SongName, '/'), '\\'));
			filename = StringUtils.replace(filename, "<album>", StringUtils.remove(StringUtils.remove(song.AlbumName, '/'), '\\'));
			filename = StringUtils.replace(filename, "<year>", StringUtils.remove(StringUtils.remove(song.Year, '/'), '\\'));
			filename = StringUtils.replace(filename, "<tracknum>", StringUtils.remove(StringUtils.remove(song.TrackNum, '/'), '\\'));
			
			path = prefString.substring(0, index);
			path = StringUtils.replace(path, "<artist>", StringUtils.remove(StringUtils.remove(song.ArtistName, '/'), '\\'));
			path = StringUtils.replace(path, "<song>", StringUtils.remove(StringUtils.remove(song.SongName, '/'), '\\'));
			path = StringUtils.replace(path, "<album>", StringUtils.remove(StringUtils.remove(song.AlbumName, '/'), '\\'));
			path = StringUtils.replace(path, "<year>", StringUtils.remove(StringUtils.remove(song.Year, '/'), '\\'));
			path = StringUtils.replace(path, "<tracknum>", StringUtils.remove(StringUtils.remove(song.TrackNum, '/'), '\\'));
		}
		else
		{
			prefString = StringUtils.replace(prefString, "<artist>", StringUtils.remove(StringUtils.remove(song.ArtistName, '/'), '\\'));
			prefString = StringUtils.replace(prefString, "<song>", StringUtils.remove(StringUtils.remove(song.SongName, '/'), '\\'));
			prefString = StringUtils.replace(prefString, "<album>", StringUtils.remove(StringUtils.remove(song.AlbumName, '/'), '\\'));
			prefString = StringUtils.replace(prefString, "<year>", StringUtils.remove(StringUtils.remove(song.Year, '/'), '\\'));
			prefString = StringUtils.replace(prefString, "<tracknum>", StringUtils.remove(StringUtils.remove(song.TrackNum, '/'), '\\'));
			filename = prefString;
		}
		System.out.println("Filename: " + filename);
		System.out.println("Path: " + path);
		
		StringBuilder builder = new StringBuilder("");
		for (int i=0; i<filename.length() && i < 255; ++i) 
		{
			try {
				new File(filename.charAt(i) + "").getCanonicalFile();
				if (filename.charAt(i) != '/' && filename.charAt(i) != '\\') 
				{
					builder.append(filename.charAt(i));
				}
			} catch(Exception ex) {System.out.println("Exception"); }
		}

		String rootDownloadDirectoryPath = prefs.get("OutputDirectory", System.getProperty("user.dir"));
		File pathDirectory = new File(rootDownloadDirectoryPath, path);
		pathDirectory.mkdirs();
		return path + "/" +  builder.toString().replace("\"", "");
	}
	
	public static GroovesharkClient getClient()
	{
		return client;
	}
}
