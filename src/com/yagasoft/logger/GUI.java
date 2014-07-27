/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Logger/com.yagasoft.logger/GUI.java
 * 
 *			Modified: 28-Jul-2014 (00:13:55)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.logger;


import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;

import com.yagasoft.logger.menu.MenuBar;


/**
 * The Class
 */
public final class GUI
{
	
	/** set when the log is accessible and ready. */
	public static boolean								initialised				= false;
	
	/** Frame. */
	public static JFrame								frame;
	
	private static boolean								visible					= false;
	private static int									actionOnClose			= WindowConstants.HIDE_ON_CLOSE;
	private static boolean								hideOnClose;
	
	private static JPanel								contentPane;
	private static MenuBar								menuBar;
	private static JScrollPane							scroller;
	static JTextPane									textPane;
	private static DefaultCaret							caret;
	
	/* Max entries to show in logger. */
	private static int									maxEntries				= 500;
	
	/* Font size for log window. */
	private static int									fontSize				= 11;
	
	/* Wrap text in view. */
	private static boolean								wrap					= false;
	
	private static boolean								autoScroll				= true;
	private static boolean								holdingBar				= false;
	
	private static SystemTray							systemTray;
	private static TrayIcon								trayIcon;
	private static Image								appIcon					= new ImageIcon(Logger.class
																						.getResource("images/icon.png"))
																						.getImage();
	// queue used to receive text sent.
	private static LinkedBlockingDeque<String>			textQueue				= new LinkedBlockingDeque<String>();
	private static LinkedBlockingDeque<AttributeSet>	attributeQueue			= new LinkedBlockingDeque<AttributeSet>();
	
	// queue used to cache text sent. It's used temporarily store text until a '\n' is encountered to flush to log.
	private static LinkedBlockingDeque<String>			secondTextQueue			= new LinkedBlockingDeque<String>();
	private static LinkedBlockingDeque<AttributeSet>	secondAttributeQueue	= new LinkedBlockingDeque<AttributeSet>();
	
	private static Thread								writerThread;
	// used for preventing write to log when scrolling manually.
	private static CountDownLatch						latch					= new CountDownLatch(0);
	
	////////////////////////////////////////////////////////////////////////////////////////
	// #region Initialisation.
	//======================================================================================
	
	/**
	 * Inits the logger.
	 */
	public static synchronized void initLogger()
	{
		if ( !File.initialised)
		{
			return;
		}
		
		if (textPane == null)
		{
			initFrame();
			initPanel();
			initLog();
		}
		
		// log writing thread.
		writerThread = new Thread(() ->
		{
			while (true)
			{
				writeToLog();
			}
		});
		
		writerThread.start();
		
		initialised = true;
	}
	
	/* Inits the frame. */
	private static synchronized void initFrame()
	{
		frame = new JFrame("Logger window");
		frame.setDefaultCloseOperation(getActionOnClose());
		frame.setBounds(25, 25, 800, 512);
		frame.setVisible(true);
		visible = true;
		frame.setIconImage(appIcon);
		
		// save options and close DB when application is closing.
		frame.addWindowListener(new WindowAdapter()
		{
			
			@Override
			public void windowIconified(WindowEvent e)
			{
				super.windowIconified(e);
				
				if (SystemTray.isSupported() && isHideOnClose())
				{
					hideLogger();
				}
			}
			
			@Override
			public void windowClosing(WindowEvent e)
			{
				super.windowClosing(e);
				
				// if 'hideOnClose' is false, the application will exit or not based on 'actionOnClose'.
				hideLogger();
				
				try
				{
					File.writer.flush();
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
				
				Logger.saveOptions();
			}
		});
	}
	
	/* Inits the panel. */
	private static synchronized void initPanel()
	{
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		
		frame.setContentPane(contentPane);
		
		menuBar = new MenuBar();
		contentPane.add(menuBar, BorderLayout.NORTH);
		
//		frame.revalidate();
	}
	
	/* Inits the log text area. */
	private static synchronized void initLog()
	{
		if (scroller != null)
		{
			contentPane.remove(scroller);
		}
		
		if (wrap)
		{
			textPane = new JTextPane();
		}
		else
		{
			/*
			 * Credit: Rob Camick (http://tips4java.wordpress.com/2009/01/25/no-wrap-text-pane/)
			 */
			textPane = new JTextPane()
			{
				
				private static final long	serialVersionUID	= 8235187348047035541L;
				
				@Override
				public boolean getScrollableTracksViewportWidth()
				{
					return getUI().getPreferredSize(this).width
					<= getParent().getSize().width;
				}
			};
		}
		
		textPane.setEditable(false);
		caret = (DefaultCaret) textPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		
		scroller = new JScrollPane(textPane);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		scroller.getVerticalScrollBar().addMouseListener(new MouseAdapter()
		{
			
			@Override
			public void mousePressed(MouseEvent e)
			{
				super.mousePressed(e);
				
				// prevent writing.
				latch = new CountDownLatch(1);
				
				holdingBar = true;
				autoScroll = false;
			}
			
			@Override
			public void mouseReleased(MouseEvent e)
			{
				super.mouseReleased(e);
				
				Rectangle visible;
				Rectangle bounds;
				
				synchronized (Logger.logDocumentWriteLock)
				{
					visible = textPane.getVisibleRect();	// get visible rectangle of the log area
					bounds = textPane.getBounds();	// get the size of the log area.
				}
				
//				System.out.println(bounds.height - (visible.height + visible.y));
				
				// if the visible rectangle is not at the bottom, stop auto scrolling
				if ((bounds.height - (visible.height + visible.y)) <= 5)
				{
					autoScroll = true;
				}
				
				holdingBar = false;
				
				// allow writing.
				latch.countDown();
			}
		});
		
		contentPane.add(scroller, BorderLayout.CENTER);
		frame.revalidate();
	}
	
	//======================================================================================
	// #endregion Initialisation.
	////////////////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////////////////
	// #region Window control.
	//======================================================================================
	
	static synchronized void showLogger()
	{
		if ( !initialised || visible)
		{
			return;
		}
		
		restoreWindow();
		visible = true;
		frame.setVisible(visible);
	}
	
	/**
	 * Hide logger. This will try to minimise to tray. The 'minimise' event fired by Swing in general will call this method if
	 * {@link #hideOnClose} is set.
	 */
	public static synchronized void hideLogger()
	{
		if ( !initialised || !visible)
		{
			return;
		}
		
		minimiseToTray();
		
		visible = false;
		frame.setVisible(visible);
	}
	
	/**
	 * Creates an action to be taken that is related to a panel to be opened.
	 * This action is a new frame to include the panel passed,
	 * and disabling for the main window.
	 *
	 * @param panel
	 *            the panel.
	 * @param title
	 *            Title of the frame.
	 * @return J frame
	 */
	public static synchronized JFrame showSubWindow(JPanel panel, String title)
	{
		if ( !initialised)
		{
			return null;
		}
		
		// create a frame for the panel.
		JFrame newFrame = new JFrame(title);
		
		// open the frame relative to the main window.
		Point mainWindowLocation = frame.getLocation();
		newFrame.setLocation((int) mainWindowLocation.getX() + 50, (int) mainWindowLocation.getY() + 50);
		
		// when the frame is closed, dispose of it and return focus to the main window.
		newFrame.addWindowListener(new WindowAdapter()
		{
			
			@Override
			public void windowClosing(WindowEvent e)
			{
				super.windowClosing(e);
				
				newFrame.dispose();
				setMainWindowFocusable(true);
			}
			
		});
		
		// add the passed panel to the frame.
		newFrame.add(panel);
		// show the frame.
		newFrame.setVisible(true);
		// fit the frame to panel.
		newFrame.pack();
		
		// disable the main window.
		setMainWindowFocusable(false);
		
		return newFrame;
	}
	
	/**
	 * Set the focus state of the main window.
	 * This is used when a window is opened on top of this main window
	 * to force the user to finish working with it first.
	 *
	 * @param focusable
	 *            true for allowing focus using the mouse click.
	 */
	public static synchronized void setMainWindowFocusable(boolean focusable)
	{
		if ( !initialised)
		{
			return;
		}
		
		frame.setFocusableWindowState(focusable);
		frame.setEnabled(focusable);
		
		// bring it to front.
		if (focusable)
		{
			frame.setVisible(true);
		}
	}
	
	/*
	 * Minimises the app to the system tray.
	 */
	private static synchronized boolean minimiseToTray()
	{
		if ( !visible || !SystemTray.isSupported())
		{
			return false;
		}
		
		try
		{
			systemTray = SystemTray.getSystemTray();
			
			PopupMenu trayMenu = new PopupMenu();
			MenuItem menuItem;
			menuItem = new MenuItem("Restore");
			menuItem.addActionListener(event -> showLogger());
			trayMenu.add(menuItem);
			
			trayIcon = new TrayIcon(appIcon, "Logger", trayMenu);
			
			trayIcon.addMouseListener(new MouseAdapter()
			{
				
				@Override
				public void mouseClicked(MouseEvent e)
				{
					super.mouseClicked(e);
					
					if (e.getClickCount() >= 2)
					{
						showLogger();
					}
				}
			});
			
			systemTray.add(trayIcon);
			
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	/*
	 * Restores the app from the system tray, and brings it to the front.
	 */
	private static synchronized void restoreWindow()
	{
		frame.setExtendedState(Frame.NORMAL);
		frame.toFront();
		
		if (systemTray != null)
		{
			systemTray.remove(trayIcon);
		}
	}
	
	//======================================================================================
	// #endregion Window control.
	////////////////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////////////////
	// #region Log methods.
	//======================================================================================
	
	/* append text to the log as is using the style passed, then write it to log file. */
	static void append(String text, AttributeSet style)
	{
		if ( !initialised)
		{
			return;
		}
		
		try
		{
			synchronized (textQueue)
			{
				textQueue.put(text);
				attributeQueue.put(style);
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void writeToLog()
	{
		if ( !Logger.initialised)
		{
			return;
		}
		
		try
		{
			String text = textQueue.take();
			AttributeSet attributes = attributeQueue.take().copyAttributes();
			
			latch.await();
			
			if (textPane != null)
			{
				SwingUtilities.invokeLater(() ->
				{
					try
					{
						// check whether a full line is ready for flush.
						if ( !text.contains("\n"))
						{
							secondTextQueue.add(text);
							secondAttributeQueue.add(attributes);
						}
						else
						{
							// if not, then get the last element (includes new line trigger)
							synchronized (textQueue)
							{
								secondTextQueue.add(text);
								secondAttributeQueue.add(attributes);
							}
							
							String textTemp = null;
							AttributeSet attributesTemp = null;
							
							// flush to log.
							while ( !secondTextQueue.isEmpty())
							{
								textTemp = secondTextQueue.poll();
								attributesTemp = secondAttributeQueue.poll();
								
								Logger.addToHistory(textTemp, attributesTemp);
								
								synchronized (Logger.logAttributesLock)
								{
									// add text to log area
									textPane.getInputAttributes().removeAttributes(textPane.getInputAttributes());
									textPane.setCharacterAttributes(textPane.getInputAttributes(), true);
									textPane.setParagraphAttributes(textPane.getInputAttributes(), true);
									
									textPane.getDocument().insertString(textPane.getDocument().getLength(), textTemp,
											attributesTemp);
								}
								
								File.writeToFile(textTemp);		// save to disk log file
							}
							
							// scroll to bottom if was already at the bottom.
							if ( !holdingBar && autoScroll)
							{
								trimLog();
								
								synchronized (Logger.logAttributesLock)
								{
									textPane.getInputAttributes().removeAttributes(textPane.getInputAttributes());
									textPane.setCharacterAttributes(textPane.getInputAttributes(), true);
									textPane.setParagraphAttributes(textPane.getInputAttributes(), true);
									
									caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
									textPane.getDocument().insertString(textPane.getDocument().getLength(), "\r", attributesTemp);
									caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
									textPane.setCaretPosition(textPane.getDocument().getLength());
								}
							}
						}
					}
					catch (Exception e)
					{
						Logger.except(e);
						e.printStackTrace();
					}
				});
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Scrolls to the bottom and left of the log.
	 */
	private static void scroll()
	{
		SwingUtilities.invokeLater(() ->
		{
			if (getEntriesNum() > maxEntries)
			{
				return;
			}
			
			try
			{
				if ((scroller != null) && (scroller.getVerticalScrollBar() != null))
				{
					scroller.getHorizontalScrollBar()
							.setValue(scroller.getHorizontalScrollBar().getMinimum());
					scroller.getVerticalScrollBar()
							.setValue(scroller.getVerticalScrollBar().getMaximum());
				}
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		});
	}
	
	static int countOverLimit(int limit)
	{
		return Math.max(getEntriesNum() - limit, 0);
	}
	
	/** reduce the max entries to be within the limit */
	public static void trimLog()
	{
		// remove extra
		removeEntries(countOverLimit(maxEntries));
	}
	
	/**
	 * Clear log.
	 */
	public static void clearLog()
	{
		if ( !Logger.initialised)
		{
			return;
		}
		
		removeEntries(countOverLimit(0));
	}
	
	/**
	 * Removes the entries.
	 *
	 * @param count
	 *            Count.
	 */
	public static void removeEntries(int count)
	{
		if ( !Logger.initialised)
		{
			return;
		}
		
		SwingUtilities
				.invokeLater(() ->
				{
					int elements = getEntriesNum();
					
					for (int i = count; ((i > 0) && (elements > 0) && (count <= elements)); i--)
					{
						try
						{
							Element first;
							
							// get <html>
				Element root = textPane.getDocument().getDefaultRootElement();
				// get <body>, and then the first entry in the body.
				first = root.getElement(0);
				
				// remove the first element.
				textPane.getDocument().remove(first.getStartOffset(), first.getEndOffset());
			}
			catch (BadLocationException e)
			{
				e.printStackTrace();
			}
		}
	})	;
	}
	
	private static int getEntriesNum()
	{
		return textPane.getDocument().getDefaultRootElement().getElementCount();
	}
	
	//======================================================================================
	// #endregion Log methods.
	////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Show message to show.
	 *
	 * @param message
	 *            Message.
	 */
	static void showMessage(String message)
	{
		JOptionPane.showMessageDialog(frame, message, "Infomation."
				, JOptionPane.INFORMATION_MESSAGE);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	// #region Getters and setters.
	//======================================================================================
	
	/**
	 * @return the maxEntries
	 */
	public static int getMaxEntries()
	{
		return maxEntries;
	}
	
	/**
	 * Sets the max entries.
	 *
	 * @param maxEntries
	 *            the maxEntries to set
	 */
	public static void setMaxEntries(int maxEntries)
	{
		GUI.maxEntries = maxEntries;
		
		if ( !Logger.initialised || (GUI.maxEntries == maxEntries))
		{
			return;
		}
		
		trimLog();
	}
	
	/**
	 * @return the fontSize
	 */
	public static int getFontSize()
	{
		return fontSize;
	}
	
	/**
	 * Sets the font size.
	 *
	 * @param fontSize
	 *            the fontSize to set
	 */
	public static synchronized void setFontSize(int fontSize)
	{
		GUI.fontSize = fontSize;
		
		if ( !Logger.initialised || (GUI.fontSize == fontSize))
		{
			return;
		}
		
		SwingUtilities.invokeLater(() ->
		{
			AttributeSet attributesCopy;
			
			synchronized (Logger.logAttributesLock)
			{
				MutableAttributeSet attributes = textPane.getInputAttributes();
				attributes.removeAttribute(StyleConstants.FontFamily);
				attributes.removeAttribute(StyleConstants.FontSize);
				attributes.removeAttribute(StyleConstants.Italic);
				attributes.removeAttribute(StyleConstants.Bold);
				attributes.removeAttribute(StyleConstants.Foreground);
				attributes.addAttribute(StyleConstants.FontSize, fontSize);
				
				attributesCopy = attributes.copyAttributes();
			}
			
			textPane.getStyledDocument().setCharacterAttributes(0, textPane.getDocument().getLength()
					, attributesCopy, false);
		});
		
//		// scroll to bottom
//		scroll();
	}
	
	/**
	 * @return the wrap
	 */
	public static boolean isWrap()
	{
		return wrap;
	}
	
	/**
	 * Sets the wrap text in view.
	 *
	 * @param wrap
	 *            the wrap to set
	 */
	public static synchronized void setWrap(boolean wrap)
	{
		if ( !Logger.initialised || (GUI.wrap == wrap))
		{
			return;
		}
		
		GUI.wrap = wrap;
		
		SwingUtilities.invokeLater(() ->
		{
			try
			{
				String oldText = textPane.getText();
				
				synchronized (Logger.logAttributesLock)
				{
					synchronized (Logger.logDocumentWriteLock)
					{
						// recreate the log panel
				initLog();
				
				// relog to new panel
				textPane.setText(oldText);
				
				// move caret to new line at the end (replacing text leaves it at end of last line).
				textPane.getDocument().insertString(textPane.getDocument().getLength(), "\r\n", null);
//
//									// scroll to bottom
//									scroll();
			}
		}
	}
	catch (BadLocationException e)
	{
		e.printStackTrace();
	}
})		;
	}
	
	/**
	 * Sets the wrap var only.
	 *
	 * @param wrap
	 *            the new wrap var only
	 */
	public static void setWrapVarOnly(boolean wrap)
	{
		GUI.wrap = wrap;
	}
	
	/**
	 * Gets the action on close.
	 *
	 * @return the actionOnClose
	 */
	public static int getActionOnClose()
	{
		return actionOnClose;
	}
	
	/**
	 * Sets the action on close.
	 *
	 * @param actionOnClose
	 *            the actionOnClose to set
	 */
	public static void setActionOnClose(int actionOnClose)
	{
		GUI.actionOnClose = actionOnClose;
		setHideOnClose(actionOnClose == WindowConstants.HIDE_ON_CLOSE);
	}
	
	/**
	 * Sets the action on close var only.
	 *
	 * @param actionOnClose
	 *            the new action on close var only
	 */
	public static void setActionOnCloseVarOnly(int actionOnClose)
	{
		GUI.actionOnClose = actionOnClose;
	}
	
	/**
	 * @return the hideOnClose
	 */
	public static boolean isHideOnClose()
	{
		return hideOnClose;
	}
	
	/**
	 * @param hideOnClose
	 *            the hideOnClose to set
	 */
	public static void setHideOnClose(boolean hideOnClose)
	{
		GUI.hideOnClose = hideOnClose;
		
		if (hideOnClose)
		{
			actionOnClose = WindowConstants.HIDE_ON_CLOSE;
		}
		else
		{
			actionOnClose = WindowConstants.EXIT_ON_CLOSE;
		}
		
		if (frame != null)
		{
			frame.setDefaultCloseOperation(actionOnClose);
		}
	}
	
	/**
	 * @return the appIcon
	 */
	public static Image getAppIcon()
	{
		return appIcon;
	}
	
	/**
	 * @param appIcon
	 *            the appIcon to set
	 */
	public static void setAppIcon(Image appIcon)
	{
		GUI.appIcon = appIcon;
		frame.setIconImage(appIcon);
	}
	
	//======================================================================================
	// #endregion Getters and setters.
	////////////////////////////////////////////////////////////////////////////////////////
	
	// static methods class!
	private GUI()
	{}
	
}
