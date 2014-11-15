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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;


/**
 * The Class Options.
 */
public class OptionsPanel extends JPanel implements ActionListener
{
	
	/** Constant: SerialVersionUID. */
	private static final long		serialVersionUID	= -1146451184847401905L;
	
	/** The frame to include this options panel. */
	private JFrame					frame;
	
	private transient JButton		buttonOk;
	private transient JButton		buttonCancel;
	private transient JTextField	textFieldNumEntries;
	private transient JTextField	textFieldFontSize;
	private transient JCheckBox		checkBoxWrapText;
	private transient JCheckBox		checkBoxCaptureConsole;
	private transient JCheckBox		checkBoxHideOnClose;
	
	/**
	 * Create the panel.
	 */
	public OptionsPanel()
	{
		super();
		initGUI();
	}
	
	/**
	 * Inits the gui.
	 */
	private void initGUI()
	{
		setLayout(new BorderLayout(0, 0));
		
		// buttons
		final JPanel buttonsPanel = new JPanel(new FlowLayout());
		
		buttonOk = new JButton("OK");
		buttonOk.addActionListener(this);
		buttonsPanel.add(buttonOk);
		
		buttonCancel = new JButton("Cancel");
		buttonCancel.addActionListener(this);
		buttonsPanel.add(buttonCancel);
		
		add(buttonsPanel, BorderLayout.SOUTH);
		//
		final JPanel panelOptionsList = new JPanel();
		add(panelOptionsList, BorderLayout.CENTER);
		final SpringLayout panelOptionsListSpringLayout = new SpringLayout();
		panelOptionsList.setLayout(panelOptionsListSpringLayout);
		//
		final JLabel labelTextOptions = new JLabel("Text options:");
		panelOptionsListSpringLayout.putConstraint(SpringLayout.NORTH, labelTextOptions, 7, SpringLayout.NORTH, panelOptionsList);
		panelOptionsListSpringLayout.putConstraint(SpringLayout.WEST, labelTextOptions, 7, SpringLayout.WEST, panelOptionsList);
		labelTextOptions.setHorizontalAlignment(SwingConstants.TRAILING);
		panelOptionsList.add(labelTextOptions);
		//
		final JLabel labelNumEntries = new JLabel("Max of entries:");
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
		textFieldNumEntries.setText(Options.getInstance().getNumberOfEntries().toString());
		panelOptionsList.add(textFieldNumEntries);
		textFieldNumEntries.setColumns(10);
		//
		final JLabel labelFontSize = new JLabel("Font size:");
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
		textFieldFontSize.setText(Options.getInstance().getFontSize().toString());
		panelOptionsList.add(textFieldFontSize);
		textFieldFontSize.setColumns(10);
		//
		checkBoxWrapText = new JCheckBox("Wrap text");
		checkBoxWrapText.setSelected(Options.getInstance().isWrap());
		panelOptionsListSpringLayout
				.putConstraint(SpringLayout.NORTH, checkBoxWrapText, 3, SpringLayout.SOUTH, textFieldFontSize);
		panelOptionsListSpringLayout.putConstraint(SpringLayout.WEST, checkBoxWrapText, 41, SpringLayout.WEST, panelOptionsList);
		panelOptionsList.add(checkBoxWrapText);
		//
		checkBoxHideOnClose = new JCheckBox("Hide on close");
		panelOptionsListSpringLayout.putConstraint(SpringLayout.WEST, checkBoxHideOnClose, 0, SpringLayout.WEST, labelNumEntries);
		checkBoxHideOnClose.setSelected(Options.getInstance().isHideOnClose());
		panelOptionsList.add(checkBoxHideOnClose);
		//
		final JLabel labelWindowBehaviour = new JLabel("Behaviour:");
		panelOptionsListSpringLayout.putConstraint(SpringLayout.NORTH, labelWindowBehaviour, 12, SpringLayout.SOUTH,
				checkBoxWrapText);
		panelOptionsListSpringLayout.putConstraint(SpringLayout.WEST, labelWindowBehaviour, 0, SpringLayout.WEST,
				labelTextOptions);
		panelOptionsList.add(labelWindowBehaviour);
		//
		checkBoxCaptureConsole = new JCheckBox("Capture console");
		checkBoxCaptureConsole.setSelected(Options.getInstance().isCaptureConsole());
		panelOptionsListSpringLayout.putConstraint(SpringLayout.NORTH, checkBoxHideOnClose, 6, SpringLayout.SOUTH,
				checkBoxCaptureConsole);
		panelOptionsListSpringLayout.putConstraint(SpringLayout.NORTH, checkBoxCaptureConsole, 6, SpringLayout.SOUTH,
				labelWindowBehaviour);
		panelOptionsListSpringLayout.putConstraint(SpringLayout.WEST, checkBoxCaptureConsole, 0, SpringLayout.WEST,
				labelNumEntries);
		panelOptionsList.add(checkBoxCaptureConsole);
		
		//
		panelOptionsList.setPreferredSize(new Dimension(160, 175));
	}
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent event)
	{
		if (event.getSource() == buttonCancel)
		{
			frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
		}
		else if (event.getSource() == buttonOk)
		{
			applyOptions();
			frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
		}
	}
	
	/**
	 * Apply options to the application.
	 */
	private void applyOptions()
	{
		final Options options = Options.getInstance();
		options.setNumberOfEntries(Integer.parseInt(textFieldNumEntries.getText()));
		options.setFontSize(Integer.parseInt(textFieldFontSize.getText()));
		options.setWrap(checkBoxWrapText.isSelected());
		options.setHideOnClose(checkBoxHideOnClose.isSelected());
		options.setCaptureConsole(checkBoxCaptureConsole.isSelected());
		
		options.saveOptions();
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
	public void setFrame(final JFrame frame)
	{
		this.frame = frame;
		frame.setResizable(false);
	}
}
