/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Logger/com.yagasoft.logger/Logger.java
 *
 *			Modified: 28-Jul-2014 (14:51:14)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.logger;


import static com.yagasoft.logger.Log.SequenceOption.BLACK_LAST_STRING;
import static com.yagasoft.logger.Log.SequenceOption.COLOUR_LAST_STRING;
import static com.yagasoft.logger.Log.SequenceOption.REMOVE_SEPARATOR;
import static com.yagasoft.logger.PrintStreamCapturer.CaptureType.ERROR;
import static com.yagasoft.logger.PrintStreamCapturer.CaptureType.OUT;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFileChooser;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLEditorKit;

import com.yagasoft.logger.Log.SequenceOption;
import com.yagasoft.logger.menu.panels.option.Options;


/**
 * This class contains methods to log entries, display them in a stylised form, and save them to disk in a log file.<br />
 * <br />
 * Everything is initialised automatically when calling the first post or error or exception. (everything is static)<br/>
 * To show the window call {@link GUI#showLogger()}, which does initialise the log as above if it was the first thing called.<br />
 * <br/>
 * <strong>Defaults:</strong> read 'README.md'.
 */
public class Logger
{
	
	// please, don't change the order of the fields.
	
	/** set when the log is accessible and ready. */
	private boolean								initialised				= false;
	
	private LinkedBlockingQueue<String>			historyTextQueue		= new LinkedBlockingQueue<String>(100);
	private LinkedBlockingQueue<AttributeSet>	historyAttributeQueue	= new LinkedBlockingQueue<AttributeSet>(100);
	
	private JTextPane							conversionPane			= new JTextPane();
	
	private enum EntryType
	{
		INFO,
		ERROR,
		EXCEPTION
	}
	
	/* Default number of colours. */
	private int				defaultNumberOfColours;
	
	/* Default coloured strings separator for {@link #infoColoured(String...)}. */
	private String			defaultColouringSeparator;
	
	/* Default black last string flag for {@link #infoColouredSeparator(String, String...)}. */
	private boolean			defaultBlackLastString;
	
	////////////////////////////////////////////////////////////////////////////////////////
	// #region Style.
	//======================================================================================
	
	private final String	font		= "Verdana";
	
	//--------------------------------------------------------------------------------------
	// #region Colours.
	
	// a bit lighter than the one from the Color class.
	private final Color		BLUE		= new Color(35, 40, 210);
	
	private final Color		ORANGE		= new Color(150, 80, 0);
	private final Color		LIGHT_BLUE	= new Color(0, 120, 120);
	
	// a bit darker than the one from the Color class.
	private final Color		GREEN		= new Color(0, 140, 0);
	
	private final Color		VIOLET		= new Color(120, 0, 200);
	private final Color		RED			= new Color(230, 0, 0);
	
	// a bit darker than the one from the Color class.
	private final Color		MAGENTA		= new Color(220, 0, 160);
	
	private final Color		BLACK		= new Color(0, 0, 0);
	private final Color		GREY		= new Color(180, 180, 180);
	
	// colours to cycle through when displaying info with words wrapped in '`'.
	private Color[]			colours		= { BLUE, ORANGE, LIGHT_BLUE, VIOLET, RED, GREEN, MAGENTA };
	
	// #endregion Colours.
	//--------------------------------------------------------------------------------------
	
	/* style passed to getStyle method. */
	private enum Style
	{
		PLAIN,
		BOLD,
		//		ITALIC,
		BOLDITALIC
	}
	
	private AttributeSet				tempStyle;
	private MutableAttributeSet			htmlAttributes;
	
	/** Attribute pool. */
	public Map<String, AttributeSet>	attrPool	= new HashMap<String, AttributeSet>(
															Style.values().length * 16 * (colours.length + 2));
	/** Style pool for HTML log file CSS. */
	public Map<AttributeSet, String>	stylePool	= new HashMap<AttributeSet, String>(
															Style.values().length * 16 * (colours.length + 2));
	
	//======================================================================================
	// #endregion Style.
	////////////////////////////////////////////////////////////////////////////////////////
	
	private GUI							gui;
	private File						file;
	
	////////////////////////////////////////////////////////////////////////////////////////
	// #region Initialisation.
	//======================================================================================
	
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
	void initLogger(final String defaultSeparator, final int defaultNumberOfColours, final boolean defaultBlackLastString)
	{
		// set defaults
		defaultColouringSeparator = defaultSeparator;
		this.defaultNumberOfColours = defaultNumberOfColours;
		this.defaultBlackLastString = defaultBlackLastString;
		
		// initialise logger if it wasn't.
		if ( !initialised)
		{
			// make sure to flush all log text, and save options before exiting.
			Runtime.getRuntime().addShutdownHook(new Thread(() ->
			{
				try
				{
					File.getInstance().shutdown();
					Options.getInstance().saveOptions();
					Thread.sleep(5000);
					
					// compress and delete logs, and close all streams
					File.getInstance().finalise();
					
					while ( !File.getInstance().isFinished())
					{
						Thread.sleep(2000);
					}
				}
				catch (final Exception e)
				{
					e.printStackTrace();
				}
			}));
			
			Options.getInstance().loadOptions();
			gui = GUI.getInstance();
			file = File.getInstance();
			initStyles();
			
			// HTML history file thread.
			new Thread(() ->
			{
				String text;
				AttributeSet attributes;
				
				while (true)
				{
					try
					{
						text = historyTextQueue.take();
						attributes = historyAttributeQueue.take();
						file.writeToHTML(getHTML(text, attributes));
					}
					catch (final Exception e)
					{
						e.printStackTrace();
					}
				}
			}).start();
			
			// used to convert text to HTML
			conversionPane.setEditorKit(new HTMLEditorKit());
			htmlAttributes = conversionPane.getInputAttributes();
			
			initialised = true;
			
			// post something and create a log file for this session.
			info("!!! `NEW LOG` !!!");
		}
	}
	
	// saves all variants of attributes in a map to save memory and time
	// forms CSS for all those attributes and saves them in the head of the HTML file to save space
	private void initStyles()
	{
		String styleString;
		file.writeToHTML("<head><style>");
		
		for (final Style style : Style.values())
		{
			for (int i = 10; i < 26; i++)
			{
				// add black variants
				tempStyle = getStyle(i, style, BLACK);
				attrPool.putIfAbsent("" + i + style + BLACK, tempStyle);
				styleString = "s"
						+ tempStyle.getAttribute(StyleConstants.FontSize)
						+ tempStyle.getAttribute(StyleConstants.Bold)
						+ tempStyle.getAttribute(StyleConstants.Italic)
						+ BLACK.getRGB();
				stylePool.putIfAbsent(tempStyle, styleString);
				styleString = "." + styleString
						+ " {font-family:Verdana;"
						+ "font-size:" + i + ";"
						+ "font-weight:"
						+ ((boolean) tempStyle.getAttribute(StyleConstants.Bold) ? "bold" : "normal") + ";"
						+ "font-style:"
						+ ((boolean) tempStyle.getAttribute(StyleConstants.Italic) ? "italic" : "normal") + ";"
						+ "color:rgb(" + BLACK.getRed() + "," + BLACK.getGreen() + "," + BLACK.getBlue() + ");} ";
				file.writeToHTML(styleString);
				
				// add grey variants
				tempStyle = getStyle(i, style, GREY);
				attrPool.putIfAbsent("" + i + style + GREY, tempStyle);
				styleString = "s"
						+ tempStyle.getAttribute(StyleConstants.FontSize)
						+ tempStyle.getAttribute(StyleConstants.Bold)
						+ tempStyle.getAttribute(StyleConstants.Italic)
						+ GREY.getRGB();
				stylePool.putIfAbsent(tempStyle, styleString);
				styleString = "." + styleString
						+ " {font-family:Verdana;"
						+ "font-size:" + i + ";"
						+ "font-weight:"
						+ ((boolean) tempStyle.getAttribute(StyleConstants.Bold) ? "bold" : "normal") + ";"
						+ "font-style:"
						+ ((boolean) tempStyle.getAttribute(StyleConstants.Italic) ? "italic" : "normal") + ";"
						+ "color:rgb(" + GREY.getRed() + "," + GREY.getGreen() + "," + GREY.getBlue() + ");} ";
				file.writeToHTML(styleString);
				
				// go through all the other colour variants ...
				for (final Color colour : colours)
				{
					tempStyle = getStyle(i, style, colour);
					attrPool.putIfAbsent("" + i + style + colour, tempStyle);
					styleString = "s"
							+ tempStyle.getAttribute(StyleConstants.FontSize)
							+ tempStyle.getAttribute(StyleConstants.Bold)
							+ tempStyle.getAttribute(StyleConstants.Italic)
							+ colour.getRGB();
					stylePool.putIfAbsent(tempStyle, styleString);
					styleString = "." + styleString
							+ " {font-family:Verdana;"
							+ "font-size:" + i + ";"
							+ "font-weight:"
							+ ((boolean) tempStyle.getAttribute(StyleConstants.Bold) ? "bold" : "normal") + ";"
							+ "font-style:"
							+ ((boolean) tempStyle.getAttribute(StyleConstants.Italic) ? "italic" : "normal") + ";"
							+ "color:rgb(" + colour.getRed() + "," + colour.getGreen() + "," + colour.getBlue() + ");} ";
					file.writeToHTML(styleString);
				}
			}
		}
		
		file.writeToHTML("</style></head>");
	}
	
	//======================================================================================
	// #endregion Initialisation.
	////////////////////////////////////////////////////////////////////////////////////////
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Posting.
	// ======================================================================================
	
	//--------------------------------------------------------------------------------------
	// #region Info posting.
	
	void info(final String entry, final int... coloursToUse)
	{
		postTimeStamp(Options.getInstance().isShowOnlyErrors());
		
		// line label
		tempStyle = attrPool.get("" + Options.getInstance().getFontSize() + Style.BOLDITALIC + GREEN);
		gui.append("Info: ", tempStyle, Options.getInstance().isShowOnlyErrors());
		
		// append the entries on new lines using number of colours passed.
		postEntry(entry, coloursToUse);
	}
	
	void info(final int coloursToUse, final String... entries)
	{
		postTimeStamp(Options.getInstance().isShowOnlyErrors());
		
		// entry label.
		tempStyle = attrPool.get("" + Options.getInstance().getFontSize() + Style.BOLDITALIC + GREEN);
		gui.append("Info ...\n", tempStyle, Options.getInstance().isShowOnlyErrors());
		
		// append the entry using number of colours passed.
		for (final String entry : entries)
		{
			postEntry(entry, coloursToUse);
		}
	}
	
	// this method is the common process of posting entries for the two methods above.
	private synchronized void postEntry(final String entry, final int... coloursToUse)
	{
		// split the entry into sections based on the delimiter '`'
		final String[] entries = entry.split("`");
		
		// calculate number of colours to use. If passed, then use, else if -1 or not passed, then use default.
		int numberOfColours = Arrays.stream(coloursToUse).findFirst().orElse(defaultNumberOfColours);
		numberOfColours = (numberOfColours == -1) ? colours.length : numberOfColours;
		
		tempStyle = attrPool.get("" + Options.getInstance().getFontSize() + Style.PLAIN + BLACK);
		
		// iterate over entry sections
		for (int i = 0; i < entries.length; i++)
		{
			// reset style
			tempStyle = attrPool.get("" + Options.getInstance().getFontSize() + Style.PLAIN + BLACK);
			
			// odd entries are the ones needing colour
			if (((i % 2) == 1) && (numberOfColours > 0))
			{
				// post escaped entry using a different colour.
				tempStyle = attrPool.get("" + Options.getInstance().getFontSize() + Style.PLAIN
						+ colours[(i / 2) % numberOfColours]);
			}
			
			gui.append(entries[i], tempStyle, Options.getInstance().isShowOnlyErrors());
		}
		
		gui.append("\n", tempStyle, Options.getInstance().isShowOnlyErrors());
	}
	
	void infoColoured(final String... strings)
	{
		infoColouredSeparator(defaultColouringSeparator, strings);
	}
	
	void infoColoured(final int coloursToUse, final boolean blackLastString, final String... strings)
	{
		infoColouredSeparator(coloursToUse, blackLastString, defaultColouringSeparator, strings);
	}
	
	void infoColouredSeparator(final String separator, final String... strings)
	{
		infoColouredSeparator(defaultNumberOfColours, defaultBlackLastString, separator, strings);
	}
	
	void infoColouredSeparator(final int coloursToUse, final boolean blackLastString
			, final String separator, final String... strings)
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
	
	void infoColouredSequence(final String separator, final String string, final SequenceOption... options)
	{
		infoColouredSequence(defaultNumberOfColours, separator, string, options);
	}
	
	void infoColouredSequence(final int coloursToUse, final String separator, final String string
			, final SequenceOption... optionsList)
	{
		final List<SequenceOption> options = Arrays.asList(optionsList);
		infoColouredSeparator(coloursToUse
				, options.contains(BLACK_LAST_STRING)
						? true : (options.contains(COLOUR_LAST_STRING) ? false : defaultBlackLastString)
				, options.contains(REMOVE_SEPARATOR) ? "" : separator, string.split(separator));
	}
	
	// #endregion Info posting.
	//--------------------------------------------------------------------------------------
	
	void error(final String entry)
	{
		postTimeStamp(false);
		
		// append line label
		tempStyle = attrPool.get("" + Options.getInstance().getFontSize() + Style.BOLDITALIC + RED);
		gui.append("!! ERROR >> ", tempStyle);
		
		// append the error
		postError(entry);
	}
	
	void errors(final String... entries)
	{
		postTimeStamp(false);
		
		// append line label
		tempStyle = attrPool.get("" + Options.getInstance().getFontSize() + Style.BOLDITALIC + RED);
		gui.append("!! ERRORS !!\n", tempStyle);
		
		// append the errors on new lines
		for (final String entry : entries)
		{
			postError(entry);
		}
	}
	
	// this method is the common process of posting entries for the two methods above.
	private synchronized void postError(final String entry)
	{
		// split the entry into sections based on the delimiter '`'
		final String[] entries = entry.split("`");
		
		tempStyle = attrPool.get("" + Options.getInstance().getFontSize() + Style.PLAIN + RED);
		
		// odd entries are the ones needing colour
		for (int i = 0; i < entries.length; i++)
		{
			// reset style
			tempStyle = attrPool.get("" + Options.getInstance().getFontSize() + Style.PLAIN + RED);
			
			if ((i % 2) == 1)
			{
				// post escaped entry using a different colour.
				tempStyle = attrPool.get("" + Options.getInstance().getFontSize() + Style.PLAIN + BLACK);
			}
			
			// add to log
			gui.append(entries[i], tempStyle);
		}
		
		// add a new line
		gui.append("\n", tempStyle);
	}
	
	void except(final Throwable exception)
	{
		postTimeStamp(false);
		
		tempStyle = attrPool.get("" + Options.getInstance().getFontSize() + Style.BOLDITALIC + RED);
		gui.append("!! EXCEPTION !!\n", tempStyle);
		
		// define how to handle the character in the stack trace.
		final PrintWriter exceptionWriter = new PrintWriter(new Writer()
		{
			
			// store lines in the stack trace to print later.
			List<String>	lines	= new ArrayList<String>();
			
			@Override
			public void write(final char[] cbuf, final int off, final int len) throws IOException
			{
				lines.add(new String(cbuf, off, len));
			}
			
			@Override
			public void flush() throws IOException
			{
				tempStyle = attrPool.get("" + Options.getInstance().getFontSize() + Style.PLAIN + RED);
				
				for (final String line : lines)
				{
					gui.append(line, tempStyle);		// send to the logger.
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
		
		gui.append("\r", tempStyle);		// send to the logger.
	}
	
	// ======================================================================================
	// #endregion Public posting interface.
	// //////////////////////////////////////////////////////////////////////////////////////
	
	// add to stylised history queue.
	void addToHistory(final String entry, final AttributeSet style)
	{
		try
		{
			historyTextQueue.put(entry);
			historyAttributeQueue.put(style);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	// #region System stream capture.
	//======================================================================================
	
	/**
	 * Capture sysout.
	 */
	public void captureSysOut()
	{
		System.setOut(new PrintStreamCapturer(System.out, OUT));
		System.setErr(new PrintStreamCapturer(System.err, ERROR));
	}
	
	// post using a single colour, and prefix with 'Stream:'. Used by PrintStreamCapturer.
	void stream(final String text)
	{
		postTimeStamp(Options.getInstance().isShowOnlyErrors());
		
		// line label
		tempStyle = attrPool.get("" + Options.getInstance().getFontSize() + Style.BOLDITALIC + VIOLET);
		gui.append("Stream: ", tempStyle, Options.getInstance().isShowOnlyErrors());
		
		// append the entries on new lines using number of colours passed.
		postEntry(text, 0);
	}
	
	// post using a red colour, and prefix with '!! Stream:'. Used by PrintStreamCapturer.
	void streamError(final String text)
	{
		postTimeStamp(false);
		
		// append line label
		tempStyle = attrPool.get("" + Options.getInstance().getFontSize() + Style.BOLDITALIC + RED);
		gui.append("!! Stream: ", tempStyle);
		
		// append the entries on new lines using number of colours passed.
		postError(text);
	}
	
	//======================================================================================
	// #endregion System stream capture.
	////////////////////////////////////////////////////////////////////////////////////////
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Text methods.
	// ======================================================================================
	
	/* post time stamp to log. */
	private synchronized void postTimeStamp(final boolean saveOnly)
	{
//		postDateIfChanged();
		
		// post date in light colour because it's repeated too much, so it becomes distracting.
		tempStyle = attrPool.get("" + Options.getInstance().getFontSize() + Style.PLAIN + GREY);
		gui.append(getDate() + " ", tempStyle, saveOnly);
		
		// post time in black.
		tempStyle = attrPool.get("" + Options.getInstance().getFontSize() + Style.PLAIN + BLACK);
		gui.append(getTime() + " ", tempStyle, saveOnly);
	}
	
	/* create a current date string. */
	private String getDate()
	{
		return new SimpleDateFormat("dd/MMM/yy").format(new Date()) /* DateFormat.getDateInstance().format(new Date()) */;
	}
	
	/* Create a current time string. */
	private String getTime()
	{
		return new SimpleDateFormat("hh:mm:ss aa").format(new Date());
	}
	
	// convenience method
	private AttributeSet getStyle(final Style style, final Color... colour)
	{
		return getStyle(Options.getInstance().getFontSize(), style, colour);
	}
	
	/*
	 * Forms and returns the style as an {@link AttributeSet} to be used with {@link JTextPane}.
	 * Pass '0' for size to use the default one. Colour is optional, black is used by default.
	 *
	 * Credit: Philip Isenhour (http://javatechniques.com/blog/setting-jtextpane-font-and-color/)
	 */
	private AttributeSet getStyle(final int size, final Style style, final Color... colour)
	{
		// if nothing is displayed, then no style is needed.
		if (GUI.getInstance().getTextPane() == null)
		{
			return null;
		}
		
		// Start with the current input attributes for the JTextPane. This
		// should ensure that we do not wipe out any existing attributes
		// (such as alignment or other paragraph attributes) currently
		// set on the text area.
		synchronized (GUI.getInstance().getLogAttributesLock())
		{
			final MutableAttributeSet attributes = GUI.getInstance().getTextPane().getInputAttributes();
			
			final Font font = new Font(this.font
					, ((style == Style.BOLD) ? Font.BOLD : 0)/*
																+ ((style == Style.ITALIC) ? Font.ITALIC : 0)*/
							+ ((style == Style.BOLDITALIC) ? Font.ITALIC + Font.BOLD : 0)
					, (size <= 0) ? Options.getInstance().getFontSize() : size);
			
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
	private String getHTML(final String text, final AttributeSet style)
	{
		conversionPane.setText("");
		
		try
		{
			// replace characters that aren't parsed by the HTML panel!!!
			// add the plain text to an HTML editor to convert the text to HTML.
			conversionPane.getDocument().insertString(conversionPane.getDocument().getLength()
					, text.replace("\n", "`new_line`").replace("\t", "`tab`")
							.replace(" ", "`space`")
					, null);
		}
		catch (final BadLocationException e)
		{
			e.printStackTrace();
			return "";
		}
		
		// remove unnecessary tags, and return tags that were replaced above.
		// get the text back from the editor as HTML.
		return "<span class=\"" + stylePool.get(style) + "\">"		// add CSS
				+ conversionPane.getText()
						.replace("<html>", "").replace("</html>", "").replace("<head>", "")		// remove redundant tags
						.replace("</head>", "").replace("<body>", "").replace("</body>", "")
						.replace("`new_line`", "<br />").replace("`tab`", "&#9;").replace("`space`", "&nbsp;")	// return tags
						.replace("<p style=\"margin-top: 0\">", "").replace("</p>", "").replace("<p>", "")	// remove extra tags
						// remove new line characters, and spaces outside tags (have no effect on resulting html)
						.replace("\n", "").replaceAll(" {2,}", "")
				+ "</span>";
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
	public void saveAsHTML()
	{
		if ( !initialised)
		{
			return;
		}
		
		// !comments are in saveAsTxt!
		
		final Path chosenFolder = chooseFolder();
		
		if (chosenFolder == null)
		{
			return;
		}
		
		final Path file = chosenFolder.resolve("log_file_-_" + File.getInstance().getFileStamp() + ".html");
		
		new Thread(() ->
		{
			try
			{
				synchronized (historyTextQueue)
				{
					File.getInstance().flushStreams();
					Files.copy(File.getInstance().getHtmlFile(), file);
					
					try (Writer writer = new OutputStreamWriter(Files.newOutputStream(file, StandardOpenOption.APPEND)))
					{
						writer.write("</html></body>");
					}
					catch (final IOException e)
					{
						e.printStackTrace();
					}
				}
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}).start();
	}
	
	/**
	 * Save as txt.
	 */
	public void saveAsTxt()
	{
		if ( !initialised)
		{
			return;
		}
		
		// ask the user for folder to save to.
		final Path chosenFolder = chooseFolder();
		
		// if nothing is chosen, do nothing.
		if (chosenFolder == null)
		{
			return;
		}
		
		// get the full path of the file, using the parent, and a time stamp.
		final Path file = chosenFolder.resolve("log_file_-_" + File.getInstance().getFileStamp() + ".txt");
		
		// copy the already existing log file
		new Thread(() ->
		{
			try
			{
				Files.copy(File.getInstance().getTextFile(), file);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}).start();
	}
	
	/**
	 * Choose folder.
	 *
	 * @return Local folder
	 */
	private Path chooseFolder()
	{
		if (Files.notExists(Paths.get(Options.getInstance().getLastDirectory())))
		{
			Options.getInstance().setLastDirectory(System.getProperty("user.home"));
		}
		
		// only choose directories.
		final JFileChooser chooser = new JFileChooser(Options.getInstance().getLastDirectory());
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		// show dialogue
		final int result = chooser.showOpenDialog(GUI.getInstance().getFrame());
		final java.io.File selectedFolder = chooser.getSelectedFile();
		
		// if a folder was not chosen ...
		if ((result != JFileChooser.APPROVE_OPTION) || (selectedFolder == null))
		{
			return null;
		}
		
		Options.getInstance().setLastDirectory(selectedFolder.toString());
		
		return selectedFolder.toPath();
	}
	
	//======================================================================================
	// #endregion Saving.
	////////////////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////////////////
	// #region Getters and setters.
	//======================================================================================
	
	/**
	 * @return the initialised
	 */
	public boolean isInitialised()
	{
		return initialised;
	}
	
	/**
	 * @param initialised
	 *            the initialised to set
	 */
	public void setInitialised(final boolean initialised)
	{
		this.initialised = initialised;
	}
	
	//======================================================================================
	// #endregion Getters and setters.
	////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * @return the gui
	 */
	public GUI getGui()
	{
		return gui;
	}
	
	Logger()
	{}
	
}


/**
 * <p>
 * A PrintStreamCapturer is passed to {@link System#setOut(PrintStream)}. It redirects the console output stream to this logger.
 * </p>
 * Credit: Joffrey (<a href="http://stackoverflow.com">StackOverflow</a>)
 */
class PrintStreamCapturer extends PrintStream
{
	
	private CaptureType	captureType	= OUT;
	
	/**
	 * The Enum CaptureType.
	 */
	public static enum CaptureType
	{
		
		/** Error. */
		ERROR,
		
		/** Out. */
		OUT
	}
	
	public PrintStreamCapturer(final PrintStream capturedStream, final CaptureType type)
	{
		super(capturedStream);
		captureType = type;
	}
	
	private void write(final String str)
	{
		if ( !Options.getInstance().isCaptureConsole())
		{
			return;
		}
		
		final String[] s = str.split("\n");
		
		if (s.length == 0)
		{
			return;
		}
		
		for (int i = 0; i < s.length; i++)
		{
			if ((i >= (s.length - 1)) && s[i].equals(""))
			{
				continue;
			}
			
			if (captureType == OUT)
			{
				Log.getInstance().stream(s[i]);
			}
			else if (captureType == ERROR)
			{
				Log.getInstance().streamError(s[i]);
			}
		}
	}
	
	private void newLine()
	{
		write("\n");
	}
	
	@Override
	public void print(final boolean b)
	{
		synchronized (this)
		{
			super.print(b);
			write(String.valueOf(b));
		}
	}
	
	@Override
	public void print(final char c)
	{
		synchronized (this)
		{
			super.print(c);
			write(String.valueOf(c));
		}
	}
	
	@Override
	public void print(final char[] s)
	{
		synchronized (this)
		{
			super.print(s);
			write(String.valueOf(s));
		}
	}
	
	@Override
	public void print(final double d)
	{
		synchronized (this)
		{
			super.print(d);
			write(String.valueOf(d));
		}
	}
	
	@Override
	public void print(final float f)
	{
		synchronized (this)
		{
			super.print(f);
			write(String.valueOf(f));
		}
	}
	
	@Override
	public void print(final int i)
	{
		synchronized (this)
		{
			super.print(i);
			write(String.valueOf(i));
		}
	}
	
	@Override
	public void print(final long l)
	{
		synchronized (this)
		{
			super.print(l);
			write(String.valueOf(l));
		}
	}
	
	@Override
	public void print(final Object o)
	{
		synchronized (this)
		{
			super.print(o);
			write(String.valueOf(o));
		}
	}
	
	@Override
	public void print(final String s)
	{
		synchronized (this)
		{
			super.print(s);
			if (s == null)
			{
				write("null");
			}
			else
			{
				write(s);
			}
		}
	}
	
	@Override
	public void println()
	{
		synchronized (this)
		{
			newLine();
			super.println();
		}
	}
	
	@Override
	public void println(final boolean x)
	{
		synchronized (this)
		{
			print(x);
			newLine();
			super.println();
		}
	}
	
	@Override
	public void println(final char x)
	{
		synchronized (this)
		{
			print(x);
			newLine();
			super.println();
		}
	}
	
	@Override
	public void println(final int x)
	{
		synchronized (this)
		{
			print(x);
			newLine();
			super.println();
		}
	}
	
	@Override
	public void println(final long x)
	{
		synchronized (this)
		{
			print(x);
			newLine();
			super.println();
		}
	}
	
	@Override
	public void println(final float x)
	{
		synchronized (this)
		{
			print(x);
			newLine();
			super.println();
		}
	}
	
	@Override
	public void println(final double x)
	{
		synchronized (this)
		{
			print(x);
			newLine();
			super.println();
		}
	}
	
	@Override
	public void println(final char x[])
	{
		synchronized (this)
		{
			print(x);
			newLine();
			super.println();
		}
	}
	
	@Override
	public void println(final String x)
	{
		synchronized (this)
		{
			print(x);
			newLine();
			super.println();
		}
	}
	
	@Override
	public void println(final Object x)
	{
		final String s = String.valueOf(x);
		synchronized (this)
		{
			print(s);
			newLine();
			super.println();
		}
	}
}
