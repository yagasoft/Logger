Logger v6.03.235
================

A simple and stylised GUI logger that saves its entries to a log file in real time.

### Features:

  + Creates a log file in the app folder automatically
	+ Flushes entries periodically to the file
	+ On shutdown, text and HTML files are compressed and originals deleted
  + Export log to HTML or text file
  + Key text is stylised
  + Wrap words in '`' character to colour them
	+ Post sequence of coloured strings, whether passed separate or in the same string
  + Parses exceptions
  + Auto scrolling stops when manually scrolling, and resumes when scrolled to bottom again
  + Options:
	+ Toggle text wrapping
	+ Adjust text size (range: 10 to 25).
	+ Limit number of entries -- only visually, the full log is saved to disk
	+ Toggle 'hide on close (x)', which minimises to tray on supported platforms
	+ Capture the console output and error streams from the currently running application
	+ Show only errors added to the log
	+ All options are persistent

### Defaults:

  + Logs are stored in '[program_folder]/var/logs/'
	+ Auto log file-names are a time-stamp of when the log started
	+ Auto log files are in plain text
  + Wrapping is off
  + Max entries are 500. It's better not to go too high or else it will REALLY eat up the memory!
  + Hide on close is on
	+ Make sure to check the thread in memory if your OS doesn't support tray icons please
	+ This is so as not to close the program associated with the logger, unless explicitly specified
  + Console capture is on. As it's fowarded to the normal console, so this is safe
  + Show only errors is off

### Notes:

  + Versioning is based on API compatibility, so v9.01.245 of this library can work with v9.12.985, but v8.58.158 can not work with v9.12.985 and vice versa. This essentially means that if the major version changes, you have to revise all dependencies for this library and any that depend on it as well.


<br>
<br>
**Copyright &copy; 2011-2014 by Ahmed el-Sawalhy**
 * The Modified MIT License (GPL v3 compatible).
