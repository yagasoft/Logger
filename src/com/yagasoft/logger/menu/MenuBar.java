//******************************************************************
// Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
//
//		The Modified MIT Licence (GPL v3 compatible)
// 			Licence terms are in a separate file (LICENCE.md)
//
//		Project/File: Logger/com.yagasoft.logger.menu/MenuBar.java
//
//			Modified: 31-Jul-2014 (10:12:37)
//			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
//******************************************************************

package com.yagasoft.logger.menu;


import java.awt.event.WindowEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;

import com.yagasoft.logger.GUI;
import com.yagasoft.logger.Log;
import com.yagasoft.logger.menu.panels.AboutPanel;
import com.yagasoft.logger.menu.panels.option.Options;
import com.yagasoft.logger.menu.panels.option.OptionsPanel;


/**
 * The Class MenuBar.
 */
public class MenuBar extends JMenuBar
{

	/** Constant: SerialVersionUID. */
	private static final long	serialVersionUID	= -8001930077352279137L;

	/**
	 * Instantiates a new menu bar.
	 */
	public MenuBar()
	{
		super();
		initMenu();
	}

	/**
	 * Initialise the menu.
	 */
	private void initMenu()
	{
		// build file menu
		final JMenu fileMenu = new JMenu("File");

		final JMenuItem saveAsHTML = new JMenuItem("Save as HTML ...");
		saveAsHTML.addActionListener(event -> Log.getInstance().saveAsHTML());
		fileMenu.add(saveAsHTML);

		final JMenuItem saveAsTxt = new JMenuItem("Save as text file ...");
		saveAsTxt.addActionListener(event -> Log.getInstance().saveAsTxt());
		fileMenu.add(saveAsTxt);

		final JMenuItem hide = new JMenuItem("Hide");
		hide.addActionListener(event -> GUI.getInstance().hideLogger());
		fileMenu.add(hide);

		final JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(event ->
		{
			GUI.getInstance().getFrame().setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			GUI.getInstance().getFrame().dispatchEvent(new WindowEvent(GUI.getInstance().getFrame(), WindowEvent.WINDOW_CLOSING));
		});

		fileMenu.add(exit);

		add(fileMenu);

		// build edit menu
		final JMenu editMenu = new JMenu("Edit");

		final JMenuItem onlyErrorsToggle = new JCheckBoxMenuItem("Show only errors");
		onlyErrorsToggle.setSelected(Options.getInstance().isShowOnlyErrors());
		onlyErrorsToggle.addActionListener(event ->
		{
			Options.getInstance().setShowOnlyErrors(onlyErrorsToggle.isSelected());
			Options.getInstance().saveOptions();
		});
		editMenu.add(onlyErrorsToggle);
		editMenu.addActionListener(event -> onlyErrorsToggle.setSelected(Options.getInstance().isShowOnlyErrors()));

		final JMenuItem options = new JMenuItem("Options");
		options.addActionListener(event ->
		{
			final OptionsPanel optionsPanel = new OptionsPanel();
			final JFrame frame = GUI.getInstance().showSubWindow(optionsPanel, "Options");
			optionsPanel.setFrame(frame);
		});
		editMenu.add(options);

		final JMenuItem clear = new JMenuItem("Clear log");
		clear.addActionListener(event -> GUI.getInstance().clearLog());
		editMenu.add(clear);

		add(editMenu);

		// build help menu
		final JMenu helpMenu = new JMenu("Help");
		final JMenuItem about = new JMenuItem("About");
		about.addActionListener(event -> GUI.getInstance().showSubWindow(new AboutPanel(), "About"));
		helpMenu.add(about);
		add(helpMenu);
	}
}
