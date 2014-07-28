/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Logger/com.yagasoft.logger/Entry.java
 *
 *			Modified: 28-Jul-2014 (05:39:18)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.logger;


import javax.swing.text.AttributeSet;


/**
 * An entry is a text and its attributes to be sent to log.
 */
public class Entry
{

	/** Text. */
	public String		text		= "";

	/** Attributes. */
	public AttributeSet	attributes	= null;

	/** Save only. */
	public boolean		saveOnly	= false;

	/**
	 * Instantiates a new entry.
	 *
	 * @param text
	 *            Text.
	 * @param attributes
	 *            Attributes.
	 */
	public Entry(String text, AttributeSet attributes)
	{
		this.text = text;
		this.attributes = attributes;
	}

	/**
	 * Instantiates a new entry.
	 *
	 * @param text
	 *            Text.
	 * @param attributes
	 *            Attributes.
	 * @param saveOnly
	 *            Save only.
	 */
	public Entry(String text, AttributeSet attributes, boolean saveOnly)
	{
		this.text = text;
		this.attributes = attributes;
		this.saveOnly = saveOnly;
	}

}
