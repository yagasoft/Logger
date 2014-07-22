/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Logger/com.yagasoft.logger/Logger.java
 * 
 *			Modified: 23-Jul-2014 (01:29:06)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
	
	/** Constant: VERSION. */
	public static final String			VERSION		= "3.02.005";
	
	private static boolean				visible		= false;
	
	/* holds last posted date. This is used for postDateIfChanged method */
	private static String				date		= "";
	
	/* font size for log window. */
	private static final int			FONT_SIZE	= 13;
	
	////////////////////////////////////////////////////////////////////////////////////////
	// #region Colours.
	//======================================================================================
	
	// a bit lighter than the one from the Color class.
	private static final Color			BLUE		= new Color(40, 50, 230);
	
	private static final Color			ORANGE		= new Color(205, 130, 0);
	private static final Color			LIGHT_BLUE	= new Color(0, 180, 180);
	
	// a bit darker than the one from the Color class.
	private static final Color			GREEN		= new Color(0, 170, 0);
	
	private static final Color			VIOLET		= new Color(150, 0, 255);
	private static final Color			DARK_RED	= new Color(190, 0, 0);
	
	// a bit darker than the one from the Color class.
	private static final Color			MAGENTA		= new Color(230, 0, 170);
	
	// colours to cycle through when displaying info with words wrapped in '`'.
	private static Color[]				colours		= { BLUE, ORANGE, LIGHT_BLUE, GREEN, VIOLET, DARK_RED, MAGENTA };
	
	//======================================================================================
	// #endregion Colours.
	////////////////////////////////////////////////////////////////////////////////////////
	
	/* where the logs will be stored relative to project path. */
	private static final Path			LOGS_FOLDER	= Paths.get(System.getProperty("user.dir") + "/var/logs/");
	
	/* current log file path. */
	private static Path					logFile;
	
	/* stream to log file. */
	private static OutputStream			stream;
	
	/* writer to log file. */
	private static OutputStreamWriter	writer;
	
	private static JFrame				frame;
	private static JPanel				contentPane;
	private static JTextPane			textPane;
	
	/* style passed to getStyle method. */
	private enum Style
	{
		PLAIN,
		BOLD,
		ITALIC,
		BOLDITALIC
	}
	
	// init the logger as soon as anything here is called.
	static
	{
		initLogger();
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Initialisation.
	// ======================================================================================
	
	/* Inits the logger. */
	private static void initLogger()
	{
		// post something and create a log file for this session.
		newLogFile();
		info("!!! NEW LOG !!!");
	}
	
	/* Inits the frame. */
	private static void initFrame()
	{
		frame = new JFrame("Logger window");
		frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		frame.setBounds(25, 25, 800, 512);
		frame.setVisible(visible);
	}
	
	/* Inits the panel. */
	private static void initPanel()
	{
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		
		frame.setContentPane(contentPane);
		frame.revalidate();
	}
	
	/* Inits the log text area. */
	private static void initLog()
	{
		// no-wrapping, hint credit: Rob Camick (http://tips4java.wordpress.com/2009/01/25/no-wrap-text-pane/)
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
	public static synchronized void showLogger()
	{
		if (textPane == null)
		{
			initFrame();
			initPanel();
			initLog();
		}
		
		visible = true;
		frame.setVisible(visible);
	}
	
	/**
	 * Hide logger window.
	 */
	public static synchronized void hideLogger()
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
	 * Informing log entry. You can use '`' character as to wrap words to be coloured. Colouring will cycle between 7 colours.
	 *
	 * @param entry
	 *            Entry.
	 */
	public static synchronized void info(String entry)
	{
		// write the time stamp, then the entry next to it.
		
		// time-stamp
		postTimeStamp();
		
		// line label
		AttributeSet style = getStyle( -1, Style.ITALIC, GREEN);
		append("Info:   ", style);
		
		// split the entry into sections based on the delimiter '`'
		String[] entries = entry.split("`");
		
		// odd entries are the ones needing colour
		for (int i = 0; i < entries.length; i++)
		{
			// reset style
			style = getStyle( -1, Style.PLAIN);
			
			if ((i % 2) == 1)
			{
				// post escaped entry using a different colour.
				style = getStyle( -1, Style.PLAIN, colours[(i - 1) % colours.length]);
			}
			
			// add to log
			append(entries[i], style);
		}
		
		// add a new line
		style = getStyle( -1, Style.PLAIN);
		append("\n", style);
	}
	
	/**
	 * Informing log entries. This will be posted one after the other in the same time-stamp.
	 * You can use '`' character as to wrap words to be coloured. Colouring will cycle between 7 colours.
	 *
	 * @param entries
	 *            Entries.
	 */
	public static synchronized void info(String... entries)
	{
		// write the time stamp, then the entry next to it.
		
		postTimeStamp();
		
		AttributeSet style = getStyle( -1, Style.ITALIC, new Color(0, 150, 0));
		append("Info:\n", style);
		
		// post entries
		style = getStyle( -1, Style.PLAIN);
		
		for (String entry : entries)
		{
			// split the entry into sections based on the delimiter '`'
			String[] subEntries = entry.split("`");
			
			// odd entries are the ones needing colour
			for (int i = 0; i < subEntries.length; i++)
			{
				// reset style
				style = getStyle( -1, Style.PLAIN);
				
				if ((i % 2) == 1)
				{
					// post escaped entry using a different colour.
					style = getStyle( -1, Style.PLAIN, colours[(i - 1) % colours.length]);
				}
				
				// add to log
				append(subEntries[i], style);
			}
			
			// add a new line
			style = getStyle( -1, Style.PLAIN);
			append("\n", style);
		}
	}
	
	/**
	 * Error log entry. You can use '`' character as to wrap words to be coloured black.
	 *
	 * @param entry
	 *            Entry.
	 */
	public static synchronized void error(String entry)
	{
		// write the time stamp, then the error next to it.
		
		postTimeStamp();
		
		AttributeSet style = getStyle( -1, Style.BOLDITALIC, Color.RED);
		append("!! ERROR >>   ", style);
		
		// split the entry into sections based on the delimiter '`'
		String[] entries = entry.split("`");
		
		// odd entries are the ones needing colour
		for (int i = 0; i < entries.length; i++)
		{
			// reset style
			style = getStyle( -1, Style.PLAIN, Color.RED);
			
			if ((i % 2) == 1)
			{
				// post escaped entry using a different colour.
				style = getStyle( -1, Style.PLAIN);
			}
			
			// add to log
			append(entries[i], style);
		}
		
		// add an extra label.
		style = getStyle( -1, Style.BOLDITALIC, Color.RED);
		append("   << ERROR !!\n", style);
	}
	
	/**
	 * Error log entry. This will be posted one after the other in the same time-stamp.
	 * You can use '`' character as to wrap words to be coloured black.
	 *
	 * @param entries
	 *            Entries.
	 */
	public static synchronized void errors(String... entries)
	{
		// write the time stamp, then the error next to it.
		
		postTimeStamp();
		
		AttributeSet style = getStyle( -1, Style.BOLDITALIC, Color.RED);
		append("!! ERRORS !!\n", style);
		
		style = getStyle( -1, Style.PLAIN, Color.RED);
		
		for (String entry : entries)
		{
			// split the entry into sections based on the delimiter '`'
			String[] subEntries = entry.split("`");
			
			// odd entries are the ones needing colour
			for (int i = 0; i < subEntries.length; i++)
			{
				// reset style
				style = getStyle( -1, Style.PLAIN, Color.RED);
				
				if ((i % 2) == 1)
				{
					// post escaped entry using a different colour.
					style = getStyle( -1, Style.PLAIN);
				}
				
				// add to log
				append(subEntries[i], style);
			}
			
			// add a new line
			style = getStyle( -1, Style.PLAIN);
			append("\n", style);
		}
	}
	
	/**
	 * Exception log entry.
	 *
	 * @param exception
	 *            the Exception.
	 */
	public static synchronized void except(Throwable exception)
	{
		// write the time stamp, then the exception below it.
		
		postTimeStamp();
		
		AttributeSet style = getStyle( -1, Style.BOLDITALIC, Color.RED);
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
				AttributeSet styleTemp = getStyle( -1, Style.PLAIN, Color.RED);
				
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
	
	/* append text to the log as is using the style passed, then write it to log file. */
	private static void append(String text, AttributeSet style)
	{
		try
		{
			// if nothing is displayed, don't append to anything.
			if (textPane != null)
			{
				textPane.getDocument().insertString(textPane.getCaretPosition(), text, style);
			}
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		
		flush(text);		// save to disk log file
		
		// make sure the frame visibility state is correct.
		// calling 'setVisible' directly every time causes focus stealing annoyance.
		if ((frame != null) && (frame.isVisible() != visible))
		{
			frame.setVisible(visible);
		}
	}
	
	/* post time stamp to log. */
	private static void postTimeStamp()
	{
//		postDateIfChanged();
		
		// post date in light colour because it's repeated too much, so it becomes distracting.
		AttributeSet style = getStyle( -1, Style.PLAIN, new Color(180, 180, 180));
		append(getDate() + " ", style);
		
		// post time in black.
		style = getStyle( -1, Style.PLAIN);
		append(getTime() + ": ", style);
	}
	
	/* posts the date if it has changed from the saved one -- saves space in the log. */
	private static void postDateIfChanged()
	{
		if ( !getDate().equals(date))
		{
			// save current date if it has changed.
			date = getDate();
			
			// post the new date in a bright colour.
			AttributeSet style = getStyle(15, Style.BOLD, new Color(255, 150, 0));
			append(getDate() + " ", style);
		}
	}
	
	/* create a current date string. */
	private static String getDate()
	{
		return new SimpleDateFormat("dd/MMM/yy").format(new Date()) /* DateFormat.getDateInstance().format(new Date()) */;
	}
	
	/* Create a current time string. */
	private static String getTime()
	{
		return new SimpleDateFormat("hh:mm:ss aa").format(new Date());
	}
	
	/*
	 * Forms and returns the style as an {@link AttributeSet} to be used with {@link JTextPane}.
	 * Pass '0' for size to use the default one. Colour is optional, black is used by default.
	 * 
	 * Credit: Philip Isenhour (http://javatechniques.com/blog/setting-jtextpane-font-and-color/)
	 */
	private static AttributeSet getStyle(int size, Style style, Color... colour)
	{
		// if nothing is displayed, then no style is needed.
		if (textPane == null)
		{
			return null;
		}
		
		// Start with the current input attributes for the JTextPane. This
		// should ensure that we do not wipe out any existing attributes
		// (such as alignment or other paragraph attributes) currently
		// set on the text area.
		MutableAttributeSet attributes = textPane.getInputAttributes();
		
		Font font = new Font("Verdana"
				, ((style == Style.BOLD) ? Font.BOLD : 0)
						+ ((style == Style.ITALIC) ? Font.ITALIC : 0)
						+ ((style == Style.BOLDITALIC) ? Font.ITALIC + Font.BOLD : 0)
				, (size <= 0) ? FONT_SIZE : size);
		
		// Set the font family, size, and style, based on properties of
		// the Font object. Note that JTextPane supports a number of
		// character attributes beyond those supported by the Font class.
		// For example, underline, strike-through, super- and sub-script.
		StyleConstants.setFontFamily(attributes, font.getFamily());
		StyleConstants.setFontSize(attributes, font.getSize());
		StyleConstants.setItalic(attributes, (font.getStyle() & Font.ITALIC) != 0);
		StyleConstants.setBold(attributes, (font.getStyle() & Font.BOLD) != 0);
		
		// Set the font colour, or black by default.
		StyleConstants.setForeground(attributes, Arrays.stream(colour).findFirst().orElse(Color.BLACK));
		
		return attributes;
	}
	
	// ======================================================================================
	// #endregion Text methods.
	// //////////////////////////////////////////////////////////////////////////////////////
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// #region File methods.
	// ======================================================================================
	
	/* create a new log file using the current date and time for its name. */
	private static void newLogFile()
	{
		try
		{
			Files.createDirectories(LOGS_FOLDER);		// make sure the log folder exists
			
			logFile = Files.createFile(LOGS_FOLDER.resolve(getFileStamp() + ".log"));
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
	
	/* create the file-name time stamp from the system's date and time. */
	private static String getFileStamp()
	{
		return (new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")).format(new Date());
	}
	
	/* flush this text to log file. */
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
//			error("failed to write to log file!");
		}
	}
	
	/**
	 * Gets the current log file.
	 *
	 * @return the current log file
	 */
	public static Path getCurrentLogFile()
	{
		return logFile;
	}
	
	// ======================================================================================
	// #endregion File methods.
	// //////////////////////////////////////////////////////////////////////////////////////
	
	// tester method
	@SuppressWarnings("all")
	public static void main(String[] args)
	{
		showLogger();
		
//		info("TEST1!!!");
//		error("TEST2!!!");
//
//		hideLogger();
//
//		error("TEST3!!!");
//		info("TEST4!!!");
//
//		showLogger();
		
		info("test `test` `test` `test` `test` `test` `test` `test` `test` `test` `test` `test` `test`");
		error("test `test` `test` `test` `test` test `test` `test` `test` `test` `test` test `test`");
		
		except(new Exception("Test!"));
		
		info(new String[] { "tte `te` te `te` st", "te `st` tt", "final test `tt` ette `te` t" });
		errors(new String[] { "te `st` tt", "t `te` te `tete` st", "fi `nal te` st" });
		
		// must stop process manually.
	}
	
}
