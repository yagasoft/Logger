//******************************************************************
// Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
//
//		The Modified MIT Licence (GPL v3 compatible)
// 			Licence terms are in a separate file (LICENCE.md)
//
//		Project/File: Logger/com.yagasoft.logger.menu.panels/AboutPanel.java
//
//			Modified: 31-Jul-2014 (10:13:10)
//			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
//******************************************************************

package com.yagasoft.logger.menu.panels;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import com.yagasoft.logger.Log;


/**
 * The 'About' panel containing info about this software.
 */
public class AboutPanel extends JPanel
{

	/** Constant: SerialVersionUID. */
	private static final long	serialVersionUID	= -749076089730034103L;

	/**
	 * Create the panel.
	 */
	public AboutPanel()
	{
		super();
		initGUI();
	}

	/**
	 * Build the 'About' panel..
	 */
	private void initGUI()
	{
		setLayout(new BorderLayout());

		// restrict size.
		setPreferredSize(new Dimension(800, 400));

		// the title and version part.
		final String headerText = "Logger v" + Log.VERSION + "\n";
		final JLabel header = new JLabel(headerText);
		header.setFont(new Font("Gabriola", Font.PLAIN, 35));
		header.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		header.setHorizontalAlignment(SwingConstants.CENTER);

		// the license and credits part.
		final String license = "\nCopyright (C) 2011-" + (Calendar.getInstance().get(Calendar.YEAR)) + " by Ahmed Osama el-Sawalhy.\n"
				+ "\n"
				+ "          Modified MIT License (GPL v3 compatible):\n"
				+ "\tPermission is hereby granted, free of charge, to any person obtaining a copy\n"
				+ "\tof this software and associated documentation files (the \"Software\"), to deal\n"
				+ "\tin the Software without restriction, including without limitation the rights\n"
				+ "\tto use, copy, modify, merge, publish, distribute, sub-license, and/or sell\n"
				+ "\tcopies of the Software, and to permit persons to whom the Software is\n"
				+ "\tfurnished to do so, subject to the following conditions:\n"
				+ "\n"
				+ "\tThe above copyright notice and this permission notice shall be included in\n"
				+ "\tall copies or substantial portions of the Software.\n"
				+ "\n"
				+ "\tExcept as contained in this notice, the name(s) of the above copyright\n"
				+ "\tholders shall not be used in advertising or otherwise to promote the sale, use\n"
				+ "\tor other dealings in this Software without prior written authorization.\n"
				+ "\n"
				+ "\tThe end-user documentation included with the redistribution, if any, must\n"
				+ "\tinclude the following acknowledgment: \"This product includes software developed\n"
				+ "\tby Ahmed Osama el-Sawalhy (http://yagasoft.com) and his contributors\", in\n"
				+ "\tthe same place and form as other third-party acknowledgments. Alternately, this\n"
				+ "\tacknowledgment may appear in the software itself, in the same form and location\n"
				+ "\tas other such third-party acknowledgments.\n\n"
				+ "\tTHE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n"
				+ "\tIMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n"
				+ "\tFITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n"
				+ "\tAUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n"
				+ "\tLIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n"
				+ "\tOUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN\n"
				+ "\tTHE SOFTWARE.\n";
		final String authorsHeader = "\nAuthor:\n";
		final String authors = "\tAhmed Osama el-Sawalhy\n";
//		String creditsHeader = "\nCredits:\n";
//		String credits = "\t.\n";

		// combine all the above.
		final String text = license + authorsHeader + authors/* + creditsHeader + credits*/;
		final JTextArea textArea = new JTextArea(text);
		textArea.setFont(new Font("Verdana", Font.BOLD, 13));
		textArea.setEditable(false);

		// hyper-link to web-site.
		final JButton linkButton = new JButton("Go to web-site");
		linkButton.addActionListener(event -> openWebSite("http://yagasoft.com"));

		// add to the panel.
		add(header, BorderLayout.NORTH);
		add(new JScrollPane(textArea), BorderLayout.CENTER);
		add(linkButton, BorderLayout.SOUTH);
	}

	/**
	 * Open web site in the default browser.
	 *
	 * <p>
	 * Credit: Vulcan (<a href="http://stackoverflow.com">StackOverflow</a>)
	 * </p>
	 *
	 * @param link
	 *            Link.
	 */
	private void openWebSite(final String link)
	{
		if (Desktop.isDesktopSupported())
		{
			final Desktop desktop = Desktop.getDesktop();

			if (desktop.isSupported(Desktop.Action.BROWSE))
			{
				try
				{
					desktop.browse(new URL(link).toURI());
				}
				catch (IOException | URISyntaxException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
