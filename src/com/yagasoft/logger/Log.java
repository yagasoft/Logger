//******************************************************************
// Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
//
//		The Modified MIT Licence (GPL v3 compatible)
// 			Licence terms are in a separate file (LICENCE.md)
//
//		Project/File: Logger/com.yagasoft.logger/Log.java
//
//			Modified: 31-Jul-2014 (11:18:46)
//			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
//******************************************************************

package com.yagasoft.logger;


import com.yagasoft.logger.menu.panels.option.Options;


/**
 * Public 'interface' class for the logger.
 */
public final class Log
{
	
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
	
	/** Constant: VERSION. */
	public static final String	VERSION						= "6.03.235";
	
	/** Instance. Set by calling {@link #initLogger(String, int, boolean)}. */
	private static Logger		instance;
	
	/* Default number of colours. */
	private static int			defaultNumberOfColours		= 7;
	
	/* Default coloured strings separator for {@link #infoColoured(String...)}. */
	private static String		defaultColouringSeparator	= " ";
	
	/* Default black last string flag for {@link #infoColouredSeparator(String, String...)}. */
	private static boolean		defaultBlackLastString;
	
	////////////////////////////////////////////////////////////////////////////////////////
	// #region Initialisation.
	//======================================================================================
	
	/**
	 * Convenience method for {@link #initAndShowLogger(String, int, boolean)}, using defaults (' ', max, false).
	 */
	public static void initAndShowLogger()
	{
		initAndShowLogger(getDefaultColouringSeparator(), getDefaultNumberOfColours(), defaultBlackLastString);
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
	public static void initAndShowLogger(final String defaultSeparator, final int defaultNumberOfColours
			, final boolean defaultBlackLastString)
	{
		initLogger(defaultSeparator, defaultNumberOfColours, defaultBlackLastString);
		getInstance().getGui().showLogger();
	}
	
	/**
	 * Convenience method for {@link #initLogger(String, int, boolean)}, using defaults (' ', max, false).
	 */
	public static void initLogger()
	{
		initLogger(getDefaultColouringSeparator(), getDefaultNumberOfColours(), defaultBlackLastString);
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
	public static void initLogger(final String defaultSeparator, final int defaultNumberOfColours
			, final boolean defaultBlackLastString)
	{
		getInstance().initLogger(defaultSeparator, defaultNumberOfColours, defaultBlackLastString);
	}
	
	/**
	 * Show logger window.
	 */
	public static void showLogger()
	{
		instance.getGui().showLogger();
	}
	
	/**
	 * Hide logger window.
	 */
	public static void hideLogger()
	{
		instance.getGui().hideLogger();
	}
	
	//======================================================================================
	// #endregion Initialisation.
	////////////////////////////////////////////////////////////////////////////////////////
	
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
	public static void info(final String entry, final int... coloursToUse)
	{
		instance.info(entry, coloursToUse);
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
	public static void info(final int coloursToUse, final String... entries)
	{
		instance.info(coloursToUse, entries);
	}
	
	/**
	 * Post strings, coloured using the max number of colours, and separated (added to entry) by the default set separator.
	 * It doesn't colour the last string as black.
	 *
	 * @param strings
	 *            Strings.
	 */
	public static void infoColoured(final String... strings)
	{
		infoColouredSeparator(getDefaultColouringSeparator(), strings);
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
	public static void infoColoured(final int coloursToUse, final boolean blackLastString, final String... strings)
	{
		infoColouredSeparator(coloursToUse, blackLastString, getDefaultColouringSeparator(), strings);
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
	public static void infoColouredSeparator(final String separator, final String... strings)
	{
		infoColouredSeparator(getDefaultNumberOfColours(), defaultBlackLastString, separator, strings);
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
	public static void infoColouredSeparator(final int coloursToUse, final boolean blackLastString
			, final String separator, final String... strings)
	{
		instance.infoColouredSeparator(coloursToUse, blackLastString, separator, strings);
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
	public static void infoColouredSequence(final String separator, final String string, final SequenceOption... options)
	{
		infoColouredSequence(getDefaultNumberOfColours(), separator, string, options);
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
	public static void infoColouredSequence(final int coloursToUse, final String separator, final String string
			, final SequenceOption... optionsList)
	{
		instance.infoColouredSequence(coloursToUse, separator, string, optionsList);
	}
	
	// #endregion Info posting.
	//--------------------------------------------------------------------------------------
	
	/**
	 * Error log entry. You can use '`' character as to wrap words to be coloured black.
	 *
	 * @param entry
	 *            Entry.
	 */
	public static void error(final String entry)
	{
		instance.error(entry);
	}
	
	/**
	 * Error log entry. This will be posted one after the other in the same time-stamp.
	 * You can use '`' character as to wrap words to be coloured black.
	 *
	 * @param entries
	 *            Entries.
	 */
	public static void errors(final String... entries)
	{
		instance.errors(entries);
	}
	
	/**
	 * Exception log entry.
	 *
	 * @param exception
	 *            the Exception.
	 */
	public static void except(final Throwable exception)
	{
		instance.except(exception);
	}
	
	// ======================================================================================
	// #endregion Public posting interface.
	// //////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Capture {@link System#out} or {@link System#err}. Useful for saving the output to file, or minimising the console to tray.
	 */
	public static void captureSysOutErr()
	{
		Options.getInstance().setCaptureConsole(true);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	// #region Setters and getters.
	//======================================================================================
	
	/**
	 * @return the defaultNumberOfColours
	 */
	public static int getDefaultNumberOfColours()
	{
		return defaultNumberOfColours;
	}
	
	/**
	 * @param defaultNumberOfColours
	 *            the defaultNumberOfColours to set
	 */
	public static void setDefaultNumberOfColours(final int defaultNumberOfColours)
	{
		Log.defaultNumberOfColours = defaultNumberOfColours;
	}
	
	/**
	 * @return the defaultColouringSeparator
	 */
	public static String getDefaultColouringSeparator()
	{
		return defaultColouringSeparator;
	}
	
	/**
	 * @param defaultColouringSeparator
	 *            the defaultColouringSeparator to set
	 */
	public static void setDefaultColouringSeparator(final String defaultColouringSeparator)
	{
		Log.defaultColouringSeparator = defaultColouringSeparator;
	}
	
	//======================================================================================
	// #endregion Setters and getters.
	////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Gets the instance.
	 *
	 * @return the instance
	 */
	public static Logger getInstance()
	{
		synchronized (Logger.class)
		{
			if (instance == null)
			{
				instance = new Logger();
			}
			
			return instance;
		}
	}
	
	private Log()
	{}
	
}
