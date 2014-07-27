/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Logger/com.yagasoft.logger.menu.panels.option/Options.java
 *
 *			Modified: 27-Jul-2014 (20:18:24)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.logger.menu.panels.option;


import java.io.Serializable;


/**
 * The Class Options.
 */
public class Options implements Serializable
{

	private static final long	serialVersionUID	= 2892595292752232935L;

	/** Number of entries. */
	public Integer					numberOfEntries;

	/** Font size. */
	public Integer					fontSize;

	/** Wrap. */
	public Boolean				wrap;

	/** Hide on close. */
	public Boolean				hideOnClose;

	/** Action on close. */
	public Integer					actionOnClose;

	/** Last directory. */
	public String				lastDirectory;

	/**
	 * Instantiates a new options.
	 */
	public Options()
	{}

	/**
	 * Instantiates a new options.
	 *
	 * @param maxEntries
	 *            Number of entries.
	 * @param fontSize
	 *            Font size.
	 * @param wrap
	 *            Wrap.
	 * @param hideOnClose
	 *            Hide on close.
	 */
	public Options(int maxEntries, int fontSize, boolean wrap, boolean hideOnClose)
	{
		numberOfEntries = maxEntries;
		this.fontSize = fontSize;
		this.wrap = wrap;
		this.hideOnClose = hideOnClose;
	}
}
