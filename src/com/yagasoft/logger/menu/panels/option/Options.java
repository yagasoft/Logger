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

import javax.swing.WindowConstants;

import com.yagasoft.logger.GUI;
import com.yagasoft.logger.Logger;


/**
 * Options class holds the options to be set in the application. When first instantiated, it holds the default options.
 * It can read the options set in the application, and apply the ones set here to the application.
 * Please remember to add an entry in {@link OptionsPanel#applyOptions()} for any new options added here.
 */
public final class Options implements Serializable
{
	
	private static final long		serialVersionUID	= 2892595292752232935L;
	
	/* where the logs will be stored relative to project path. */
	private transient final Path	OPTIONS_FILE		= Paths.get(System.getProperty("user.dir") + "/var/options.dat");
	
	private static Options			instance;
	
	/** Number of entries. */
	public Integer					numberOfEntries		= 500;
	
	/** Font size. */
	public Integer					fontSize			= 12;
	
	/** Wrap. */
	public Boolean					wrap				= false;
	
	/** Hide on close. */
	public Boolean					hideOnClose			= true;
	
	/** Action on close. */
	public Integer					actionOnClose		= WindowConstants.HIDE_ON_CLOSE;
	
	/** Last directory. */
	public String					lastDirectory		= System.getProperty("user.home");
	
	/** Show only errors. */
	public Boolean					showOnlyErrors		= false;
	
	/** Capture console. */
	public Boolean					captureConsole		= true;
	
	/**
	 * Collect options from all over the application.
	 */
	public synchronized void collectOptions()
	{
		fontSize = GUI.getFontSize();
		wrap = GUI.isWrap();
		hideOnClose = GUI.isHideOnClose();
		captureConsole = Logger.isCaptureConsole();
		actionOnClose = GUI.getActionOnClose();
		showOnlyErrors = GUI.isShowOnlyErrors();
		lastDirectory = Logger.getLastDirectory().toString();
	}
	
	/**
	 * Apply options set to the application.
	 */
	public synchronized void applyOptions()
	{
		GUI.setMaxEntries(numberOfEntries);
		GUI.setFontSize(fontSize);
		GUI.setWrap(wrap);
		GUI.setHideOnClose(hideOnClose);
		GUI.setShowOnlyErrors(showOnlyErrors);
		Logger.setLastDirectory(Paths.get(lastDirectory));
		Logger.setCaptureConsole(captureConsole);
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
		Options options;
		
		try
		{
			fileStream = new FileInputStream(OPTIONS_FILE.toString());
			objectStream = new ObjectInputStream(fileStream);
			options = (Options) objectStream.readObject();
			objectStream.close();
			
			options.applyOptions();
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
	
	/**
	 * Gets the single instance of Options.
	 *
	 * @param options
	 *            the new instance
	 */
	public static void setInstance(Options options)
	{
		instance = options;
		options.applyOptions();
	}
	
	private Options()
	{}
}
