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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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

import com.yagasoft.logger.GUI;
import com.yagasoft.logger.Logger;


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
		GridBagLayout panelOptionsListGridBagLayout = new GridBagLayout();
		panelOptionsListGridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		panelOptionsListGridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
		panelOptionsListGridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panelOptionsListGridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelOptionsList.setLayout(panelOptionsListGridBagLayout);
		//
		labelNumEntries = new JLabel("Number of entries:");
		GridBagConstraints labelNumEntriesGridBagConstraints = new GridBagConstraints();
		labelNumEntriesGridBagConstraints.insets = new Insets(0, 0, 5, 5);
		labelNumEntriesGridBagConstraints.anchor = GridBagConstraints.EAST;
		labelNumEntriesGridBagConstraints.gridx = 0;
		labelNumEntriesGridBagConstraints.gridy = 0;
		panelOptionsList.add(labelNumEntries, labelNumEntriesGridBagConstraints);
		//
		textFieldNumEntries = new JTextField();
		textFieldNumEntries.setText(Logger.getMaxEntries() + "");
		GridBagConstraints textFieldNumEntries_GridBagConstraints = new GridBagConstraints();
		textFieldNumEntries_GridBagConstraints.insets = new Insets(0, 0, 5, 0);
		textFieldNumEntries_GridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		textFieldNumEntries_GridBagConstraints.gridx = 1;
		textFieldNumEntries_GridBagConstraints.gridy = 0;
		panelOptionsList.add(textFieldNumEntries, textFieldNumEntries_GridBagConstraints);
		textFieldNumEntries.setColumns(10);
		//
		labelFontSize = new JLabel("Font size:");
		GridBagConstraints labelFontSizeGridBagConstraints = new GridBagConstraints();
		labelFontSizeGridBagConstraints.anchor = GridBagConstraints.EAST;
		labelFontSizeGridBagConstraints.insets = new Insets(0, 0, 5, 5);
		labelFontSizeGridBagConstraints.gridx = 0;
		labelFontSizeGridBagConstraints.gridy = 1;
		panelOptionsList.add(labelFontSize, labelFontSizeGridBagConstraints);
		//
		textFieldFontSize = new JTextField();
		textFieldFontSize.setText(Logger.getFontSize() + "");
		GridBagConstraints textFieldFontSizeGridBagConstraints = new GridBagConstraints();
		textFieldFontSizeGridBagConstraints.insets = new Insets(0, 0, 5, 0);
		textFieldFontSizeGridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		textFieldFontSizeGridBagConstraints.gridx = 1;
		textFieldFontSizeGridBagConstraints.gridy = 1;
		panelOptionsList.add(textFieldFontSize, textFieldFontSizeGridBagConstraints);
		textFieldFontSize.setColumns(10);
		//
		checkBoxWrapText = new JCheckBox("Wrap text");
		checkBoxWrapText.setSelected(GUI.isWrap());
		GridBagConstraints checkBoxWrapTextGridBagConstraints = new GridBagConstraints();
		checkBoxWrapTextGridBagConstraints.gridwidth = 2;
		checkBoxWrapTextGridBagConstraints.gridx = 0;
		checkBoxWrapTextGridBagConstraints.gridy = 2;
		panelOptionsList.add(checkBoxWrapText, checkBoxWrapTextGridBagConstraints);
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
								, checkBoxWrapText.isSelected())));
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
	}

}
