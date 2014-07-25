/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Logger/com.yagasoft.logger.menu.panels.option/IOptionsListener.java
 *
 *			Modified: 24-Jul-2014 (16:17:01)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.logger.menu.panels.option;


/**
 * The listener interface for receiving IOptions events.
 * The class that is interested in processing a IOptions
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addIOptionsListener<code> method. When
 * the IOptions event occurs, that object's appropriate
 * method is invoked.
 */
@FunctionalInterface
public interface IOptionsListener
{

	/**
	 * Options set.
	 *
	 * @param options
	 *            Options.
	 */
	void optionsSet(Options options);
}
