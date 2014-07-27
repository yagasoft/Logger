/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Logger/com.yagasoft.logger.menu.panels.option/OptionsPanel.java
 *
 *			Modified: 24-Jul-2014 (09:54:50)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.logger.menu.panels.option;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import com.yagasoft.logger.GUI;


/**
 * The Class Options.
 */
public class OptionsPanel extends JPanel implements ActionListener
{
	
	/** Constant: SerialVersionUID. */
	private static final long		serialVersionUID	= -1146451184847401905L;
	
	/** The frame to include this options panel. */
	private JFrame					frame;
	
	/** Button ok. */
	private JButton					buttonOk;
	
	/** Button cancel. */
	private JButton					buttonCancel;
	
	/** Listeners. */
	private Set<IOptionsListener>	listeners			= new HashSet<IOptionsListener>();
	private JPanel					panelOptionsList;
	private JLabel					labelNumEntries;
	private JTextField				textFieldNumEntries;
	private JLabel					labelFontSize;
	private JTextField				textFieldFontSize;
	private JCheckBox				checkBoxWrapText;
	private JCheckBox				checkBoxHideOnClose;
	private JLabel					labelTextOptions;
	private JLabel					labelWindowBehaviour;
	
	/**
	 * Create the panel.
	 */
	public OptionsPanel()
	{
		initGUI();
	}
	
	/**
	 * Inits the gui.
	 */
	private void initGUI()
	{
		setLayout(new BorderLayout(0, 0));
		
		// buttons
		JPanel buttonsPanel = new JPanel(new FlowLayout());
		
		buttonOk = new JButton("OK");
		buttonOk.addActionListener(this);
		buttonsPanel.add(buttonOk);
		
		buttonCancel = new JButton("Cancel");
		buttonCancel.addActionListener(this);
		buttonsPanel.add(buttonCancel);
		
		add(buttonsPanel, BorderLayout.SOUTH);
		//
		panelOptionsList = new JPanel();
		add(panelOptionsList, BorderLayout.CENTER);
		SpringLayout panelOptionsListSpringLayout = new SpringLayout();
		panelOptionsList.setLayout(panelOptionsListSpringLayout);
		//
		labelTextOptions = new JLabel("Text options:");
		panelOptionsListSpringLayout.putConstraint(SpringLayout.NORTH, labelTextOptions, 7, SpringLayout.NORTH, panelOptionsList);
		panelOptionsListSpringLayout.putConstraint(SpringLayout.WEST, labelTextOptions, 7, SpringLayout.WEST, panelOptionsList);
		labelTextOptions.setHorizontalAlignment(SwingConstants.TRAILING);
		panelOptionsList.add(labelTextOptions);
		//
		labelNumEntries = new JLabel("Max of entries:");
		panelOptionsListSpringLayout.putConstraint(SpringLayout.NORTH, labelNumEntries, 6, SpringLayout.SOUTH, labelTextOptions);
		panelOptionsList.add(labelNumEntries);
		//
		textFieldNumEntries = new JTextField();
		panelOptionsListSpringLayout
				.putConstraint(SpringLayout.EAST, labelNumEntries, -6, SpringLayout.WEST, textFieldNumEntries);
		panelOptionsListSpringLayout.putConstraint(SpringLayout.NORTH, textFieldNumEntries, 26, SpringLayout.NORTH,
				panelOptionsList);
		panelOptionsListSpringLayout.putConstraint(SpringLayout.WEST, textFieldNumEntries, 120, SpringLayout.WEST,
				panelOptionsList);
		panelOptionsListSpringLayout.putConstraint(SpringLayout.EAST, textFieldNumEntries, -10, SpringLayout.EAST,
				panelOptionsList);
		textFieldNumEntries.setText(GUI.getMaxEntries() + "");
		panelOptionsList.add(textFieldNumEntries);
		textFieldNumEntries.setColumns(10);
		//
		labelFontSize = new JLabel("Font size:");
		panelOptionsListSpringLayout.putConstraint(SpringLayout.NORTH, labelFontSize, 6, SpringLayout.SOUTH, labelNumEntries);
		panelOptionsListSpringLayout.putConstraint(SpringLayout.EAST, labelFontSize, 0, SpringLayout.EAST, labelNumEntries);
		panelOptionsList.add(labelFontSize);
		//
		textFieldFontSize = new JTextField();
		panelOptionsListSpringLayout.putConstraint(SpringLayout.NORTH, textFieldFontSize, 0, SpringLayout.SOUTH,
				textFieldNumEntries);
		panelOptionsListSpringLayout.putConstraint(SpringLayout.WEST, textFieldFontSize, 6, SpringLayout.EAST, labelFontSize);
		panelOptionsListSpringLayout
				.putConstraint(SpringLayout.EAST, textFieldFontSize, -23, SpringLayout.EAST, panelOptionsList);
		textFieldFontSize.setText(GUI.getFontSize() + "");
		panelOptionsList.add(textFieldFontSize);
		textFieldFontSize.setColumns(10);
		//
		checkBoxWrapText = new JCheckBox("Wrap text");
		checkBoxWrapText.setSelected(GUI.isWrap());
		panelOptionsListSpringLayout
				.putConstraint(SpringLayout.NORTH, checkBoxWrapText, 3, SpringLayout.SOUTH, textFieldFontSize);
		panelOptionsListSpringLayout.putConstraint(SpringLayout.WEST, checkBoxWrapText, 41, SpringLayout.WEST, panelOptionsList);
		panelOptionsList.add(checkBoxWrapText);
		//
		checkBoxHideOnClose = new JCheckBox("Hide on close");
		checkBoxHideOnClose.setSelected(GUI.isHideOnClose());
		panelOptionsListSpringLayout.putConstraint(SpringLayout.WEST, checkBoxHideOnClose, 31, SpringLayout.WEST,
				panelOptionsList);
		panelOptionsListSpringLayout.putConstraint(SpringLayout.SOUTH, checkBoxHideOnClose, -1, SpringLayout.SOUTH,
				panelOptionsList);
		panelOptionsList.add(checkBoxHideOnClose);
		//
		labelWindowBehaviour = new JLabel("Window behaviour:");
		panelOptionsListSpringLayout.putConstraint(SpringLayout.NORTH, labelWindowBehaviour, 12, SpringLayout.SOUTH,
				checkBoxWrapText);
		panelOptionsListSpringLayout.putConstraint(SpringLayout.WEST, labelWindowBehaviour, 0, SpringLayout.WEST,
				labelTextOptions);
		panelOptionsList.add(labelWindowBehaviour);
		
		panelOptionsList.setPreferredSize(new Dimension(160, 140));
	}
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == buttonCancel)
		{
			clearListeners();
			frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
		}
		else if (e.getSource() == buttonOk)
		{
			notifyListeners();
			clearListeners();
			frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
		}
	}
	
	/**
	 * Adds the listener.
	 *
	 * @param listener
	 *            Listener.
	 */
	public void addListener(IOptionsListener listener)
	{
		listeners.add(listener);
	}
	
	/**
	 * Notify listeners.
	 */
	private void notifyListeners()
	{
		listeners.parallelStream()
				.forEach(listener -> listener.optionsSet(
						new Options(Integer.parseInt(textFieldNumEntries.getText())
								, Integer.parseInt(textFieldFontSize.getText())
								, checkBoxWrapText.isSelected()
								, checkBoxHideOnClose.isSelected())));
	}
	
	/**
	 * Removes the listener.
	 *
	 * @param listener
	 *            Listener.
	 */
	public void removeListener(IOptionsListener listener)
	{
		listeners.remove(listener);
	}
	
	/**
	 * Clear listeners.
	 */
	public void clearListeners()
	{
		listeners.clear();
	}
	
	/**
	 * Gets the frame.
	 *
	 * @return the frame
	 */
	public JFrame getFrame()
	{
		return frame;
	}
	
	/**
	 * Sets the frame.
	 *
	 * @param frame
	 *            the frame to set
	 */
	public void setFrame(JFrame frame)
	{
		this.frame = frame;
		frame.setResizable(false);
	}
}
