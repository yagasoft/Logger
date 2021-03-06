/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Logger/com.yagasoft.logger/GUI.java
 *
 *			Modified: 28-Jul-2014 (14:51:28)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.logger;


import java.awt.AWTException;
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
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;

import com.yagasoft.logger.menu.MenuBar;
import com.yagasoft.logger.menu.panels.option.Options;


/**
 * The Class
 */
public final class GUI
{
	
	/** set when the log is accessible and ready. */
	private transient boolean									initialised;
	
	private static GUI											instance;
	
	/** Frame. */
	private JFrame												frame;
	private String												title					= "Logger";
	
	private transient boolean									visible;
	private transient int										actionOnClose;
	
	private transient JPanel									contentPane;
	private transient JTextPane									textPane;
	private transient DefaultCaret								caret;
	
	private transient boolean									autoScroll				= true;
	private transient boolean									holdingBar;
	private final static int									SCROLL_CLEARANCE		= 5;
	
	private transient SystemTray								systemTray;
	private transient TrayIcon									trayIcon;
	private Image												appIcon					= new ImageIcon(GUI.class
																								.getResource("images/icon.png"))
																								.getImage();
	
	// queue used to receive text sent.
	private final transient LinkedBlockingQueue<String>			textQueue				= new LinkedBlockingQueue<String>(10);
	private final transient LinkedBlockingQueue<AttributeSet>	attributeQueue			= new LinkedBlockingQueue<AttributeSet>(
																								10);
	private final transient LinkedBlockingQueue<Boolean>		saveOnlyQueue			= new LinkedBlockingQueue<Boolean>(10);
	
	// queue used to cache text sent. It's used temporarily store text until a '\n' is encountered to flush to log.
	private final transient LinkedBlockingQueue<String>			secondTextQueue			= new LinkedBlockingQueue<String>(100);
	private final transient LinkedBlockingQueue<AttributeSet>	secondAttributeQueue	= new LinkedBlockingQueue<AttributeSet>(
																								100);
	private final transient LinkedBlockingQueue<Boolean>		secondSaveOnlyQueue		= new LinkedBlockingQueue<Boolean>(100);
	
	// used for preventing write to log when scrolling manually.
	private transient CountDownLatch							latch					= new CountDownLatch(0);
	
	private transient MouseAdapter								mouseScrollerListener;
	
	private final Object										logAttributesLock		= new Object();
	private final transient Object								syncObject				= new Object();
	
	////////////////////////////////////////////////////////////////////////////////////////
	// #region Initialisation.
	//======================================================================================
	
	/**
	 * Inits the logger.
	 */
	private void initGUI()
	{
		if (instance == null)
		{
			initFrame();
			initPanel();
			
			mouseScrollerListener = new MouseAdapter()
			{
				
				Rectangle	visible;
				Rectangle	bounds;
				
				@Override
				public void mousePressed(final MouseEvent event)
				{
					super.mousePressed(event);
					
					// prevent writing.
					latch = new CountDownLatch(1);
					
					holdingBar = true;
					autoScroll = false;
				}
				
				@Override
				public void mouseReleased(final MouseEvent event)
				{
					super.mouseReleased(event);
					
					synchronized (logAttributesLock)
					{
						visible = textPane.getVisibleRect();	// get visible rectangle of the log area
						bounds = textPane.getBounds();	// get the size of the log area.
					}
					
					// if the visible rectangle is not at the bottom, stop auto scrolling
					if ((bounds.height - (visible.height + visible.y)) <= SCROLL_CLEARANCE)
					{
						autoScroll = true;
					}
					
					holdingBar = false;
					
					// allow writing.
					latch.countDown();
				}
			};
			
			initLog();
			initTray();
			
			// log writing thread.
			new Thread(() ->
			{
				while (true)
				{
					writeToLog();
				}
			}).start();
		}
		
		initialised = true;
	}
	
	/* Inits the frame. */
	private void initFrame()
	{
		frame = new JFrame("Logger window");
		frame.setDefaultCloseOperation(actionOnClose);
		frame.setBounds(25, 25, 800, 512);
		frame.setVisible(true);
		visible = true;
		frame.setIconImage(appIcon);
		
		// save options and close DB when application is closing.
		frame.addWindowListener(new WindowAdapter()
		{
			
			@Override
			public void windowIconified(final WindowEvent event)
			{
				super.windowIconified(event);
				
				if (SystemTray.isSupported() && Options.getInstance().isHideOnClose())
				{
					hideLogger();
				}
			}
			
			@Override
			public void windowClosing(final WindowEvent e)
			{
				super.windowClosing(e);
				hideLogger();
			}
		});
	}
	
	/* Inits the panel. */
	private void initPanel()
	{
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		
		frame.setContentPane(contentPane);
		
		contentPane.add(new MenuBar(), BorderLayout.NORTH);
	}
	
	/* Inits the log text area. */
	private void initLog()
	{
		if (Options.getInstance().isWrap())
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
				
				private static final long	serialVersionUID	= 7134437176140763527L;
				
				@Override
				public boolean getScrollableTracksViewportWidth() // NOPMD by Ahmed on 31/07/14 11:01
				{
					return getUI().getPreferredSize(this).width <= getParent().getSize().width;
				}
			};
		}
		
		textPane.setEditable(false);
		caret = (DefaultCaret) textPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		
		final JScrollPane scroller = new JScrollPane(textPane);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.getVerticalScrollBar().addMouseListener(mouseScrollerListener);
		
		contentPane.add(scroller, BorderLayout.CENTER);
		frame.revalidate();
	}
	
	//======================================================================================
	// #endregion Initialisation.
	////////////////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////////////////
	// #region Window control.
	//======================================================================================
	
	void showLogger()
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
	 * {@link Options#isHideOnClose()} is set to true.
	 */
	public void hideLogger()
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
	public JFrame showSubWindow(final JPanel panel, final String title)
	{
		// create a frame for the panel.
		final JFrame newFrame = new JFrame(title);
		
		if (initialised)
		{
			// open the frame relative to the main window.
			final Point mainWindowLocation = frame.getLocation();
			newFrame.setLocation((int) mainWindowLocation.getX() + 50, (int) mainWindowLocation.getY() + 50);
			
			// when the frame is closed, dispose of it and return focus to the main window.
			newFrame.addWindowListener(new WindowAdapter()
			{
				
				@Override
				public void windowClosing(final WindowEvent event)
				{
					super.windowClosing(event);
					
					newFrame.dispose();
					setMainWindowFocusable(true);
				}
				
			});
			
			// add the passed panel to the frame.
			newFrame.add(panel);
			// show the frame.
			newFrame.setVisible(true);
			// size to contents
			newFrame.pack();
			
			// disable the main window.
			setMainWindowFocusable(false);
		}
		
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
	void setMainWindowFocusable(final boolean focusable)
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
	
	private void initTray()
	{
		if ( !SystemTray.isSupported())
		{
			return;
		}
		
		systemTray = SystemTray.getSystemTray();
		
		final PopupMenu trayMenu = new PopupMenu();
		MenuItem menuItem;
		
		menuItem = new MenuItem("Restore");
		menuItem.addActionListener(event -> showLogger());
		trayMenu.add(menuItem);
		
		menuItem = new MenuItem("Exit");
		menuItem.addActionListener(event ->
		{
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
		});
		trayMenu.add(menuItem);
		
		trayIcon = new TrayIcon(appIcon, title, trayMenu);
		
		trayIcon.addMouseListener(new MouseAdapter()
		{
			
			@Override
			public void mouseClicked(final MouseEvent event)
			{
				super.mouseClicked(event);
				
				if (event.getClickCount() >= 2)
				{
					showLogger();
				}
			}
		});
	}
	
	/*
	 * Minimises the app to the system tray.
	 */
	private boolean minimiseToTray()
	{
		boolean minimised = false;
		
		if (visible && SystemTray.isSupported())
		{
			try
			{
				systemTray.add(trayIcon);
				minimised = true;
			}
			catch (final AWTException e)
			{
				e.printStackTrace();
				minimised = false;
			}
		}
		
		return minimised;
	}
	
	/*
	 * Restores the app from the system tray, and brings it to the front.
	 */
	private void restoreWindow()
	{
		frame.setExtendedState(Frame.NORMAL);
		frame.toFront();
		
		if ((systemTray != null) && Arrays.stream(systemTray.getTrayIcons()).anyMatch(icon -> icon.equals(trayIcon)))
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
	
	void append(final String text, final AttributeSet attributes)
	{
		append(text, attributes, false);
	}
	
	/* append text to the log as is using the style passed, then write it to log file. */
	void append(final String text, final AttributeSet attributes, final boolean saveOnly)
	{
		try
		{
			textQueue.put(text);
			attributeQueue.put(attributes);
			saveOnlyQueue.put(saveOnly);
		}
		catch (final InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	private void writeToLog()
	{
		if ( !Log.getInstance().isInitialised())
		{
			return;
		}
		
		try
		{
			String text = textQueue.take();
			AttributeSet attributes = attributeQueue.take();
			boolean saveOnly = saveOnlyQueue.take();
			
			latch.await();
			
			if (textPane != null)
			{
				secondTextQueue.add(text);
				secondAttributeQueue.add(attributes);
				secondSaveOnlyQueue.add(saveOnly);
				
				// check whether a full line is ready for flush.
				if (text.contains("\n") || (secondTextQueue.remainingCapacity() <= 1))
				{
					// flush to log.
					while ( !secondTextQueue.isEmpty())
					{
						text = secondTextQueue.poll();
						attributes = secondAttributeQueue.poll();
						saveOnly = secondSaveOnlyQueue.poll();
						
						Log.getInstance().addToHistory(text, attributes);
						
						if ( !saveOnly)
						{
							synchronized (logAttributesLock)
							{
								// add text to log area
								textPane.getInputAttributes().removeAttributes(textPane.getInputAttributes());
								textPane.setCharacterAttributes(textPane.getInputAttributes(), true);
								textPane.setParagraphAttributes(textPane.getInputAttributes(), true);
								
								synchronized (syncObject)
								{
									textPane.getDocument().insertString(textPane.getDocument().getLength(), text,
											attributes);
								}
							}
						}
						
						File.getInstance().queueForWrite(text);		// save to disk log file
					}
					
					// scroll to bottom if was already at the bottom.
					if ( !holdingBar && autoScroll && (text != null) && !saveOnly)
					{
						trimLog();
						
						synchronized (logAttributesLock)
						{
							textPane.getInputAttributes().removeAttributes(textPane.getInputAttributes());
							textPane.setCharacterAttributes(textPane.getInputAttributes(), true);
							textPane.setParagraphAttributes(textPane.getInputAttributes(), true);
							
							caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
							
							synchronized (syncObject)
							{
								textPane.getDocument().insertString(textPane.getDocument().getLength(), "\r",
										attributes);
							}
							
							caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
							textPane.setCaretPosition(textPane.getDocument().getLength());
						}
					}
				}
			}
		}
		catch (final BadLocationException | InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	int countOverLimit(final int limit)
	{
		return Math.max(getEntriesNum() - limit, 0);
	}
	
	/** reduce the max entries to be within the limit */
	public void trimLog()
	{
		synchronized (syncObject)
		{
			// remove extra
			removeEntries(countOverLimit(Options.getInstance().getNumberOfEntries()));
		}
	}
	
	/**
	 * Clear log.
	 */
	public void clearLog()
	{
		if ( !Log.getInstance().isInitialised())
		{
			return;
		}
		
		synchronized (syncObject)
		{
			removeEntries(countOverLimit(1));
		}
	}
	
	/**
	 * Removes the entries.
	 *
	 * @param count
	 *            Count.
	 */
	private void removeEntries(final int count)
	{
		if ( !Log.getInstance().isInitialised())
		{
			return;
		}
		
		final int elements = getEntriesNum();
		
		Element root;
		Element first;
		
		for (int i = count; ((i > 0) && (elements > 0) && (count <= elements)); i--)
		{
			try
			{
				root = textPane.getDocument().getDefaultRootElement();
				first = root.getElement(0);
				textPane.getDocument().remove(first.getStartOffset(), first.getEndOffset());
			}
			catch (final BadLocationException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private int getEntriesNum()
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
	void showMessage(final String message)
	{
		JOptionPane.showMessageDialog(frame, message, "Infomation."
				, JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Sets the font size.
	 *
	 * @param fontSize
	 *            the fontSize to set
	 */
	public void setFontSize(final int fontSize)
	{
		synchronized (logAttributesLock)
		{
			final MutableAttributeSet attributes = textPane.getInputAttributes();
			attributes.removeAttribute(StyleConstants.FontFamily);
			attributes.removeAttribute(StyleConstants.FontSize);
			attributes.removeAttribute(StyleConstants.Italic);
			attributes.removeAttribute(StyleConstants.Bold);
			attributes.removeAttribute(StyleConstants.Foreground);
			attributes.addAttribute(StyleConstants.FontSize, fontSize);
			
			synchronized (syncObject)
			{
				textPane.getStyledDocument().setCharacterAttributes(0, textPane.getDocument().getLength()
						, attributes, false);
			}
		}
	}
	
	/**
	 * Sets the wrap text in view.
	 *
	 * @param wrap
	 *            the wrap to set
	 */
	public void setWrap(final boolean wrap)
	{
		try
		{
			synchronized (logAttributesLock)
			{
				synchronized (syncObject)
				{
					final String oldText = textPane.getText();
					
					textPane.setText("");
					
					// recreate the log panel
					initLog();
					
					// relog to new panel
					textPane.setText(oldText);
					
					// move caret to new line at the end (replacing text leaves it at end of last line).
					textPane.getDocument().insertString(textPane.getDocument().getLength(), "\r\n", null);
				}
			}
		}
		catch (final BadLocationException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the action on close.
	 *
	 * @param actionOnClose
	 *            the actionOnClose to set
	 */
	public void setActionOnClose(final int actionOnClose)
	{
		setHideOnClose(actionOnClose == WindowConstants.HIDE_ON_CLOSE);
	}
	
	/**
	 * @param hideOnClose
	 *            the hideOnClose to set
	 */
	public void setHideOnClose(final boolean hideOnClose)
	{
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
	public Image getAppIcon()
	{
		return appIcon;
	}
	
	/**
	 * @param appIcon
	 *            the appIcon to set
	 */
	public void setAppIcon(final Image appIcon)
	{
		this.appIcon = appIcon;
		frame.setIconImage(appIcon);
		initTray();
	}
	
	/**
	 * @return the textPane
	 */
	public JTextPane getTextPane()
	{
		return textPane;
	}
	
	/**
	 * @param textPane
	 *            the textPane to set
	 */
	public void setTextPane(final JTextPane textPane)
	{
		this.textPane = textPane;
	}
	
	/**
	 * @return the frame
	 */
	public JFrame getFrame()
	{
		return frame;
	}
	
	/**
	 * @param frame
	 *            the frame to set
	 */
	public void setFrame(final JFrame frame)
	{
		this.frame = frame;
	}
	
	/**
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}
	
	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(final String title)
	{
		this.title = title;
		frame.setTitle(title);
		initTray();
	}
	
	/**
	 * Gets the log attributes lock.
	 *
	 * @return the log attributes lock
	 */
	public Object getLogAttributesLock()
	{
		return logAttributesLock;
	}
	
	//======================================================================================
	// #endregion Getters and setters.
	////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Gets the single instance of GUI.
	 *
	 * @return single instance of GUI
	 */
	public static GUI getInstance()
	{
		synchronized (GUI.class)
		{
			if (instance == null)
			{
				instance = new GUI();
			}
			
			return instance;
		}
	}
	
	// Singleton!
	private GUI()
	{
		initGUI();
	}
	
}
