/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: KeepUp/com.yagasoft.keepup.ui.menu/MenuBar.java
 *
 *			Modified: 16-Jun-2014 (17:32:15)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.logger.menu;


import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;

import com.yagasoft.logger.GUI;
import com.yagasoft.logger.Logger;
import com.yagasoft.logger.menu.panels.AboutPanel;
import com.yagasoft.logger.menu.panels.option.OptionsPanel;


/**
 * The Class MenuBar.
 */
public class MenuBar extends JMenuBar
{

	/** Constant: SerialVersionUID. */
	private static final long	serialVersionUID	= -8001930077352279137L;

	private JMenu				fileMenu;
	private JMenuItem			saveAsHTML;
	private JMenuItem			saveAsTxt;
	private JMenuItem			hide;
	private JMenuItem			exit;

	private JMenu				editMenu;
	private JMenuItem			clear;
	private JMenuItem			options;

	private JMenu				helpMenu;
	private JMenuItem			about;

	/**
	 * Instantiates a new menu bar.
	 */
	public MenuBar()
	{
		initMenu();
	}

	/**
	 * Initialise the menu.
	 */
	private void initMenu()
	{
		// build file menu
		fileMenu = new JMenu("File");

		saveAsHTML = new JMenuItem("Save as HTML ...");
		saveAsHTML.addActionListener(event -> Logger.saveAsHTML());
		fileMenu.add(saveAsHTML);

		saveAsTxt = new JMenuItem("Save as text file ...");
		saveAsTxt.addActionListener(event -> Logger.saveAsTxt());
		fileMenu.add(saveAsTxt);

		hide = new JMenuItem("Hide");
		hide.addActionListener(event -> GUI.hideLogger());
		fileMenu.add(hide);

		exit = new JMenuItem("Exit");
		exit.addActionListener(event ->
		{
			GUI.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			GUI.frame.dispatchEvent(new WindowEvent(GUI.frame, WindowEvent.WINDOW_CLOSING));
		});

		fileMenu.add(exit);

		add(fileMenu);

		// build edit menu
		editMenu = new JMenu("Edit");

		options = new JMenuItem("Options");
		options.addActionListener(event ->
		{
			OptionsPanel optionsPanel = new OptionsPanel();
			JFrame frame = GUI.showSubWindow(optionsPanel, "Options");
			optionsPanel.setFrame(frame);
			optionsPanel.addListener(options ->
			{
				GUI.setMaxEntries(options.numberOfEntries);
				GUI.setFontSize(options.fontSize);
				GUI.setWrap(options.wrap);
				GUI.setHideOnClose(options.hideOnClose);
				Logger.saveOptions();
			});
		});
		editMenu.add(options);

		clear = new JMenuItem("Clear log");
		clear.addActionListener(event -> GUI.clearLog());
		editMenu.add(clear);

		add(editMenu);

		// build help menu
		helpMenu = new JMenu("Help");
		about = new JMenuItem("About");
		about.addActionListener(event -> GUI.showSubWindow(new AboutPanel(), "About"));
		helpMenu.add(about);
		add(helpMenu);
	}
}
