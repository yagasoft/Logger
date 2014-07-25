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


import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;


final class File
{

	/* where the logs will be stored relative to project path. */
	static final Path			LOGS_FOLDER	= Paths.get(System.getProperty("user.dir") + "/var/logs/");

	/** set when the log is accessible and ready. */
	public static boolean		initialised	= false;

	/* current log file path. */
	static Path					logFile;

	/* stream to log file. */
	static OutputStream			stream;

	/* writer to log file. */
	static OutputStreamWriter	writer;

	static void initFile()
	{
		newLogFile();
	}

	/* create a new log file using the current date and time for its name. */
	private static synchronized void newLogFile()
	{
		try
		{
			Files.createDirectories(LOGS_FOLDER);		// make sure the log folder exists

			logFile = Files.createFile(LOGS_FOLDER.resolve(getFileStamp() + ".log"));
			stream = Files.newOutputStream(logFile, StandardOpenOption.APPEND);
			writer = new OutputStreamWriter(stream);
			initialised = true;

			Logger.info("`Created` a `log file` at " + logFile.toAbsolutePath());
		}
		catch (IOException e)
		{	// oops, don't bother with a log file for this session!
			e.printStackTrace();

			writer = null;
			stream = null;
			logFile = null;

			Logger.initialised = false;

			Logger.error("Failed to create log file!");
			Logger.except(e);
		}
	}

	/**
	 * create the file-name time stamp from the system's date and time.
	 *
	 * @return the file stamp
	 */
	public static String getFileStamp()
	{
		return (new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")).format(new Date());
	}

	/* flush this text to log file. */
	static void flush(String text)
	{
		if ( !Logger.initialised)
		{
			return;
		}

		try
		{
			// if a file was created and open ...
			if (writer != null)
			{
				Logger.SLOTS.acquire();
				writer.write(text.replace("\r", ""));
				writer.flush();		// write entry to physical file immediately; to avoid crash corruption.
			}
		}
		catch (IOException | InterruptedException e)
		{
			Logger.initialised = false;

			e.printStackTrace();
			System.err.println("ERROR: Failed to write to log file!");
//			error("failed to write to log file!");
		}
		finally
		{
			Logger.SLOTS.release();
		}
	}

	// static class!
	private File()
	{}
}
