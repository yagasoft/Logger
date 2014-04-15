/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Logger/com.yagasoft.logger/Logger.java
 * 
 *			Modified: Apr 15, 2014 (7:54:46 AM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.logger;


import java.awt.BorderLayout;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;


/**
 * The Class Logger.
 */
public class Logger
{
	
	/** Logger window is visible. */
	private static boolean				visible			= false;
	
	/** Constant: Log entry divider. */
	private static final String			divider			= "----------------------------------------------------------------------------------"
																+ "------------------------------------------------------------------------\n";
	
	/** Section open flag. */
	private static boolean				sectionOpen		= false;
	
	/** Constant: LogsFolder. */
	private static final Path			logsFolder		= Paths.get(System.getProperty("user.dir") + "/var/logs/");
	
	/** Log file path. */
	private static Path					logFile;
	
	/** Stream to log file. */
	private static OutputStream			stream;
	
	/** Writer to log file. */
	private static OutputStreamWriter	writer;
	
	/** Logger window frame. */
	private static JFrame				frame			= initFrame();
	
	/** Logger content pane. */
	private static JPanel				contentPane		= initPanel();
	
	/** Logger text area. */
	private static JTextArea			textArea		= initLog();
	
	/** The current section title. */
	private static String				currentTitle	= "";
	
	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args)
	{
		showLogger();
		
		post("TEST1!!!");
		post("TEST2!!!");
		
		hideLogger();
		
		post("TEST3!!!");
		post("TEST4!!!");
		
		showLogger();
	}
	
	/**
	 * Show logger window.
	 */
	public static void showLogger()
	{
		visible = true;
		frame.setVisible(visible);
	}
	
	/**
	 * Hide logger window.
	 */
	public static void hideLogger()
	{
		visible = false;
		frame.setVisible(visible);
	}
	
	/**
	 * Inits the frame.
	 * 
	 * @return the frame
	 */
	private static JFrame initFrame()
	{
		frame = new JFrame("Logger window");
		frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		frame.setBounds(25, 25, 800, 512);
		frame.setVisible(visible);
		
		return frame;
	}
	
	/**
	 * Inits the panel.
	 * 
	 * @return the panel
	 */
	private static JPanel initPanel()
	{
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		
		frame.setContentPane(contentPane);
		frame.revalidate();
		
		return contentPane;
	}
	
	/**
	 * Inits the log text area.
	 * 
	 * @return the text area
	 */
	private static JTextArea initLog()
	{
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(false);
		
		JScrollPane scroller = new JScrollPane(textArea);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		DefaultCaret caret = (DefaultCaret) textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		contentPane.add(scroller);
		frame.revalidate();
		
		// post something and create a log file for this session.
		post("NEW LOG!");
		newLogFile();
		
		return textArea;
	}
	
	/**
	 * New titled log section,
	 * which can be used to post related entries without bothering with remembering if a related section was open before.
	 */
	public static void newTitledSection(String title)
	{
		if ( !Logger.currentTitle.equals(title))
		{
			currentTitle = title;
			newSection(">>>>> New section: " + title);
		}
	}
	
	/**
	 * New log section.
	 */
	public static void newSection()
	{
		newSection("");
	}
	
	/**
	 * New log section, and post the passed entry right after.
	 * 
	 * @param entry
	 *            Entry.
	 */
	public static void newSection(String entry)
	{
		sectionOpen = true;
		append(divider);
		
		// if nothing was passed, then write nothing.
		if ((entry != null) && (entry.length() > 0))
		{
			newEntry(entry);
		}
	}
	
	/**
	 * New log entry.
	 * 
	 * @param entry
	 *            Entry.
	 */
	public static void newEntry(String entry)
	{
		// must make sure to write in a section.
		if (sectionOpen)
		{
			// write the time stamp, then the entry next to it.
			append(getTimeStamp() + ": ");
			append(entry + "\n");
		}
		else
		{	// create new section if necessary.
			newSection(entry);
		}
	}
	
	/**
	 * End log section.
	 */
	public static void endSection()
	{
		endSection("");
	}
	
	/**
	 * End log section, and write the passed entry.
	 * 
	 * @param entry
	 *            Entry.
	 */
	public static void endSection(String entry)
	{
		currentTitle = "";
		
		// want to end a section when a section is not open?
		if ( !sectionOpen)
		{
			// open a section, then close it right after.
			newSection(entry);
			endSection();
			return;		// done!
		}
		
		// nothing to write?
		if ((entry != null) && (entry.length() > 0))
		{
			newEntry(entry);
		}
		
		sectionOpen = false;		// section is done!
	}
	
	/**
	 * Post an entry in a separate section.
	 * 
	 * @param text
	 *            Text to post.
	 */
	public static void post(String text)
	{
		newSection(text);
		endSection();
	}
	
	/**
	 * Append text to the log as is, then write it to log file.
	 * 
	 * @param text
	 *            Text.
	 */
	private static void append(String text)
	{
		textArea.append(text);
		flush(text);
		
		// make sure the frame visibility state is correct.
		if (frame.isVisible() != visible)
		{
			frame.setVisible(visible);
		}
	}
	
	/**
	 * Create the time stamp from the system's date and time.
	 * 
	 * @return the time stamp
	 */
	public static String getTimeStamp()
	{
		return DateFormat.getDateTimeInstance().format(new Date());
	}
	
	/**
	 * Create the file-name time stamp from the system's date and time.
	 * 
	 * @return the file stamp
	 */
	public static String getFileStamp()
	{
		return (new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")).format(new Date());
	}
	
	/**
	 * Create a new log file using the current date and time.
	 */
	private static void newLogFile()
	{
		try
		{
			Files.createDirectories(logsFolder);		// make sure the log folder exists
			
			logFile = Files.createFile(logsFolder.resolve(getFileStamp() + ".log"));
			stream = Files.newOutputStream(logFile, StandardOpenOption.APPEND);
			writer = new OutputStreamWriter(stream);
			
			post("Created a log file at " + logFile.toAbsolutePath());
		}
		catch (IOException e)
		{	// oops, don't bother with a log file for this session!
			e.printStackTrace();
			
			writer = null;
			stream = null;
			logFile = null;
			
			post("Failed to create log file!");
		}
	}
	
	/**
	 * Flush this text to log file.
	 * 
	 * @param text
	 *            Text.
	 */
	private static void flush(String text)
	{
		try
		{
			// if a file was created and open ...
			if (writer != null)
			{
				writer.write(text);
				writer.flush();		// write entry to physical file immediately; to avoid crash corruption.
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.err.println("ERROR: Failed to write to log file!");
		}
	}
}
