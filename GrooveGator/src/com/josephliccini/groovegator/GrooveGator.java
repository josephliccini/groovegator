package com.josephliccini.groovegator;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import net.miginfocom.swing.MigLayout;

import com.scilor.grooveshark.API.Base.GroovesharkAudioStream;
import com.scilor.grooveshark.API.Base.GroovesharkClient;
import com.scilor.grooveshark.API.Functions.AuthenticateUserEx.AuthenticateUserExResponse;
import com.scilor.grooveshark.API.Functions.SearchArtist.SearchArtistResult;
import com.scilor.grooveshark.API.Functions.UserGetPlaylists.PlaylistResult;

public class GrooveGator extends JFrame
{
	private static final long serialVersionUID = 5542110953480375013L;
	private static Object[][] resultsTableValues;
	private static Object[][] queueTableValues;
	private static Object[] tableHeaders;
	private static SearchArtistResult[] results;
	private Vector<SearchArtistResult> resultsInQueue;
	private Vector<SearchArtistResult> failedResultsInQueue;
	private PlaylistResult[] playlistResults;
	private static AtomicInteger songsToBeDownloaded;
	private static AtomicInteger percentageDivisor;
	private String version = "v0.1";

	private static AtomicInteger displayPercentage;
	JTabbedPane tabbedPane;
	JPanel wrapperPanel;
	JPanel northPanel;
	JPanel centerPanel;
	JPanel southPanel;
	
	JPanel playlistWrapperPanel;
	JPanel loginPanel;
	JPanel playlistPanel;

	JLabel chooseDirectoryLabel;
	JLabel searchLabel;
	JLabel usernameLabel;
	JLabel passwordLabel;
	JLabel playlistIdLabel;
	
	JTextField chooseDirectoryTextField;
	JTextField searchBar;
	JTextField usernameTextField;
	JTextField playlistByIdTextField;
	JPasswordField passwordTextField;
	
	JButton chooseDirectoryButton;
	JButton searchButton;
	JButton popularSongsButton;
	JButton downloadButton;
	JButton addToQueueButton;
	JButton removeFromQueueButton;
	JButton loginButton;
	JButton copyPlaylistToSearchButton;
	JButton copyPlaylistByIdToSearchButton;
	JButton selectAllButton;
	JButton invertSelectionButton;
	
	JComboBox<String> playlistComboBox;
	
	JProgressBar progressBar;
	JProgressBar searchProgressBar;
	JProgressBar playlistProgressBar;
	
	JScrollPane resultsScrollPane;
	JScrollPane queueScrollPane;

	JTable resultsTable;
	JTable queueTable;
	
	DefaultTableModel queueTableModel;
	DefaultTableModel resultsTableModel;
	
	JMenuBar menuBar;
	
	JFrame settingsFrame;
	Preferences prefs;
	
	public static void main(String[] args)
	{
		 SwingUtilities.invokeLater(new Runnable() {
		        public void run() {
		          createAndShowGUI();
		        }
		    });
	}	
	
	private static void createAndShowGUI()
	{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException e) {}
		catch (InstantiationException e) {}
		catch (IllegalAccessException e) {}
		catch (UnsupportedLookAndFeelException e) {}

		GrooveGator app = new GrooveGator("GrooveGator: An Open Source Java Grooveshark\u2122 Downloader");
		app.setSize(1000, 700);
		app.setVisible(true);
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		app.setLocationRelativeTo(null);
	}
	
	public GrooveGator(String s)
	{
		super(s);
		prefs = Preferences.userRoot().node(this.getClass().getName());
		tabbedPane = new JTabbedPane();
		tabbedPane.setTabPlacement(JTabbedPane.TOP);

		wrapperPanel = new JPanel(new MigLayout("", "[pref!][grow,fill]", "[][c,grow,fill]15[]"));
		northPanel = new JPanel(new MigLayout("", "[pref!][0:0,grow 80,fill][0:10,grow 5, center][pref!]", "[]15[]"));
		centerPanel = new JPanel(new BorderLayout());
		JPanel nestedCenterPanel = new JPanel(new GridLayout(1,1, 5, 5));
		JPanel nestedSouthPanel = new JPanel(new MigLayout("", "[5%][5%,left][40%,left][50%,center]", ""));
		centerPanel.setBorder(BorderFactory.createTitledBorder("Results & Queue:"));
		southPanel = new JPanel(new MigLayout("", "[pref!][0:0,grow 85,fill][0:0,grow 15,fill]", "[]15[]"));
		
		playlistWrapperPanel = new JPanel(new MigLayout("", "[pref!][grow]", "[]30[]"));
		loginPanel = new JPanel(new MigLayout("wrap", "[pref!][grow 15,center][grow 50]", "[]15[]"));
		loginPanel.setBorder(BorderFactory.createTitledBorder("Grooveshark\u2122 Login"));
		playlistPanel = new JPanel(new MigLayout("", "[pref!][grow 100][pref!]", "[]10[]"));
		playlistPanel.setBorder(BorderFactory.createTitledBorder("Playlist by ID"));

		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/com/josephliccini/groovegator/GrooveGator_final_32.png")));

		tableHeaders = new Object[] {"", "No.", "Song  Name", "Artist Name", "Album Name"};
		resultsTableValues = new Object[0][0];
		queueTableValues = new Object[0][0]; 
		resultsInQueue = new Vector<SearchArtistResult>();
		
		searchLabel = new JLabel("Search: ");
		searchBar = new JTextField("");
		searchBar.addActionListener(new SearchBarListener());
		searchButton = new JButton("Go");
		searchButton.addActionListener(new SearchButtonListener());
		popularSongsButton = new JButton("Popular Songs");
		popularSongsButton.addActionListener(new PopularSongsListener());
		
		queueTableModel = new GrooveGatorTableModel(queueTableValues, tableHeaders);
		
		resultsTableModel = new GrooveGatorTableModel(resultsTableValues, tableHeaders);	

		resultsTable = createTable(resultsTableModel);
		queueTable = createTable(queueTableModel);
		
		menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new ExitMenuItemListener());
		fileMenu.add(exitMenuItem);
		JMenu settingsMenu = new JMenu("Settings");
		JMenuItem configureSettingsMenuItem = new JMenuItem("Configure Settings...");
		configureSettingsMenuItem.addActionListener(new ConfigureSettingsMenuItemListener());
		JMenuItem reconnectMenuItem = new JMenuItem("Reconnect");
		reconnectMenuItem.addActionListener(new ReconnectMenuItemListener());

		JMenu aboutMenu = new JMenu("About");
		JMenuItem aboutMenuItem = new JMenuItem("About this software...");
		aboutMenuItem.addActionListener(new AboutMenuItemListener());
		aboutMenu.add(aboutMenuItem);

		settingsMenu.add(configureSettingsMenuItem);
		settingsMenu.add(reconnectMenuItem);
		menuBar.add(fileMenu);
		menuBar.add(settingsMenu);
		menuBar.add(aboutMenu);

		resultsScrollPane = new JScrollPane(resultsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		queueScrollPane = new JScrollPane(queueTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		nestedCenterPanel.add(resultsScrollPane);
		nestedCenterPanel.add(queueScrollPane);
		
		downloadButton = new JButton("Download");
		downloadButton.addActionListener(new DownloadButtonListener());
		
		addToQueueButton = new JButton("Add to Download Queue");
		addToQueueButton.addActionListener(new AddToDownloadQueueListener());
		selectAllButton = new JButton("Select All");
		selectAllButton.addActionListener(new SelectionListener());
		invertSelectionButton  = new JButton("Invert Selection");
		invertSelectionButton.addActionListener(new SelectionListener());
		removeFromQueueButton = new JButton("Remove From Download Queue");
		removeFromQueueButton.addActionListener(new RemoveFromDownloadQueueListener());
		nestedSouthPanel.add(selectAllButton);
		nestedSouthPanel.add(invertSelectionButton);
		nestedSouthPanel.add(addToQueueButton);
		nestedSouthPanel.add(removeFromQueueButton, "wrap");
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		
		searchProgressBar = new JProgressBar();
		playlistProgressBar = new JProgressBar();
		
		usernameLabel = new JLabel("Username: ");
		passwordLabel = new JLabel("Password: ");
		usernameTextField = new JTextField("");
		usernameTextField.addActionListener(new UsernameAndPasswordListener());
		passwordTextField = new JPasswordField("");
		passwordTextField.addActionListener(new UsernameAndPasswordListener());
		playlistComboBox = new JComboBox<String>();
		
		loginButton = new JButton("Login & Fetch");
		loginButton.addActionListener(new LoginButtonListener());
		copyPlaylistToSearchButton =  new JButton("Copy to Search");
		copyPlaylistToSearchButton.addActionListener(new CopyPlaylistToSearchButtonListener());
		loginPanel.add(usernameLabel, "right");
		loginPanel.add(usernameTextField, "span 2, growx, wrap");
		loginPanel.add(passwordLabel, "right");
		loginPanel.add(passwordTextField, "span 2, growx, wrap");
		loginPanel.add(loginButton, "span 4, center");
		
		loginPanel.add(playlistComboBox, "span 4, growx, wrap");
		loginPanel.add(copyPlaylistToSearchButton, "span 4, right");
		
		playlistIdLabel = new JLabel("Enter Playlist ID: ");
		playlistByIdTextField = new JTextField();
		playlistByIdTextField.addActionListener(new PlaylistByIdListener());
		copyPlaylistByIdToSearchButton = new JButton("Copy to Search");
		copyPlaylistByIdToSearchButton.addActionListener(new CopyPlaylistToSearchButtonListener());
		
		playlistPanel.add(playlistIdLabel);
		playlistPanel.add(playlistByIdTextField, "span 3, growx");
		playlistPanel.add(copyPlaylistByIdToSearchButton, "wrap");

		northPanel.add(searchLabel, "gap para");
		northPanel.add(searchBar, "growx");
		northPanel.add(searchButton);
		northPanel.add(popularSongsButton, "wrap");
		northPanel.add(searchProgressBar, "span 4, growx");
		northPanel.setBorder(BorderFactory.createTitledBorder("Query Grooveshark.com"));
		centerPanel.add(nestedCenterPanel, BorderLayout.CENTER);
		centerPanel.add(nestedSouthPanel, BorderLayout.SOUTH);
		southPanel.add(downloadButton, "cell 0 1, center, growx 0");
		southPanel.add(progressBar, "span 2, growx");
		wrapperPanel.add(northPanel,"span, growx, wrap para");
		wrapperPanel.add(centerPanel,"span, growx, wrap para");
		wrapperPanel.add(southPanel,"span, growx, wrap");
		
		playlistWrapperPanel.add(playlistProgressBar, "span 3, growx, wrap");
		playlistWrapperPanel.add(loginPanel, "span 3, growx, wrap para");
		playlistWrapperPanel.add(playlistPanel, "span 3, growx, wrap para");

		tabbedPane.addTab("Download", wrapperPanel);
		tabbedPane.addTab("Playlists", playlistWrapperPanel);
		this.add(tabbedPane);
		this.setJMenuBar(menuBar);
		
		connectToGrooveshark();
	}
	
	private void connectToGrooveshark()
	{	
		int attempts = 0;
		try {
			GrooveGatorHelpers.refreshSession();
			while (attempts < 3)
			{
				++attempts;
				GrooveGatorHelpers.tryConnect();
				System.out.println("Connected!");
				break;
			}
		} catch (Exception e) {
			if (e.getLocalizedMessage().contains("Missing GrooveFix.xml!"))
				downloadGrooveFix();
		}
	}
	
	private void downloadGrooveFix()
	{
		try {
			URL groovefixSite= new URL("http://www.scilor.com/grooveshark/xml/GrooveFix.xml");
			ReadableByteChannel byteChannel = Channels.newChannel(groovefixSite.openStream());
			FileOutputStream fos = new FileOutputStream("GrooveFix.xml");
			fos.getChannel().transferFrom(byteChannel, 0, Long.MAX_VALUE);
			fos.close();
			JOptionPane.showMessageDialog(centerPanel, "Downloaded 'GrooveFix.xml' successfully!\nNOTE: This file is necessary to GrooveGator, and adivised not to be deleted.");
			GrooveGatorHelpers.refreshSession();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	private void downloadSong(SearchArtistResult song) 
	{
		System.out.println("Download: " + song.ArtistName + " - " + song.Name);
		try {
			GroovesharkAudioStream stream = GrooveGatorHelpers.getClient().GetMusicStream(song.SongID);
			String filename = GrooveGatorHelpers.fixFilename(song);
			filename+=".mp3";
			
			String outputPath = prefs.get("OutputDirectory", System.getProperty("user.dir"));
			FileOutputStream writer = new FileOutputStream(outputPath + "/" + filename);
			int readBytes = 0;
			int pos=0;
			int percentage = 0;
			int prevPercentage = 0;
			
			String lastOutput = null;
			do {
				byte[] buffer = new byte[4096];
				readBytes = stream.Stream().read(buffer);
				pos += readBytes;
				progressBar.setValue((int)Math.ceil((displayPercentage.doubleValue())));
				if (readBytes > 0) writer.write(buffer, 0, readBytes);
				percentage = (100 * pos / (stream.Length() - 1));
				if (percentage > prevPercentage + 4) 
				{
					lastOutput = percentage + "%" + " \"" + filename + "\"";
					System.out.println(lastOutput);
					displayPercentage.addAndGet(-prevPercentage / percentageDivisor.get());
					prevPercentage = percentage;
					displayPercentage.addAndGet(percentage / percentageDivisor.get());
					System.out.println("Total Percentage: " + displayPercentage.get());
				}
			} while (readBytes > 0);
			progressBar.setValue((int)Math.ceil((displayPercentage.doubleValue()))+2);

			stream.MarkSongAsDownloaded();
			
			writer.flush();
			writer.close();
			stream.Stream().close();
		} catch (Exception e) {
			System.out.println("Exception in downloadSong!");
			resultsInQueue.remove(song);
			failedResultsInQueue.add(song);
			displayPercentage.addAndGet(100 / percentageDivisor.get());
			e.printStackTrace();
		}

	}
	
	private JTable createTable(DefaultTableModel tableModel)
	{
		final JTable table = new JTable(tableModel)
		{
			private static final long serialVersionUID = 991867857464888611L;

			@Override
			public boolean isCellEditable(int rowIndex, int colIndex)
			{
				return (colIndex == 0);
			}
			
		};

		for(int i = 0; i < 5; ++i)
		{
			TableColumn column = table.getColumnModel().getColumn(i);
			if (i == 0)
				column.setPreferredWidth(20);
			else if (i == 1)
				column.setPreferredWidth(50);
			else 
				column.setPreferredWidth(300);
		}
		
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent event)
			{
				if (!event.getValueIsAdjusting())
				{
					try {
						if (table.getSelectedColumn() > 0)
						{
							int selectedRow = table.getSelectedRow();
							boolean curValue = (boolean) table.getModel().getValueAt(selectedRow, 0);
							table.setValueAt(!curValue, selectedRow, 0);
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						System.out.println("ArrayIndexOutOfBounds Exception Thrown");
						System.out.println("Ignoring (this is from the queueTableModel detecting change in row state)");
					}
				}
			}
		});
		
		table.setRowHeight(20);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		table.setAutoCreateColumnsFromModel(false);
		return table;
	}
	
	private void fillSearchTable(SearchArtistResult[] results)
	{
			resultsTableValues = new Object[results.length][5];
			for (int i = 0; i < results.length; ++i)
			{
					resultsTableValues[i][0] = false;
					resultsTableValues[i][1] = i+1;
					resultsTableValues[i][2] = results[i].SongName;
					resultsTableValues[i][3] = results[i].ArtistName;
					resultsTableValues[i][4] = results[i].AlbumName;
			}
			resultsTableModel.setDataVector(resultsTableValues, tableHeaders);
	}
	
	private void playlistToSearch(int playlistId) 
	{
		GroovesharkClient client = GrooveGatorHelpers.getClient();
		try {
			SearchArtistResult[] playlistResults = client.GetPlaylistSongs(playlistId).result.Songs;
			if (playlistResults == null)
			{
				JOptionPane.showMessageDialog(null, "Error copying playlist to Search!");
				return;
			}
			results = playlistResults.clone();
			fillSearchTable(results);
		} catch (Exception e1) {
			System.out.println("Error retriving songs from the playlist");
			e1.printStackTrace();
		}
	}
	
	private class ConfigureSettingsMenuItemListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if (settingsFrame == null)
			{
				settingsFrame = new GrooveGatorSettings("Configure Settings");
				settingsFrame.setLocationRelativeTo(centerPanel);
			}
			else
				settingsFrame.setVisible(true);
		}
	}

	private class SearchButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			String query = searchBar.getText();
			searchButton.setEnabled(false);
			searchProgressBar.setIndeterminate(true);
			SearchTask searchTask = new SearchTask(query);
			searchTask.execute();
		}

	}
	
	private class SearchBarListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			searchButton.doClick();
		}
		
	}
	
	private class DownloadButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (resultsInQueue.size() <= 0)
				return;
			failedResultsInQueue = new Vector<SearchArtistResult>();
			displayPercentage = new AtomicInteger(0);
			downloadButton.setEnabled(false);
			songsToBeDownloaded = new AtomicInteger(resultsInQueue.size());
			percentageDivisor = new AtomicInteger(resultsInQueue.size());
			for(int i = resultsInQueue.size()-1; i >= 0; --i)
			{
					DownloadTask downloadTask = new DownloadTask(resultsInQueue.get(i));
					downloadTask.execute();
					queueTableModel.removeRow(i);
			}
		}
	}
	
	private class AddToDownloadQueueListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			for(int i = 0; i < resultsTable.getRowCount(); ++i)
			{
				if ((boolean)resultsTable.getValueAt(i, 0))
				{
					resultsInQueue.add(results[i]);
					queueTableModel.addRow(resultsTableValues[i]);
					resultsTable.getModel().setValueAt(false, i, 0);
				}
			}
		}
	}
	
	private class RemoveFromDownloadQueueListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			for(int i = queueTable.getRowCount()-1; i >= 0; --i)
			{
				if ((boolean)queueTable.getValueAt(i, 0))
				{
					resultsInQueue.remove(i);
					queueTableModel.removeRow(i);;
				}
			}
		}
	}
	
	private class AboutMenuItemListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			String message = "GrooveGator: An Open-Source Java Grooveshark\u2122 Downloader\n"
					+ "Version: " + version + "\n"
					+ "License: GPLv3" + "\n\n"
					+ "I am not responsible for violating any terms of use with Grooveshark\u2122.com.\n"
					+ "This is more or less a proof of concept. I am not associated with Grooveshark\u2122 in any way.\n"
					+ "Any bugs or problems please file an issue at: "
					+ "http://www.github.com/josephliccini\n"
					+ "A huge thanks to SciLor and his API, without his help GrooveGator would not be possible!\n"
					+ "Please consider donating to SciLor at: http://www.scilor.com/donate.html\n";
			
			ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/com/josephliccini/groovegator/GrooveGator_final_About.png")));
			JOptionPane.showMessageDialog(centerPanel, message, "About this software", JOptionPane.INFORMATION_MESSAGE, icon);
		}
	}
	
	private class ExitMenuItemListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			GrooveGator.this.dispose();
			System.exit(0);
		}
	}
	
	private class ReconnectMenuItemListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			connectToGrooveshark();
		}
	}
	
	private class PlaylistByIdListener implements ActionListener
	{
		@Override 
		public void actionPerformed(ActionEvent e)
		{
			copyPlaylistByIdToSearchButton.doClick();
		}
	}
	
	private class UsernameAndPasswordListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			loginButton.doClick();
		}
	}
	
	private class LoginButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			playlistComboBox.removeAllItems();
			String username = usernameTextField.getText();
			char[] password = passwordTextField.getPassword();
			LoginTask loginTask = new LoginTask(username, password);
			playlistProgressBar.setIndeterminate(true);
			loginTask.execute();
			
		}
	}
	
	private class CopyPlaylistToSearchButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			int playlistId = 0;
			playlistProgressBar.setIndeterminate(true);
			if (e.getSource() == copyPlaylistToSearchButton)
			{
				copyPlaylistToSearchButton.setEnabled(false);
				int index = playlistComboBox.getSelectedIndex();
				PlaylistResult selectedPlaylist = playlistResults[index];
				playlistId = selectedPlaylist.PlaylistID;
			}
			else if (e.getSource() == copyPlaylistByIdToSearchButton)
			{
				copyPlaylistByIdToSearchButton.setEnabled(false);
				try {
					playlistId = Integer.parseInt(playlistByIdTextField.getText());
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(playlistWrapperPanel, "Error, invalid Playlist Id!");
					ex.printStackTrace();
				}
			}
			PlaylistByIdTask playlistByIdTask = new PlaylistByIdTask(playlistId);
			playlistByIdTask.execute();
		}
	}
	
	private class SelectionListener implements ActionListener 
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() == invertSelectionButton)
			{
				for(int i = 0; i < resultsTable.getRowCount(); ++i)
				{
					boolean currentValue = (boolean) resultsTable.getValueAt(i, 0);
					resultsTable.getModel().setValueAt(!currentValue, i, 0);
				}
			}
			else if (e.getSource() == selectAllButton)
			{
				for(int i = 0; i < resultsTable.getRowCount(); ++i)
				{
					resultsTable.getModel().setValueAt(true, i, 0);
				}
			}
		}
	}
	
	private class PopularSongsListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			popularSongsButton.setEnabled(false);
			searchProgressBar.setIndeterminate(true);
			PopularSongsTask task = new PopularSongsTask();
			task.execute();
		}
	}
	
	class DownloadTask extends SwingWorker<Void, Void>
	{
		SearchArtistResult song;

		public DownloadTask(SearchArtistResult song)  
		{
			super();
			this.song = song;
		}
		
		@Override
		public Void doInBackground() throws Exception
		{
			downloadSong(song);
			return null;
		}
		
		@Override
		public void done()
		{
			resultsInQueue.remove(this.song);
			songsToBeDownloaded.getAndDecrement();
		
			Toolkit.getDefaultToolkit().beep();

            //Java will release this song from memory so it can be played right away
            this.song = null;
			if (songsToBeDownloaded.get() == 0)
			{
			    downloadButton.setEnabled(true);
				displayPercentage = new AtomicInteger(0);
				progressBar.setValue(displayPercentage.get());
				if (failedResultsInQueue.size() > 0)
				{
					StringBuilder sb = new StringBuilder("");
					for (SearchArtistResult song: failedResultsInQueue)
						sb.append(song.Name + " - " + song.ArtistName + " - " + song.AlbumName + "\n");
					int response = JOptionPane.showConfirmDialog(centerPanel, "Retry Downloading?\n" + 
						sb.toString(), "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (response == JOptionPane.YES_OPTION)
					{
						@SuppressWarnings("unchecked")
						Vector<SearchArtistResult> tempList = (Vector<SearchArtistResult>) resultsInQueue.clone();
						resultsInQueue.clear();
						resultsInQueue.addAll(failedResultsInQueue);
						downloadButton.doClick();
						resultsInQueue = tempList;
					}
				}
			}
		}
	}
	
	class SearchTask extends SwingWorker<Void, Void>
	{
		String query;
		
		public SearchTask(String query)  
		{
			super();
			this.query = query;
		}
		
		@Override
		public Void doInBackground() throws Exception
		{
			try {
				SearchArtistResult[] tempResults = GrooveGatorHelpers.listSearch(query);
				if (tempResults == null)
				{
					JOptionPane.showMessageDialog(null, "No Results found!");
					return null;
				}
				results = tempResults.clone();
				fillSearchTable(results);
		} catch (Exception e1) {
				e1.printStackTrace();
			}
			return null;
		}
		
		@Override
		public void done()
		{
			Toolkit.getDefaultToolkit().beep();
			searchProgressBar.setIndeterminate(false);
			searchButton.setEnabled(true);
		}
	}
	
	class LoginTask extends SwingWorker<Void, Void>
	{
		String username;
		char[] password;
		
		public LoginTask(String username, char[] password)  
		{
			super();
			this.username = username;
			this.password = password;
		}
		
		@Override
		public Void doInBackground() throws Exception
		{
			try {
				GroovesharkClient client = GrooveGatorHelpers.getClient();
				AuthenticateUserExResponse ex = client.Login(username, new String(password));
				int userId = ex.result.userID;
				PlaylistResult[] playlists = client.GetPlaylists(userId).result.Playlists;
				playlistResults = playlists;
				for (PlaylistResult p: playlists)
					playlistComboBox.addItem(p.Name);
				Toolkit.getDefaultToolkit().beep();
			} catch (Exception e1) {
				System.out.println("Client could not login user or pass");
			}
			return null;
		}
		
		@Override
		public void done()
		{
			loginButton.setEnabled(true);
			playlistProgressBar.setIndeterminate(false);
			Toolkit.getDefaultToolkit().beep();
		}
	}	
	
	class PlaylistByIdTask extends SwingWorker<Void, Void>
	{
		int playlistId; 

		public PlaylistByIdTask(int playlistId)  
		{
			super();
			this.playlistId = playlistId;
		}
		
		@Override
		public Void doInBackground() throws Exception
		{
			playlistToSearch(playlistId);
			return null;
		}

		@Override
		public void done()
		{
			copyPlaylistByIdToSearchButton.setEnabled(true);
			copyPlaylistToSearchButton.setEnabled(true);
			playlistProgressBar.setIndeterminate(false);
			Toolkit.getDefaultToolkit().beep();
		}
	}	
	
	class PopularSongsTask extends SwingWorker<Void, Void>
	{
		@Override
		public Void doInBackground() throws Exception
		{
			GroovesharkClient client = GrooveGatorHelpers.getClient();
			try {
				results = client.PopularSongs().result.Songs;
				fillSearchTable(results);
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(centerPanel, "Error loading Popular Songs");
				e1.printStackTrace();
			}
			return null;
		}
		
		public void done()
		{
			popularSongsButton.setEnabled(true);
			searchProgressBar.setIndeterminate(false);
			Toolkit.getDefaultToolkit().beep();
		}
		
	}
}
	
