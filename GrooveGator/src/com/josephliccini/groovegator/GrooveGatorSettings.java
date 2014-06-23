package com.josephliccini.groovegator;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.util.Arrays;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;

public class GrooveGatorSettings extends JFrame implements WindowFocusListener {
	
	private static final long serialVersionUID = 1555513139110891669L;

	JTextField defaultOutputDirectoryTextField;
	JTextField previewTextField;
	JTextField previewExplanationTextField;
	JButton chooseDefaultOutputDirectoryButton;
	JButton saveFormatButton;
	
	JComboBox<String> specifyFormatComboBox;
	
	JLabel defaultOutputDirectoryLabel;
	JLabel specifyFormatLabel;
	JLabel previewLabel;
	
	
	JFileChooser chooser = new JFileChooser();
	JPanel outputDirectoryPanel;
	JPanel fileFormatPanel;
	Preferences prefs;
	
	String[] templates = new String[] { "<artist> - <song> - <album>", 
			"<artist> - <song> - <album>(<year>)", "<artist>/<album>/<artist> - <song> - <album>", 
			"<artist>/<album>(<year>)/<artist> - <song> - <album>(<year>)"};
	
	public GrooveGatorSettings(String s)  
	{
		super(s);
		this.setLayout(new BorderLayout());
		this.addWindowFocusListener(this);
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/GrooveGator_final_32.png")));
		prefs = Preferences.userRoot().node(GrooveGator.class.getName());
		
		outputDirectoryPanel = new JPanel(new MigLayout("", "[pref!][0:0,grow 85,fill][0:0,grow 15,fill]", "[]15[]"));
		fileFormatPanel = new JPanel(new MigLayout("", "[pref!][0:0,grow 85,fill][0:0,grow 15,fill]", "[]15[]"));
		
		defaultOutputDirectoryLabel = new JLabel("Choose Default Output Directory: ");
		defaultOutputDirectoryTextField = new JTextField(prefs.get("OutputDirectory", System.getProperty("user.dir")));
		defaultOutputDirectoryTextField.setEditable(false);
		chooseDefaultOutputDirectoryButton = new JButton("Browse...");
		chooseDefaultOutputDirectoryButton.addActionListener(new ChooseDirectoryListener());
		
		specifyFormatLabel = new JLabel("Specify output format:");
		saveFormatButton = new JButton("Save");
		saveFormatButton.addActionListener(new SaveFileFormatListener());
		specifyFormatComboBox = new JComboBox<String>(templates);
		initializeFileFormatComboBox();
		
		specifyFormatComboBox.setEditable(true);
		fileFormatPanel.setBorder(BorderFactory.createTitledBorder("File Naming Pattern"));
		previewLabel = new JLabel("Preview: ");
		previewTextField = new JTextField(generatePreview(prefs.get("OutputFormat", "<artist> - <song> - <album>")));
		previewTextField.setEditable(false);
		previewExplanationTextField = new JTextField("Acceptable patterns include: <artist>, <song>, <album>, <year>, <tracknum>. "
				+ "Backslash \"\\\" or Forward Slash \"/\" will create subdirectories.");
		previewExplanationTextField.setEditable(false);

		
		fileFormatPanel.add(specifyFormatLabel);
		fileFormatPanel.add(specifyFormatComboBox, "growx");
		fileFormatPanel.add(saveFormatButton, "wrap");
		fileFormatPanel.add(previewLabel);
		fileFormatPanel.add(previewTextField, "span 2, growx, wrap");
		fileFormatPanel.add(previewExplanationTextField, "span 3, growx, wrap");
		
		outputDirectoryPanel.add(defaultOutputDirectoryLabel);
		outputDirectoryPanel.add(defaultOutputDirectoryTextField, "growx");
		outputDirectoryPanel.add(chooseDefaultOutputDirectoryButton);
		outputDirectoryPanel.setBorder(BorderFactory.createTitledBorder("Default Download Directory"));
		
		this.add(outputDirectoryPanel, BorderLayout.NORTH);
		this.add(fileFormatPanel, BorderLayout.CENTER);

		this.setVisible(true);
		this.setSize(1000, 250);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setAutoRequestFocus(true);
		this.setAlwaysOnTop(true);
	}
	
	private void initializeFileFormatComboBox()
	{
		String custom = prefs.get("OutputFormat", null);
		if (custom != null)
		{
			if (!Arrays.asList(templates).contains(custom))
			{
				specifyFormatComboBox.addItem(custom);
			}
			specifyFormatComboBox.setSelectedItem(custom);
		}
		specifyFormatComboBox.addItemListener(new ChooseFileFormatListener());
	}
	
	private String generatePreview(String outputFormatString) 
	{
		String s = outputFormatString;
		s = StringUtils.replace(s, "<artist>", "David Guetta");
		s = StringUtils.replace(s, "<song>", "Titanium");
		s = StringUtils.replace(s, "<album>", "Nothing but the Beat");
		s = StringUtils.replace(s, "<year>", "2010");
		s = StringUtils.replace(s, "<tracknum>", "1");
		StringBuilder sb = new StringBuilder(s);
		sb.append(".mp3");

		return sb.toString();
	}

	private class ChooseDirectoryListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = chooser.showOpenDialog(GrooveGatorSettings.this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File outputDirectory = new File(chooser.getSelectedFile().getAbsolutePath() + "/");
				prefs.put("OutputDirectory", outputDirectory.getAbsolutePath());
				defaultOutputDirectoryTextField.setText(outputDirectory.getAbsolutePath());
			}
		}
	}
	
	private class ChooseFileFormatListener implements ItemListener
	{
		@Override
		public void itemStateChanged(ItemEvent e) 
		{
			previewTextField.setText(generatePreview((String)e.getItem()));
		}
		
	}

	private class SaveFileFormatListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			String custom = prefs.get("OutputFormat", null);
			if (custom != null)
			{
				if (!Arrays.asList(templates).contains(custom))
				{
					specifyFormatComboBox.removeItem(custom);
				}
			}
			String s = (String) specifyFormatComboBox.getSelectedItem();
			
			prefs.put("OutputFormat", s); 
				
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(outputDirectoryPanel, "Format Saved!", "Settings", JOptionPane.INFORMATION_MESSAGE, null);
			initializeFileFormatComboBox();
			GrooveGatorSettings.this.revalidate();
			GrooveGatorSettings.this.repaint();
		}
	}
	@Override
	public void windowGainedFocus(WindowEvent e) {}

	@Override
	public void windowLostFocus(WindowEvent e) 
	{
		if (e.getNewState() != WindowEvent.WINDOW_CLOSED)
		{
			GrooveGatorSettings.this.requestFocus();
			setAlwaysOnTop(true);
		}
	}
	
}
