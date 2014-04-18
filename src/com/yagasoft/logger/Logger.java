/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Logger/com.yagasoft.logger/Logger.java
 *
 *			Modified: 18-Apr-2014 (16:16:50)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.logger;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;


/**
 * This class contains static methods to log entries, display them in a stylised form, and save them to disk in a log file.<br />
 * <br />
 * Everything is initialised automatically when calling the first post or error or exception. (everything is static)<br/>
 * To show the window call {@link #showLogger()}, which does initialise the log as above if it was the first thing called.<br />
 * <br/>
 * <strong>Defaults:</strong> read 'README.md'.
 */
public class Logger
{
	
	// please, don't change the order of the fields.
	
	/** Logger window is visible. */
	private static boolean				visible		= false;
	
	/** Constant: LogsFolder. */
	private static final Path			logsFolder	= Paths.get(System.getProperty("user.dir") + "/var/logs/");
	
	/** Log file path. */
	private static Path					logFile;
	
	/** Stream to log file. */
	private static OutputStream			stream;
	
	/** Writer to log file. */
	private static OutputStreamWriter	writer;
	
	/** Logger window frame. */
	private static JFrame				frame		= initFrame();
	
	/** Logger content pane. */
	private static JPanel				contentPane	= initPanel();
	
	/** Logger text pane. */
	private static JTextPane			textPane	= initLog();
	
	/**
	 * Styles passed to {@link Logger#getStyle(int, Styles, Color)}.
	 */
	private enum Styles
	{
		PLAIN,
		BOLD,
		ITALIC,
		BOLDITALIC
	}
	
	/**
	 * Tester method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args)
	{
		showLogger();
		
		info("TEST1!!!");
		error("TEST2!!!");
		
		hideLogger();
		
		error("TEST3!!!");
		info("TEST4!!!");
		
		showLogger();
		
		except(new Exception("Test!"));
		
		// must stop process manually.
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Initialisation.
	// ======================================================================================
	
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
	private static JTextPane initLog()
	{
		textPane = new JTextPane()
		{
			
			private static final long	serialVersionUID	= 9120063598362532890L;
			
			@Override
			public boolean getScrollableTracksViewportWidth()
			{
				return getUI().getPreferredSize(this).width <= getParent().getSize().width;
			}
		};
		
		textPane.setEditable(false);
		
		JScrollPane scroller = new JScrollPane(textPane);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		DefaultCaret caret = (DefaultCaret) textPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		contentPane.add(scroller);
		frame.revalidate();
		
		// post something and create a log file for this session.
		info("!!! NEW LOG !!!");
		newLogFile();
		
		return textPane;
	}
	
	// ======================================================================================
	// #endregion Initialisation.
	// //////////////////////////////////////////////////////////////////////////////////////
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Window control.
	// ======================================================================================
	
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
	
	// ======================================================================================
	// #endregion Window control.
	// //////////////////////////////////////////////////////////////////////////////////////
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Public posting interface.
	// ======================================================================================
	
	/**
	 * Informing log entry.
	 * 
	 * @param entry
	 *            Entry.
	 */
	public static void info(String entry)
	{
		// write the time stamp, then the entry next to it.
		
		AttributeSet style = getStyle(13, Styles.PLAIN, null);
		append(getTimeStamp() + ": ", style);
		
		style = getStyle( -1, Styles.ITALIC, new Color(0, 150, 0));
		append("Info:   ", style);
		
		style = getStyle( -1, Styles.PLAIN, Color.BLUE);
		append(entry + "\n", style);
	}
	
	/**
	 * Error log entry.
	 * 
	 * @param entry
	 *            Entry.
	 */
	public static void error(String entry)
	{
		// write the time stamp, then the error next to it.
		
		AttributeSet style = getStyle( -1, Styles.PLAIN, null);
		append(getTimeStamp() + ": ", style);
		
		style = getStyle( -1, Styles.BOLDITALIC, Color.RED);
		append("!! ERROR >>   ", style);
		
		style = getStyle( -1, Styles.PLAIN, Color.BLUE);
		append(entry, style);
		
		style = getStyle( -1, Styles.BOLDITALIC, Color.RED);
		append("   << ERROR !!\n", style);
	}
	
	/**
	 * Exception log entry.
	 * 
	 * @param entry
	 *            Entry.
	 */
	public static void except(Exception exception)
	{
		// write the time stamp, then the exception below it.
		
		AttributeSet style = getStyle( -1, Styles.PLAIN, null);
		append(getTimeStamp() + ": ", style);
		
		style = getStyle( -1, Styles.BOLDITALIC, Color.RED);
		append("!! EXCEPTION !!\n", style);
		
		// define how to handle the character in the stack trace.
		PrintWriter exceptionWriter = new PrintWriter(new Writer()
		{
			
			// store lines in the stack trace to print later.
			ArrayList<String>	lines	= new ArrayList<String>();
			
			@Override
			public void write(char[] cbuf, int off, int len) throws IOException
			{
				lines.add(new String(cbuf, off, len));
			}
			
			@Override
			public void flush() throws IOException
			{
				AttributeSet styleTemp = getStyle( -1, Styles.PLAIN, Color.RED);
				
				for (String line : lines)
				{
					Logger.append(line, styleTemp);		// send to the logger.
				}
			}
			
			@Override
			public void close() throws IOException
			{}
		});
		
		// print the exception.
		exception.printStackTrace(exceptionWriter);
		exceptionWriter.flush();
		exceptionWriter.close();
		
		Logger.append("\n", null);		// send to the logger.
	}
	
	// ======================================================================================
	// #endregion Public posting interface.
	// //////////////////////////////////////////////////////////////////////////////////////
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Text methods.
	// ======================================================================================
	
	/**
	 * Append text to the log as is, then write it to log file.
	 * 
	 * @param text
	 *            Text.
	 */
	private static void append(String text, AttributeSet style)
	{
		try
		{
			textPane.getDocument().insertString(textPane.getCaretPosition(), text, style);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		flush(text);		// save to disk log file
		
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
	private static String getTimeStamp()
	{
		return DateFormat.getDateTimeInstance().format(new Date());
	}
	
	/**
	 * Forms and returns the style as an {@link AttributeSet} to be used with {@link JTextPane}.<br />
	 * <br />
	 * Credit: Philip Isenhour (<a href="http://javatechniques.com/blog/setting-jtextpane-font-and-color/">link</a>)
	 * 
	 * @param size
	 *            Size, -1 for default.
	 * @param styles
	 *            Styles from {@link Styles}.
	 * @param colour
	 *            Colour, null for default.
	 * @return the style
	 */
	private static AttributeSet getStyle(int size, Styles styles, Color colour)
	{
		// Start with the current input attributes for the JTextPane. This
		// should ensure that we do not wipe out any existing attributes
		// (such as alignment or other paragraph attributes) currently
		// set on the text area.
		MutableAttributeSet attributes = textPane.getInputAttributes();
		
		Font font = new Font("Verdana"
				, ((styles == Styles.BOLD) ? Font.BOLD : 0)
						+ ((styles == Styles.ITALIC) ? Font.ITALIC : 0)
						+ ((styles == Styles.BOLDITALIC) ? Font.ITALIC + Font.BOLD : 0)
				, (size == -1) ? 12 : size);
		
		// Set the font family, size, and style, based on properties of
		// the Font object. Note that JTextPane supports a number of
		// character attributes beyond those supported by the Font class.
		// For example, underline, strike-through, super- and sub-script.
		StyleConstants.setFontFamily(attributes, font.getFamily());
		StyleConstants.setFontSize(attributes, font.getSize());
		StyleConstants.setItalic(attributes, (font.getStyle() & Font.ITALIC) != 0);
		StyleConstants.setBold(attributes, (font.getStyle() & Font.BOLD) != 0);
		
		// Set the font colour
		StyleConstants.setForeground(attributes, (colour == null) ? Color.BLACK : colour);
		
		return attributes;
	}
	
	// ======================================================================================
	// #endregion Text methods.
	// //////////////////////////////////////////////////////////////////////////////////////
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// #region File methods.
	// ======================================================================================
	
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
			
			info("Created a log file at " + logFile.toAbsolutePath());
		}
		catch (IOException e)
		{	// oops, don't bother with a log file for this session!
			e.printStackTrace();
			
			writer = null;
			stream = null;
			logFile = null;
			
			error("failed to create log file!");
		}
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
			error("failed to write to log file!");
		}
	}
	
	// ======================================================================================
	// #endregion File methods.
	// //////////////////////////////////////////////////////////////////////////////////////
	
}
