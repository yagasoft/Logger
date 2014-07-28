/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Logger/com.yagasoft.logger.menu.panels.option/Options.java
 *
 *			Modified: 28-Jul-2014 (04:38:39)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.logger.menu.panels.option;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.yagasoft.logger.GUI;
import com.yagasoft.logger.Log;


/**
 * Options class holds the options to be set in the application. When first instantiated, it holds the default options.
 * It can read the options set in the application, and apply the ones set here to the application.
 * Please remember to add an entry in {@link OptionsPanel#applyOptions()} for any new options added here.
 */
public final class Options implements Serializable
{

	private static final long			serialVersionUID	= 2892595292752232935L;

	/* where the logs will be stored relative to project path. */
	private static transient final Path	OPTIONS_FILE		= Paths.get(System.getProperty("user.dir") + "/var/options.dat");

	private static Options				instance;

	/** Number of entries. */
	private Integer						numberOfEntries		= 500;

	/** Font size. */
	private Integer						fontSize			= 12;

	/** Wrap. */
	private Boolean						wrap				= false;

	/** Hide on close. */
	private Boolean						hideOnClose			= true;

	/** Show only errors. */
	private Boolean						showOnlyErrors		= false;

	/** Capture console. */
	private Boolean						captureConsole		= true;

	/** Last directory. */
	private String						lastDirectory		= System.getProperty("user.home");

	/**
	 * Collect options from all over the application.
	 */
	public synchronized void collectOptions()
	{
		numberOfEntries = getNumberOfEntries();
		fontSize = getFontSize();
		wrap = isWrap();
		hideOnClose = isHideOnClose();
		showOnlyErrors = isShowOnlyErrors();
		captureConsole = isCaptureConsole();
		lastDirectory = getLastDirectory();
	}

	/**
	 * Apply options set to the application.
	 */
	public synchronized void applyOptions()
	{
		setNumberOfEntries(numberOfEntries);
		setFontSize(fontSize);
		setWrap(wrap);
		setHideOnClose(hideOnClose);
		setShowOnlyErrors(showOnlyErrors);
		setCaptureConsole(captureConsole);
	}

	/**
	 * Load options from disk.
	 */
	public synchronized void loadOptions()
	{
		if (Files.notExists(OPTIONS_FILE))
		{
			applyOptions();
			return;
		}

		FileInputStream fileStream;		// incoming link to file.
		ObjectInputStream objectStream;		// link to objects read from file.

		try
		{
			fileStream = new FileInputStream(OPTIONS_FILE.toString());
			objectStream = new ObjectInputStream(fileStream);
			instance = (Options) objectStream.readObject();
			objectStream.close();

			instance.applyOptions();
		}
		catch (ClassNotFoundException | IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Save options to disk.
	 */
	public synchronized void saveOptions()
	{
		collectOptions();

		try
		{
			FileOutputStream fileStream = new FileOutputStream(OPTIONS_FILE.toString());
			ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
			objectStream.writeObject(instance);
			objectStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Gets the single instance of Options.
	 *
	 * @return single instance of Options
	 */
	public static Options getInstance()
	{
		if (instance == null)
		{
			instance = new Options();
		}

		return instance;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// #region Getters and setters.
	//======================================================================================

	/**
	 * @return the numberOfEntries
	 */
	public Integer getNumberOfEntries()
	{
		return numberOfEntries;
	}

	/**
	 * @param numberOfEntries
	 *            the numberOfEntries to set
	 */
	public void setNumberOfEntries(Integer numberOfEntries)
	{
		if ( !Log.instance.isInitialised() || (this.numberOfEntries == numberOfEntries))
		{
			this.numberOfEntries = numberOfEntries;
			return;
		}

		this.numberOfEntries = numberOfEntries;

		GUI.getInstance().trimLog();
	}

	/**
	 * @return the fontSize
	 */
	public Integer getFontSize()
	{
		return fontSize;
	}

	/**
	 * @param fontSize
	 *            the fontSize to set
	 */
	public void setFontSize(Integer fontSize)
	{
		if ((fontSize < 10) || (fontSize > 25))
		{
			return;
		}

		if ( !Log.instance.isInitialised() || (this.fontSize == fontSize))
		{
			this.fontSize = fontSize;
			return;
		}

		this.fontSize = fontSize;

		GUI.getInstance().setFontSize(fontSize);
	}

	/**
	 * @return the wrap
	 */
	public boolean isWrap()
	{
		return wrap;
	}

	/**
	 * @param wrap
	 *            the wrap to set
	 */
	public void setWrap(boolean wrap)
	{
		if ( !Log.instance.isInitialised() || (this.wrap == wrap))
		{
			this.wrap = wrap;
			return;
		}

		this.wrap = wrap;

		GUI.getInstance().setWrap(wrap);
	}

	/**
	 * @return the hideOnClose
	 */
	public boolean isHideOnClose()
	{
		return hideOnClose;
	}

	/**
	 * @param hideOnClose
	 *            the hideOnClose to set
	 */
	public void setHideOnClose(boolean hideOnClose)
	{
		this.hideOnClose = hideOnClose;
		GUI.getInstance().setHideOnClose(hideOnClose);
	}

	/**
	 * @return the showOnlyErrors
	 */
	public boolean isShowOnlyErrors()
	{
		return showOnlyErrors;
	}

	/**
	 * @param showOnlyErrors
	 *            the showOnlyErrors to set
	 */
	public void setShowOnlyErrors(boolean showOnlyErrors)
	{
		this.showOnlyErrors = showOnlyErrors;
	}

	/**
	 * @return the captureConsole
	 */
	public boolean isCaptureConsole()
	{
		return captureConsole;
	}

	/**
	 * @param captureConsole
	 *            the captureConsole to set
	 */
	public void setCaptureConsole(boolean captureConsole)
	{
		if ((this.captureConsole != captureConsole) && captureConsole)
		{
			Log.instance.captureSysOut();
		}

		this.captureConsole = captureConsole;
	}

	/**
	 * @return the lastDirectory
	 */
	public String getLastDirectory()
	{
		return lastDirectory;
	}

	/**
	 * @param lastDirectory
	 *            the lastDirectory to set
	 */
	public void setLastDirectory(String lastDirectory)
	{
		this.lastDirectory = lastDirectory;
	}

	//======================================================================================
	// #endregion Getters and setters.
	////////////////////////////////////////////////////////////////////////////////////////

	private Options()
	{}
}
