/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Logger/com.yagasoft.logger/Logger.java
 *
 *			Modified: 26-Jul-2014 (15:55:29)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.logger;


import static com.yagasoft.logger.Logger.SequenceOption.BLACK_LAST_STRING;
import static com.yagasoft.logger.Logger.SequenceOption.COLOUR_LAST_STRING;
import static com.yagasoft.logger.Logger.SequenceOption.REMOVE_SEPARATOR;

import java.awt.Color;
import java.awt.Font;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLEditorKit;

import com.yagasoft.logger.menu.panels.option.Options;


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
	public static final String	VERSION					= "5.03.135";
	
	/** set when the log is accessible and ready. */
	public static boolean		initialised				= false;
	
	/* holds last posted date. This is used for postDateIfChanged method */
	private static String		date					= "";
	
	// everything that has been logged so far in plain format.
//	private static String		history				= "";
	
	// everything that has been logged in styled format.
	private static List<String>	historyStylised			= new ArrayList<String>();
	
	// last directory browsed to save a file
	private static Path			lastDirectory			= Paths.get(System.getProperty("user.home"));
	
	/* where the logs will be stored relative to project path. */
	private static final Path	OPTIONS_FILE			= Paths.get(System.getProperty("user.dir") + "/var/options.dat");
	
	static Object				maxEntriesLock			= new Object();
	static Object				logAttributesLock		= new Object();
	static Object				logDocumentWriteLock	= new Object();
	
	private static enum EntryType
	{
		INFO,
		ERROR,
		EXCEPTION
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	// #region Style.
	//======================================================================================
	
	private static final String	font						= "Verdana";
	
	//--------------------------------------------------------------------------------------
	// #region Colours.
	
	// a bit lighter than the one from the Color class.
	private static final Color	BLUE						= new Color(35, 40, 210);
	
	private static final Color	ORANGE						= new Color(170, 90, 0);
	private static final Color	LIGHT_BLUE					= new Color(0, 150, 150);
	
	// a bit darker than the one from the Color class.
	private static final Color	GREEN						= new Color(0, 140, 0);
	
	private static final Color	VIOLET						= new Color(150, 0, 255);
	private static final Color	DARK_RED					= new Color(230, 0, 0);
	
	// a bit darker than the one from the Color class.
	private static final Color	MAGENTA						= new Color(220, 0, 160);
	
	// colours to cycle through when displaying info with words wrapped in '`'.
	private static Color[]		colours						= { BLUE, ORANGE, LIGHT_BLUE, GREEN, VIOLET, DARK_RED, MAGENTA };
	
	// #endregion Colours.
	//--------------------------------------------------------------------------------------
	
	/** Default number of colours. */
	public static int			defaultNumberOfColours		= colours.length;
	
	/** Default coloured strings separator for {@link #infoColoured(String...)}. */
	public static String		defaultColouringSeparator	= " ";
	
	/** Default black last string flag for {@link #infoColouredSeparator(String, String...)}. */
	public static boolean		defaultBlackLastString		= false;
	
	/* style passed to getStyle method. */
	private enum Style
	{
		PLAIN,
		BOLD,
		ITALIC,
		BOLDITALIC
	}
	
	/**
	 * Options for {@link Logger#infoColouredSequence(int, String, String, SequenceOption...)}
	 */
	public static enum SequenceOption
	{
		
		/** Remove separator after using it to split the string. */
		REMOVE_SEPARATOR,
		
		/** Black-coloured last string. */
		BLACK_LAST_STRING,
		
		/** Colour the last string. */
		COLOUR_LAST_STRING,
		
		/** No option! */
		NONE
	}
	
	//======================================================================================
	// #endregion Style.
	////////////////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////////////////
	// #region Initialisation.
	//======================================================================================
	
	/**
	 * Convenience method for {@link #initAndShowLogger(String, int, boolean)}, using defaults (' ', max, false).
	 */
	public static void initAndShowLogger()
	{
		initAndShowLogger(defaultColouringSeparator, defaultNumberOfColours, defaultBlackLastString);
	}
	
	/**
	 * Convenience method for {@link #initLogger(String, int, boolean)}.
	 *
	 * @param defaultSeparator
	 *            Default separator.
	 * @param defaultNumberOfColours
	 *            Default number of colours.
	 * @param defaultBlackLastString
	 *            Default black last string?
	 */
	public static void initAndShowLogger(String defaultSeparator, int defaultNumberOfColours
			, boolean defaultBlackLastString)
	{
		initLogger(defaultSeparator, defaultNumberOfColours, defaultBlackLastString);
		GUI.showLogger();
	}
	
	/**
	 * Convenience method for {@link #initLogger(String, int, boolean)}, using defaults (' ', max, false).
	 */
	public static void initLogger()
	{
		initLogger(defaultColouringSeparator, defaultNumberOfColours, defaultBlackLastString);
	}
	
	/**
	 * Initialises the logger by loading the options, initialising the log file, and the GUI.
	 *
	 * @param defaultSeparator
	 *            Default separator to use for {@link #infoColoured(String...)}
	 * @param defaultNumberOfColours
	 *            Default number of colours to use for {@link #info(String, int...)}
	 * @param defaultBlackLastString
	 *            Default black last string to use for {@link #infoColouredSeparator(int, boolean, String, String...)}
	 */
	public static void initLogger(String defaultSeparator, int defaultNumberOfColours
			, boolean defaultBlackLastString)
	{
		// set defaults
		Logger.defaultColouringSeparator = defaultSeparator;
		Logger.defaultNumberOfColours = defaultNumberOfColours;
		Logger.defaultBlackLastString = defaultBlackLastString;
		
		// initialise logger if it wasn't.
		if ( !initialised)
		{
			loadOptions();
			
			File.initFile();
			GUI.initLogger();
			
			initialised = true;
			
			// post something and create a log file for this session.
			info("!!! `NEW LOG` !!!");
		}
	}
	
	//======================================================================================
	// #endregion Initialisation.
	////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Show logger window.
	 */
	public static synchronized void showLogger()
	{
		GUI.showLogger();
	}
	
	/**
	 * Hide logger window.
	 */
	public static synchronized void hideLogger()
	{
		GUI.hideLogger();
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Public posting interface.
	// ======================================================================================
	
	//--------------------------------------------------------------------------------------
	// #region Info posting.
	
	/**
	 * Informing log entry. You can use '`' character as to wrap words to be coloured. Colouring will cycle between 7 colours.
	 *
	 * @param entry
	 *            Entry.
	 * @param coloursToUse
	 *            Number of colours to use, excluding black (current max is 7), optional.
	 *            Anything outside 0..max results in max.
	 */
	public static synchronized void info(String entry, int... coloursToUse)
	{
		// write the time stamp, then the entry next to it.
		
		// time-stamp
		postTimeStamp();
		
		// line label
		AttributeSet style = getStyle(0, Style.ITALIC, GREEN);
		GUI.append("Info:   ", style);
		
		// append the entries on new lines using number of colours passed.
		postEntry(entry, coloursToUse);
	}
	
	/**
	 * Informing log entries. This will be posted one after the other in the same time-stamp.
	 * You can use '`' character as to wrap words to be coloured. Colouring will cycle between colours.
	 *
	 * @param coloursToUse
	 *            Number of colours to use, excluding black (current max is 7). Pass '-1' for max colours.
	 * @param entries
	 *            List of entries to post.
	 */
	public static synchronized void info(int coloursToUse, String... entries)
	{
		// write the time stamp
		postTimeStamp();
		
		// entry label.
		AttributeSet style = getStyle(0, Style.ITALIC, new Color(0, 150, 0));
		GUI.append("Info ...\n", style);
		
		// append the entry using number of colours passed.
		for (String entry : entries)
		{
			postEntry(entry, coloursToUse);
		}
	}
	
	// this method is the common process of posting entries for the two methods above.
	private static synchronized void postEntry(String entry, int... coloursToUse)
	{
		// split the entry into sections based on the delimiter '`'
		String[] entries = entry.split("`");
		
		// calculate number of colours to use. If passed, then use, else if -1 or not passed, then use default.
		int numberOfColours = Arrays.stream(coloursToUse).findFirst().orElse(defaultNumberOfColours);
		numberOfColours = (numberOfColours == -1) ? defaultNumberOfColours : numberOfColours;
		
		AttributeSet style = getStyle(Style.PLAIN);
		
		// iterate over entry sections
		for (int i = 0; i < entries.length; i++)
		{
			// reset style
			style = getStyle(Style.PLAIN);
			
			// odd entries are the ones needing colour
			if (((i % 2) == 1) && (numberOfColours > 0))
			{
				// post escaped entry using a different colour.
				style = getStyle(Style.PLAIN, colours[(i / 2) % numberOfColours]);
			}
			
			// add to log
			GUI.append(entries[i], style);
		}
		
		// add a new line
		GUI.append("\n", style);
	}
	
	/**
	 * Post strings, coloured using the max number of colours, and separated (added to entry) by the default set separator.
	 * It doesn't colour the last string as black.
	 *
	 * @param strings
	 *            Strings.
	 */
	public static void infoColoured(String... strings)
	{
		infoColouredSeparator(defaultColouringSeparator, strings);
	}
	
	/**
	 * Post strings, coloured using the number of colours passed, and separated (added to entry) by the default set separator.
	 *
	 * @param coloursToUse
	 *            Colours to use, -1 for max.
	 * @param blackLastString
	 *            Black last string?
	 * @param strings
	 *            Strings.
	 */
	public static void infoColoured(int coloursToUse, boolean blackLastString, String... strings)
	{
		infoColouredSeparator(coloursToUse, blackLastString, defaultColouringSeparator, strings);
	}
	
	/**
	 * Post strings, coloured using the max number of colours, and separated (added to entry) by the passed separator.
	 * It doesn't colour the last string as black.
	 *
	 * @param separator
	 *            Separator.
	 * @param strings
	 *            Strings.
	 */
	public static void infoColouredSeparator(String separator, String... strings)
	{
		infoColouredSeparator(defaultNumberOfColours, defaultBlackLastString, separator, strings);
	}
	
	/**
	 * Post strings, coloured using the number of colours passed, and separated (added to entry) by the passed separator.
	 *
	 * @param coloursToUse
	 *            Colours to use, -1 for max.
	 * @param blackLastString
	 *            Black last string?
	 * @param separator
	 *            Separator.
	 * @param strings
	 *            Strings.
	 */
	public static void infoColouredSeparator(int coloursToUse, boolean blackLastString
			, String separator, String... strings)
	{
		if (strings.length == 0)
		{
			return;
		}
		
		// form the entry
		// add the first string using the colouring symbol
		String entry = "`" + strings[0] + "`" + ((strings.length > 1) ? separator : "");
		
		// add the rest if there are any, except last string
		for (int i = 1; (i < (strings.length - 1)) && (strings.length > 2); i++)
		{
			entry += "`" + strings[i] + "`" + separator;
		}
		
		// end with the last string, and if 'black' is specified, then don't wrap it in '`'.
		if (strings.length > 1)
		{
			entry += (blackLastString ? "" : "`") + strings[strings.length - 1] + (blackLastString ? "" : "`");
		}
		
		info(entry, coloursToUse);
	}
	
	/**
	 * Post this string using {@link #infoColouredSequence(int, String, String, SequenceOption...)} after splitting it using
	 * the separator passed. It forwards the {@link #defaultNumberOfColours}.
	 *
	 * @param separator
	 *            Separator.
	 * @param string
	 *            String.
	 * @param options
	 *            Options list accepts 'removal of separator' (default false), 'colour last string',
	 *            and 'black last string' flags.
	 *            The defaults {@link #defaultBlackLastString} for latter two if neither is passed.
	 */
	public static void infoColouredSequence(String separator, String string, SequenceOption... options)
	{
		infoColouredSequence(defaultNumberOfColours, separator, string, options);
	}
	
	/**
	 * Post this string using {@link #infoColouredSeparator(int, boolean, String, String...)} after splitting it using the
	 * separator passed.
	 *
	 * @param coloursToUse
	 *            Colours to use, -1 for max.
	 * @param separator
	 *            Separator.
	 * @param string
	 *            String.
	 * @param optionsList
	 *            Options list accepts 'removal of separator' (default false), 'colour last string',
	 *            and 'black last string' flags.
	 *            The defaults {@link #defaultBlackLastString} for latter two if neither is passed.
	 */
	public static void infoColouredSequence(int coloursToUse, String separator, String string
			, SequenceOption... optionsList)
	{
		List<SequenceOption> options = Arrays.asList(optionsList);
		infoColouredSeparator(coloursToUse
				, options.contains(BLACK_LAST_STRING)
						? true : (options.contains(COLOUR_LAST_STRING) ? false : defaultBlackLastString)
				, options.contains(REMOVE_SEPARATOR) ? "" : separator, string.split(separator));
	}
	
	// #endregion Info posting.
	//--------------------------------------------------------------------------------------
	
	/**
	 * Error log entry. You can use '`' character as to wrap words to be coloured black.
	 *
	 * @param entry
	 *            Entry.
	 */
	public static synchronized void error(String entry)
	{
		// write the time stamp
		postTimeStamp();
		
		// append line label
		AttributeSet style = getStyle(0, Style.BOLDITALIC, Color.RED);
		GUI.append("!! ERROR >>   ", style);
		
		// append the error
		postError(entry);
		
		// add an extra label.
		style = getStyle(Style.BOLDITALIC, Color.RED);
		GUI.append("   << ERROR !!\n", style);
		
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
		// write the time stamp
		postTimeStamp();
		
		// append line label
		AttributeSet style = getStyle(0, Style.BOLDITALIC, Color.RED);
		GUI.append("!! ERRORS !!\n", style);
		
		// append the errors on new lines
		for (String entry : entries)
		{
			postError(entry);
		}
	}
	
	// this method is the common process of posting entries for the two methods above.
	private static synchronized void postError(String entry)
	{
		// split the entry into sections based on the delimiter '`'
		String[] entries = entry.split("`");
		
		AttributeSet style = getStyle(Style.PLAIN, Color.RED);
		
		// odd entries are the ones needing colour
		for (int i = 0; i < entries.length; i++)
		{
			// reset style
			style = getStyle(Style.PLAIN, Color.RED);
			
			if ((i % 2) == 1)
			{
				// post escaped entry using a different colour.
				style = getStyle(Style.PLAIN);
			}
			
			// add to log
			GUI.append(entries[i], style);
		}
		
		// add a new line
		GUI.append("\n", style);
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
		
		AttributeSet style = getStyle(0, Style.BOLDITALIC, Color.RED);
		GUI.append("!! EXCEPTION !!\n", style);
		
		// define how to handle the character in the stack trace.
		PrintWriter exceptionWriter = new PrintWriter(new Writer()
		{
			
			// store lines in the stack trace to print later.
			List<String>	lines	= new ArrayList<String>();
			
			@Override
			public void write(char[] cbuf, int off, int len) throws IOException
			{
				lines.add(new String(cbuf, off, len));
			}
			
			@Override
			public void flush() throws IOException
			{
				AttributeSet styleTemp = getStyle(0, Style.PLAIN, Color.RED);
				
				for (String line : lines)
				{
					GUI.append(line, styleTemp);		// send to the logger.
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
		
		GUI.append("\r", style);		// send to the logger.
	}
	
	// ======================================================================================
	// #endregion Public posting interface.
	// //////////////////////////////////////////////////////////////////////////////////////
	
	// add to stylised history.
	static void addToHistory(String entry, AttributeSet style)
	{
		synchronized (historyStylised)
		{
//			history += entry;
			historyStylised.add(getHTML(entry, style));
		}
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Text methods.
	// ======================================================================================
	
	/* post time stamp to log. */
	private static synchronized void postTimeStamp()
	{
//		postDateIfChanged();
		
		// post date in light colour because it's repeated too much, so it becomes distracting.
		AttributeSet style = getStyle(0, Style.PLAIN, new Color(180, 180, 180));
		GUI.append(getDate() + " ", style);
		
		// post time in black.
		style = getStyle(Style.PLAIN);
		GUI.append(getTime() + ": ", style);
	}
	
	/* posts the date if it has changed from the saved one -- saves space in the log. */
	private static synchronized void postDateIfChanged()
	{
		if ( !getDate().equals(date))
		{
			// save current date if it has changed.
			date = getDate();
			
			// post the new date in a bright colour.
			AttributeSet style = getStyle(15, Style.BOLD, new Color(255, 150, 0));
			GUI.append(getDate() + " ", style);
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
	
	// convenience method
	private static AttributeSet getStyle(Style style, Color... colour)
	{
		return getStyle(GUI.getFontSize(), style, colour);
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
		if (GUI.textPane == null)
		{
			return null;
		}
		
		// Start with the current input attributes for the JTextPane. This
		// should ensure that we do not wipe out any existing attributes
		// (such as alignment or other paragraph attributes) currently
		// set on the text area.
		synchronized (logAttributesLock)
		{
			MutableAttributeSet attributes = GUI.textPane.getInputAttributes();
			
			Font font = new Font(Logger.font
					, ((style == Style.BOLD) ? Font.BOLD : 0)
							+ ((style == Style.ITALIC) ? Font.ITALIC : 0)
							+ ((style == Style.BOLDITALIC) ? Font.ITALIC + Font.BOLD : 0)
					, (size <= 0) ? GUI.getFontSize() : size);
			
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
			
			return attributes.copyAttributes();
		}
	}
	
	// convert text and style to HTML.
	private static String getHTML(String text, AttributeSet style)
	{
		// use to convert text to HTML
		JTextPane conversionPane = new JTextPane();
		conversionPane.setEditorKit(new HTMLEditorKit());
		
		try
		{
			// replace characters that aren't parsed by the HTML panel!!!
			// add the plain text to an HTML editor to convert the text to a stylised HTML.
			conversionPane.getDocument().insertString(conversionPane.getCaretPosition()
					, text.replace("\n", "`new_line`").replace("\t", "`tab`")
							.replace(" ", "`space`")
					, style);
			
			synchronized (logAttributesLock)
			{
				MutableAttributeSet attributes = conversionPane.getInputAttributes();
				attributes.removeAttribute(StyleConstants.FontFamily);
				attributes.removeAttribute(StyleConstants.FontSize);
				attributes.removeAttribute(StyleConstants.Italic);
				attributes.removeAttribute(StyleConstants.Bold);
				attributes.removeAttribute(StyleConstants.Foreground);
				attributes.addAttribute(StyleConstants.FontSize, 8);
				
				conversionPane.getStyledDocument().setCharacterAttributes(0, conversionPane.getDocument().getLength()
						, attributes.copyAttributes(), false);
			}
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
			return "";
		}
		
		// remove unnecessary tags, and return tags that were replaced above.
		// get the text back from the editor as HTML.
		return conversionPane.getText().replace("<html>", "").replace("</html>", "").replace("<head>", "")
				.replace("</head>", "").replace("<body>", "").replace("</body>", "")
				.replace("`new_line`", "<br />").replace("`tab`", "&#9;").replace("`space`", "&nbsp;")
				.replace("<p style", "<span style").replace("</p>", "</span>");
	}
	
	// ======================================================================================
	// #endregion Text methods.
	// //////////////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////////////////
	// #region Saving.
	//======================================================================================
	
	/**
	 * Save as html.
	 */
	public static void saveAsHTML()
	{
		if ( !initialised)
		{
			return;
		}
		
		// !comments are in saveAsTxt!
		
		Path chosenFolder = chooseFolder();
		
		if (chosenFolder == null)
		{
			return;
		}
		
		Path file = chosenFolder.resolve("log_file_-_" + File.getFileStamp() + ".html");
		
		new Thread(() ->
		{
			try (Writer writer = new OutputStreamWriter(Files.newOutputStream(file)))
			{
				try
				{
					// this operation takes a LONG time if the log is big enough.
				GUI.showMessage("Saving as HTML to file '" + file + "'. You will be notified when it's done ...");
				
				List<String> stream;
				
				synchronized (historyStylised)
				{
					stream = historyStylised.stream().collect(Collectors.toList());
				}
				
				writer.write("<html><body>"
						+ stream.stream().reduce((e1, e2) -> e1 + e2).orElse("")
						+ "</html></body>");
				
				GUI.showMessage("Finished saving as HTML to file '" + file + "'.");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		catch (IOException e)
		{
			Logger.except(e);
			e.printStackTrace();
		}
	}	).start();
	}
	
	/**
	 * Save as txt.
	 */
	public static void saveAsTxt()
	{
		if ( !initialised)
		{
			return;
		}
		
		// ask the user for folder to save to.
		Path chosenFolder = chooseFolder();
		
		// if nothing is chosen, do nothing.
		if (chosenFolder == null)
		{
			return;
		}
		
		// get the full path of the file, using the parent, and a time stamp.
		Path file = chosenFolder.resolve("log_file_-_" + File.getFileStamp() + ".txt");
		
		// copy the already existing log file
		new Thread(() ->
		{
			try
			{
				Files.copy(File.logFile, file);
			}
			catch (Exception e)
			{
				Logger.except(e);
				e.printStackTrace();
			}
		}).start();
	}
	
	/**
	 * Choose folder.
	 *
	 * @return Local folder
	 */
	private static Path chooseFolder()
	{
		if (Files.notExists(lastDirectory))
		{
			lastDirectory = Paths.get(System.getProperty("user.home"));
		}
		
		// only choose directories.
		JFileChooser chooser = new JFileChooser(lastDirectory.toString());
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		// show dialogue
		int result = chooser.showOpenDialog(GUI.frame);
		java.io.File selectedFolder = chooser.getSelectedFile();
		
		// if a folder was not chosen ...
		if ((result != JFileChooser.APPROVE_OPTION) || (selectedFolder == null))
		{
			return null;
		}
		
		lastDirectory = Paths.get(selectedFolder.toURI());
		
		return lastDirectory;
	}
	
	/**
	 * Load options.
	 */
	public static synchronized void loadOptions()
	{
		if (Files.notExists(OPTIONS_FILE))
		{
			return;
		}
		
		FileInputStream fileStream;		// incoming link to file.
		ObjectInputStream objectStream;		// link to objects read from file.
		Options options;
		
		try
		{
			fileStream = new FileInputStream(OPTIONS_FILE.toString());
			objectStream = new ObjectInputStream(fileStream);
			options = (Options) objectStream.readObject();
			objectStream.close();
			
			GUI.setMaxEntries(options.numberOfEntries);
			GUI.setFontSize(options.fontSize);
			GUI.setWrapVarOnly(options.wrap);
			GUI.setActionOnCloseVarOnly(options.actionOnClose);
			lastDirectory = Paths.get(options.lastDirectory);
		}
		catch (ClassNotFoundException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Save options.
	 */
	public static synchronized void saveOptions()
	{
		FileOutputStream fileStream;
		ObjectOutputStream objectStream;
		Options options = new Options(GUI.getMaxEntries(), GUI.getFontSize(), GUI.isWrap());
		options.actionOnClose = GUI.getActionOnClose();
		options.lastDirectory = lastDirectory.toString();
		
		try
		{
			fileStream = new FileOutputStream(OPTIONS_FILE.toString());
			objectStream = new ObjectOutputStream(fileStream);
			objectStream.writeObject(options);
			objectStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	//======================================================================================
	// #endregion Saving.
	////////////////////////////////////////////////////////////////////////////////////////
	
	// static class!
	private Logger()
	{}
	
}
