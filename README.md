Logger v5.02.090
================

A simple and stylised GUI logger that saves its entries to a log file in real time.

### Features:

  + Creates a log file in the app folder automatically
  + Flush entries to the file automatically
  + Key text is stylised
  + Wrap words in '`' character to colour them
	+ Post sequence of coloured strings, whether passed separate or in the same string.
  + Parses exceptions
  + Export log to HTML or text file
  + Auto scrolling stops when manually scrolling, and resumes when scrolled to bottom again
  + Options:
	+ Toggle text wrapping
	+ Adjust text size
	+ Limit number of entries (only visually -- full log saved to disk)
	+ Toggle 'hide on close (x)', which minimised to tray on supported platforms
	+ All options are persistent

### Defaults:

  + Logs are stored in '[program_folder]/var/logs/'
  + Auto log file-names are a time-stamp of when the log started
  + Auto log files are in plain text
  + Wrapping is off
  + Max entries are 500
  + Hide on close is on

### Notes:

  + Versioning is based on API compatibility, so v9.01.245 of this library can work with v9.12.985, but v8.58.158 can not work with v9.12.985 and vice versa. This essentially means that if the major version changes, you have to revise all dependencies for this library and any that depend on it as well.


<br>
<br>
**Copyright &copy; 2011-2014 by Ahmed el-Sawalhy**
 * The Modified MIT License (GPL v3 compatible).
