/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Logger/com.yagasoft.logger/File.java
 *
 *			Modified: 24-Jul-2014 (18:43:59)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.logger;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


final class File
{
	
	/* set when the log is accessible and ready. */
	private transient boolean							initialised;
	
	private static File									instance;
	
	/* where the logs will be stored relative to project path. */
	private transient final Path						LOGS_FOLDER	= Paths.get(System.getProperty("user.dir") + "/var/logs/");
	
	/* current log file path. */
	private transient Path								textFile;
	private transient Path								htmlFile;
	
	/* stream to log file. */
	private transient OutputStream						textStream;
	private transient OutputStream						htmlStream;
	
	/* writer to log file. */
	private transient OutputStreamWriter				textWriter;
	private transient OutputStreamWriter				htmlWriter;
	
	private transient boolean							flush;
	private transient boolean							finished;
	
	private transient final LinkedBlockingQueue<String>	textQueue	= new LinkedBlockingQueue<String>(100);
	private transient final LinkedBlockingQueue<String>	htmlQueue	= new LinkedBlockingQueue<String>(100);
	
	// get elapsed time since last physical write to file.
	private transient long								lastFlush	= Calendar.getInstance().getTimeInMillis();
	
	private void initFile()
	{
		if (instance == null)
		{
			newLogFile();
			
			new Thread(() ->
			{
				while (true)
				{
					writeToDisk();
				}
			}).start();
		}
	}
	
	/* create a new log file using the current date and time for its name. */
	private void newLogFile()
	{
		try
		{
			Files.createDirectories(LOGS_FOLDER);		// make sure the log folder exists
			textFile = Files.createFile(LOGS_FOLDER.resolve(getFileStamp() + ".log"));
			textStream = Files.newOutputStream(textFile, StandardOpenOption.APPEND);
			textWriter = new OutputStreamWriter(textStream);
			
			htmlFile = Files.createFile(LOGS_FOLDER.resolve(getFileStamp() + ".html"));
			htmlStream = Files.newOutputStream(htmlFile, StandardOpenOption.APPEND);
			htmlWriter = new OutputStreamWriter(htmlStream);
			htmlWriter.write("<html><body>\n");
			
			initialised = true;
		}
		catch (final IOException e)
		{	// oops, don't bother with a log file for this session!
			e.printStackTrace();
			initialised = false;
		}
	}
	
	/**
	 * create the file-name time stamp from the system's date and time.
	 *
	 * @return the file stamp
	 */
	public String getFileStamp()
	{
		return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());
	}
	
	private void writeToDisk()
	{
		final long currentTime = Calendar.getInstance().getTimeInMillis();		// compare
		
		try
		{
			// if a file was created and open ...
			if ((textWriter != null) && (htmlWriter != null) && initialised)
			{
				// avoid locking here at the start of the application.
				if (Log.getInstance().isInitialised())
				{
					textWriter.write(textQueue.take().replace("\r", ""));
				}
				
				htmlWriter.write(htmlQueue.take().replace("\r", ""));
				
				if (((currentTime - lastFlush) > 5000) || flush)
				{
					flushStreams();
					lastFlush = Calendar.getInstance().getTimeInMillis();		// reset timer
				}
			}
		}
		catch (IOException | InterruptedException e)
		{
			e.printStackTrace();
			initialised = false;
		}
	}
	
	/* flush this text to log file. */
	void queueForWrite(final String text)
	{
		try
		{
			textQueue.put(text);
		}
		catch (final InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	/* flush this text to log file. */
	void writeToHTML(final String text)
	{
		try
		{
			htmlQueue.put(text);
		}
		catch (final InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	// write entries to physical file.
	void flushStreams()
	{
		try
		{
			textWriter.flush();
			textStream.flush();
			
			htmlWriter.flush();
			htmlStream.flush();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}
	
	void shutdown()
	{
		flush = true;
	}
	
	// compress the logs and delete them.
	void finalise()
	{
		initialised = false;
		finished = false;
		
		final byte[] buffer = new byte[1024];
		
		try
		{
			final FileOutputStream outFileStream = new FileOutputStream(textFile.toString().replace(".log", "_log") + ".zip");
			final ZipOutputStream zipOutStream = new ZipOutputStream(outFileStream);
			zipOutStream.setMethod(ZipOutputStream.DEFLATED);
			
			textWriter.flush();
			textStream.flush();
			ZipEntry zipEntry = new ZipEntry(textFile.getFileName().toString());
			zipOutStream.putNextEntry(zipEntry);
			FileInputStream inFileStream = new FileInputStream(textFile.toString());
			
			int len = inFileStream.read(buffer);
			
			while (len > 0)
			{
				zipOutStream.write(buffer, 0, len);
				len = inFileStream.read(buffer);
			}
			
			inFileStream.close();
			
			htmlWriter.write("\n</html></body>");
			htmlWriter.flush();
			htmlStream.flush();
			zipEntry = new ZipEntry(htmlFile.getFileName().toString());
			zipOutStream.putNextEntry(zipEntry);
			inFileStream = new FileInputStream(htmlFile.toString());
			
			len = inFileStream.read(buffer);
			
			while (len > 0)
			{
				zipOutStream.write(buffer, 0, len);
				len = inFileStream.read(buffer);
			}
			
			inFileStream.close();
			zipOutStream.closeEntry();
			zipOutStream.close();
			
			textWriter.close();
			textStream.close();
			Files.deleteIfExists(textFile);
			
			htmlWriter.close();
			htmlStream.close();
			Files.deleteIfExists(htmlFile);
		}
		catch (final IOException ex)
		{
			ex.printStackTrace();
		}
		
		finished = true;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	// #region Getters and setters.
	//======================================================================================
	
	/**
	 * @return the htmlFile
	 */
	public Path getHtmlFile()
	{
		return htmlFile;
	}
	
	/**
	 * @param htmlFile
	 *            the htmlFile to set
	 */
	public void setHtmlFile(final Path htmlFile)
	{
		this.htmlFile = htmlFile;
	}
	
	/**
	 * @return the textFile
	 */
	public Path getTextFile()
	{
		return textFile;
	}
	
	/**
	 * @param textFile
	 *            the textFile to set
	 */
	public void setTextFile(final Path textFile)
	{
		this.textFile = textFile;
	}
	
	/**
	 * @return the finished
	 */
	public boolean isFinished()
	{
		return finished;
	}
	
	/**
	 * @param finished
	 *            the finished to set
	 */
	public void setFinished(final boolean finished)
	{
		this.finished = finished;
	}
	
	//======================================================================================
	// #endregion Getters and setters.
	////////////////////////////////////////////////////////////////////////////////////////
	
	static File getInstance()
	{
		synchronized (File.class)
		{
			if (instance == null)
			{
				instance = new File();
			}
			
			return instance;
		}
	}
	
	// Singleton!
	private File()
	{
		initFile();
	}
}
