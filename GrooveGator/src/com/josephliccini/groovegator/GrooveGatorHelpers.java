package com.josephliccini.groovegator;

import java.awt.HeadlessException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

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
			path = prefString.substring(0, index);
			path = StringUtils.replace(path, "<artist>", StringUtils.remove(StringUtils.remove(song.ArtistName, '/'), '\\'));
			path = StringUtils.replace(path, "<song>", StringUtils.remove(StringUtils.remove(song.SongName, '/'), '\\'));
			path = StringUtils.replace(path, "<album>", StringUtils.remove(StringUtils.remove(song.AlbumName, '/'), '\\'));
			path = StringUtils.replace(path, "<year>", StringUtils.remove(StringUtils.remove(song.Year, '/'), '\\'));
			path = StringUtils.replace(path, "<tracknum>", StringUtils.remove(StringUtils.remove(song.TrackNum, '/'), '\\'));
		}
		else
		{
			filename = prefString;
		}
		filename = StringUtils.replace(filename, "<artist>", StringUtils.remove(StringUtils.remove(song.ArtistName, '/'), '\\'));
		filename = StringUtils.replace(filename, "<song>", StringUtils.remove(StringUtils.remove(song.SongName, '/'), '\\'));
		filename = StringUtils.replace(filename, "<album>", StringUtils.remove(StringUtils.remove(song.AlbumName, '/'), '\\'));
		filename = StringUtils.replace(filename, "<year>", StringUtils.remove(StringUtils.remove(song.Year, '/'), '\\'));
		filename = StringUtils.replace(filename, "<tracknum>", StringUtils.remove(StringUtils.remove(song.TrackNum, '/'), '\\'));
			
		System.out.println("Filename: " + filename);
		System.out.println("Path: " + path);
		
		filename = filename.replace("\"", "");
		String rootDownloadDirectoryPath = prefs.get("OutputDirectory", System.getProperty("user.dir"));
		File pathDirectory = new File(rootDownloadDirectoryPath, path);
		if ((pathDirectory.getAbsolutePath() + "/" + filename).length() > 255)
			return fixLength(pathDirectory, filename).toString();
		pathDirectory.mkdirs();
		return pathDirectory.toString() + "/" + filename;
	}
	
	private static String fixLength(File pathDirectory, String filename)
	{
		File temp = new File(pathDirectory, filename);
		try {
			if (temp.getCanonicalPath().length() > 255)
			{
				temp = new File(temp.getCanonicalPath().substring(0, 250));
				JOptionPane.showMessageDialog(null, "Path and Filename for song: \n" + filename + "\n" 
						+ "Are too long... shortening filename, you can find your file at:\n"
						+ temp.getCanonicalPath());
			}
		} catch (HeadlessException | IOException e) {
			e.printStackTrace();
		}
		temp.getParentFile().mkdirs();
		return temp.toString();
	}
	
	public static GroovesharkClient getClient()
	{
		return client;
	}
}
