Logger v4.01.015
================

A simple and stylised GUI logger that saves its entries to a log file in real time.

### Features:

  + Creates a log file in the app folder automatically.
  + Flush entries to the file automatically.
  + Key text is stylised.
  + Wrap words in '`' character to colour them.
  + Parses exceptions.

### Defaults:

  + Lines don't wrap.
  + Log window is hidden.
  + The 'x' button only HIDES the window, so please makes sure you exit the program some other way.
  + Logs are stored in '[program_folder]/var/logs/'.
  + Log files' names are a time-stamp of when the log started.
  + Log files are in plain text.

### Notes:

  + Versioning is based on API compatibility, so v9.01.245 of this library can work with v9.12.985, but v8.58.158 can not work with v9.12.985 and vice versa. This essentially means that if the major version changes, you have to revise all dependencies for this library and any that depend on it as well.


<br>
<br>
**Copyright &copy; 2011-2014 by Ahmed el-Sawalhy**
 * The Modified MIT License (GPL v3 compatible).
