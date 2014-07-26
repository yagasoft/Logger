/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Logger/com.yagasoft.logger/GUI.java
 *
 *			Modified: 26-Jul-2014 (15:30:50)
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
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.InlineView;

import com.yagasoft.logger.menu.MenuBar;


/**
 * The Class GUI.
 */
public final class GUI
{
	
	/** set when the log is accessible and ready. */
	public static boolean								initialised		= false;
	
	/** Frame. */
	public static JFrame								frame;
	
	private static boolean								visible			= false;
	private static int									actionOnClose	= WindowConstants.HIDE_ON_CLOSE;
	
	private static JPanel								contentPane;
	private static MenuBar								menuBar;
	static JScrollPane									scroller;
	static JTextPane									textPane;
	private static DefaultCaret							caret;
	
	/* Wrap text in view. */
	private static boolean								wrap			= false;
	
	private static SystemTray							systemTray;
	private static TrayIcon								trayIcon;
	private static Image								appIcon			= new ImageIcon(Logger.class
																				.getResource("images/icon.png")).getImage();
	
	private static LinkedBlockingQueue<String>			textQueue		= new LinkedBlockingQueue<String>();
	private static LinkedBlockingQueue<AttributeSet>	attributeQueue	= new LinkedBlockingQueue<AttributeSet>();
	
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
		new Thread(() ->
		{
			while (true)
			{
				writeToLog();
			}
		}).start();
		
		initialised = true;
	}
	
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
	
	static synchronized void hideLogger()
	{
		if ( !initialised || !visible || (actionOnClose == WindowConstants.EXIT_ON_CLOSE))
		{
			return;
		}
		
		if (minimiseToTray())
		{
			visible = false;
			frame.setVisible(visible);
		}
	}
	
	/* Inits the frame. */
	private static synchronized void initFrame()
	{
		frame = new JFrame("Logger window");
		frame.setDefaultCloseOperation(getActionOnClose());
		frame.setBounds(25, 25, 800, 512);
		frame.setVisible(visible);
		frame.setIconImage(appIcon);
		
		// save options and close DB when application is closing.
		frame.addWindowListener(new WindowAdapter()
		{
			
			@Override
			public void windowIconified(WindowEvent e)
			{
				super.windowIconified(e);
				hideLogger();
			}
			
			@Override
			public void windowClosing(WindowEvent e)
			{
				super.windowClosing(e);
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
		
		frame.revalidate();
	}
	
	/* Inits the log text area. */
	private static synchronized void initLog()
	{
		if (scroller != null)
		{
			contentPane.remove(scroller);
		}
		
		textPane = new JTextPane();
		textPane.setEditable(false);
		textPane.setEditorKit(new WrapControlledEditorKit(wrap));
		caret = (DefaultCaret) textPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		
		scroller = new JScrollPane(textPane);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		contentPane.add(scroller, BorderLayout.CENTER);
		frame.revalidate();
	}
	
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
		try
		{
			String text = textQueue.take();
			AttributeSet attributes = attributeQueue.take();
			
			if (textPane != null)
			{
				try
				{
					Rectangle visible = textPane.getVisibleRect();	// get visible rectangle of the log area
					Rectangle bounds = textPane.getBounds();	// get the size of the log area.
					
					Logger.SLOTS.acquire();
					
					// add text to log area
					textPane.getDocument().insertString(textPane.getDocument().getLength(), text, attributes);
					
					if ((bounds.height - (visible.height + visible.y)) < 50)
					{
						// if the visible rectangle is not at the bottom, stop auto scrolling
						if (scroller != null)
						{
							scroller.getVerticalScrollBar()
									.setValue(scroller.getVerticalScrollBar().getMaximum());
						}
					}
				}
				catch (BadLocationException | InterruptedException e)
				{
					Logger.except(e);
					e.printStackTrace();
				}
				finally
				{
					Logger.SLOTS.release();
				}
			}
			
			Logger.addToHistory(text, attributes);
			File.writeToFile(text);		// save to disk log file
			
			Logger.trimLog();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Removes the entries.
	 *
	 * @param count
	 *            Count.
	 */
	public static void removeEntries(int count)
	{
		for (int i = count; ((i > 0) && (textPane.getDocument().getDefaultRootElement().getElement(0).getElementCount() > 0)); i--)
		{
			try
			{
				Logger.SLOTS.acquire();
				
				// get <html>
				Element root = textPane.getDocument().getDefaultRootElement();
				// get <body>, and then the first entry in the body.
				Element first = root.getElement(0).getElement(0);
				
				// remove the first element.
				textPane.getDocument().remove(first.getStartOffset(), first.getEndOffset());
			}
			catch (BadLocationException | InterruptedException e)
			{
				e.printStackTrace();
			}
			finally
			{
				Logger.SLOTS.release();
			}
		}
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
				GUI.setMainWindowFocusable(true);
			}
			
		});
		
		// add the passed panel to the frame.
		newFrame.add(panel);
		// show the frame.
		newFrame.setVisible(true);
		// fit the frame to panel.
		newFrame.pack();
		
		// disable the main window.
		GUI.setMainWindowFocusable(false);
		
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
	
	////////////////////////////////////////////////////////////////////////////////////////
	// #region Getters and setters.
	//======================================================================================
	
	/**
	 * Checks if is wrap text in view.
	 *
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
		if ( !initialised || (GUI.wrap == wrap))
		{
			return;
		}
		
		GUI.wrap = wrap;
		
		try
		{
			Logger.SLOTS.acquire();
			
			String oldText = GUI.textPane.getText().replace("\t", "&#9;");
			// recreate the log panel
			initLog();
			
			// relog to new panel
			textPane.setText(oldText);
			
			// move caret to new line at the end (replacing text leaves it at end of last line).
			textPane.getDocument().insertString(textPane.getDocument().getLength(), "\r\n", null);
			
			// scroll to bottom
			if (GUI.scroller != null)
			{
				GUI.frame.revalidate();
				GUI.scroller.getVerticalScrollBar().setValue(GUI.scroller.getVerticalScrollBar().getMaximum());
			}
		}
		catch (BadLocationException | InterruptedException e)
		{
			e.printStackTrace();
		}
		finally
		{
			Logger.SLOTS.release();
		}
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
	public static synchronized void setActionOnClose(int actionOnClose)
	{
		GUI.actionOnClose = actionOnClose;
		GUI.frame.setDefaultCloseOperation(actionOnClose);
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
	
	//======================================================================================
	// #endregion Getters and setters.
	////////////////////////////////////////////////////////////////////////////////////////
	
	// static class!
	private GUI()
	{}
	
}


class WrapControlledEditorKit extends HTMLEditorKit
{
	
	private static final long	serialVersionUID	= -1541542932309843548L;
	private boolean				wrap				= false;
	
	public WrapControlledEditorKit(boolean wrap)
	{
		this.wrap = wrap;
	}
	
	@Override
	public ViewFactory getViewFactory()
	{
		return new WrapControlledViewFactory();
	}
	
	/**
	 * Checks if is wrap.
	 *
	 * @return the wrap
	 */
	public boolean isWrap()
	{
		return wrap;
	}
	
	/**
	 * Sets the wrap.
	 *
	 * @param wrap
	 *            the wrap to set
	 */
	public void setWrap(boolean wrap)
	{
		this.wrap = wrap;
	}
	
	class WrapControlledViewFactory extends HTMLEditorKit.HTMLFactory
	{
		
		@Override
		public View create(Element elem)
		{
			Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
			
			if (o instanceof HTML.Tag)
			{
				HTML.Tag kind = (HTML.Tag) o;
				if (kind == HTML.Tag.CONTENT)
				{
					if ( !wrap)
					{
						return new NoWrapBoxView(elem);
					}
					
					return new InlineView(elem);
				}
			}
			
			return super.create(elem);
		}
	}
	
	static class NoWrapBoxView extends InlineView
	{
		
		public NoWrapBoxView(Element elem)
		{
			super(elem);
		}
		
		@Override
		public int getBreakWeight(int axis, float pos, float len)
		{
			return BadBreakWeight;
		}
		
	}
	
}
