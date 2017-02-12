package com.cylee.androidlib.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * ClassName:Log
 *
 * @author Jerome Song
 * @Date 2014-5-31 下午9:23:29
 * @see android.util.Log 的封装，可以根据Level控制log的输出。<br>
 * 默认级别VERBOSE,打印所有,建议用Log.d Log.i<br>
 * 软件发布时调整级别为SUPPRESS
 */
public class Log {
  /**
   * Priority constant for the println method; use Log.v.
   */
  public static final int VERBOSE = android.util.Log.VERBOSE;

  /**
   * Priority constant for the println method; use Log.d.
   */
  public static final int DEBUG = android.util.Log.DEBUG;

  /**
   * Priority constant for the println method; use Log.i.
   */
  public static final int INFO = android.util.Log.INFO;

  /**
   * Priority constant for the println method; use Log.w.
   */
  public static final int WARN = android.util.Log.WARN;

  /**
   * Priority constant for the println method; use Log.e.
   */
  public static final int ERROR = android.util.Log.ERROR;

  /**
   * Priority constant for the println method.
   */
  public static final int ASSERT = android.util.Log.ASSERT;

  public static final int OFF = ASSERT + 1;

  protected static int LOG_LEVEL = VERBOSE;

  public static final String BIGLOG_FILE = "sdcard/L/log/api-log-%s.txt";
  private static final String BIGLOG_TAG = "BIGLOG";
  private static boolean appendLogFile = false;

  public Log() {
  }

  /**
   * setLogLevel 设置log输出级别
   */
  public static void setLogLevel(int log_level) {
    LOG_LEVEL = log_level;
  }

  public static int v(String msg) {
    return v(defaultTag(), msg);
  }

  /**
   * Send a {@link #VERBOSE} log message.
   *
   * @param tag Used to identify the source of a log message. It usually
   * identifies the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  public static int v(String tag, String msg) {
    if (LOG_LEVEL <= VERBOSE) {
      return android.util.Log.v(tag, msg);
    }
    return 0;
  }

  /**
   * Send a {@link #VERBOSE} log message and log the exception.
   *
   * @param tag Used to identify the source of a log message. It usually
   * identifies the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   * @param tr An exception to log
   */
  public static int v(String tag, String msg, Throwable tr) {
    if (LOG_LEVEL <= VERBOSE) {
      return android.util.Log.v(tag, msg, tr);
    }
    return 0;
  }

  public static int d(String msg) {
    return d(defaultTag(), msg);
  }

  /**
   * Send a {@link #DEBUG} log message.
   *
   * @param tag Used to identify the source of a log message. It usually
   * identifies the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  public static int d(String tag, String msg) {
    if (LOG_LEVEL <= DEBUG) {
      return android.util.Log.d(tag, msg);
    }
    return 0;
  }

  /**
   * Send a {@link #DEBUG} log message and log the exception.
   *
   * @param tag Used to identify the source of a log message. It usually
   * identifies the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   * @param tr An exception to log
   */
  public static int d(String tag, String msg, Throwable tr) {
    if (LOG_LEVEL <= DEBUG) {
      return android.util.Log.d(tag, msg, tr);
    }
    return 0;
  }

  public static int i(String msg) {
    return i(defaultTag(), msg);
  }

  /**
   * Send an {@link #INFO} log message.
   *
   * @param tag Used to identify the source of a log message. It usually
   * identifies the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  public static int i(String tag, String msg) {
    if (LOG_LEVEL <= INFO) {
      return android.util.Log.i(tag, msg);
    }
    return 0;
  }

  /**
   * Send a {@link #INFO} log message and log the exception.
   *
   * @param tag Used to identify the source of a log message. It usually
   * identifies the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   * @param tr An exception to log
   */
  public static int i(String tag, String msg, Throwable tr) {
    if (LOG_LEVEL <= INFO) {
      return android.util.Log.i(tag, msg, tr);
    }
    return 0;
  }

  public static int w(String msg) {
    return w(defaultTag(), msg);
  }

  /**
   * Send a {@link #WARN} log message.
   *
   * @param tag Used to identify the source of a log message. It usually
   * identifies the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  public static int w(String tag, String msg) {
    if (LOG_LEVEL <= WARN) {
      return android.util.Log.w(tag, msg);
    }
    return 0;
  }

  /**
   * Send a {@link #WARN} log message and log the exception.
   *
   * @param tag Used to identify the source of a log message. It usually
   * identifies the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   * @param tr An exception to log
   */
  public static int w(String tag, String msg, Throwable tr) {
    if (LOG_LEVEL <= WARN) {
      return android.util.Log.w(tag, msg, tr);
    }
    return 0;
  }

  /**
   * Checks to see whether or not a log for the specified tag is loggable at
   * the specified level.
   *
   * The default level of any tag is set to INFO. This means that any level
   * above and including INFO will be logged. Before you make any calls to a
   * logging method you should check to see if your tag should be logged. You
   * can change the default level by setting a system property: 'setprop
   * log.tag.&lt;YOUR_LOG_TAG> &lt;LEVEL>' Where level is either VERBOSE,
   * DEBUG, INFO, WARN, ERROR, ASSERT, or SUPPRESS. SUPRESS will turn off all
   * logging for your tag. You can also create a local.prop file that with the
   * following in it: 'log.tag.&lt;YOUR_LOG_TAG>=&lt;LEVEL>' and place that in
   * /data/local.prop.
   *
   * @param tag The tag to check.
   * @param level The level to check.
   * @return Whether or not that this is allowed to be logged.
   * @throws IllegalArgumentException is thrown if the tag.length() > 23.
   */
  public static boolean isLoggable(String tag, int level) {
    return android.util.Log.isLoggable(tag, level);
  }

  /**
   * Send a {@link #WARN} log message and log the exception.
   *
   * @param tag Used to identify the source of a log message. It usually
   * identifies the class or activity where the log call occurs.
   * @param tr An exception to log
   */
  public static int w(String tag, Throwable tr) {
    if (LOG_LEVEL <= WARN) {
      return android.util.Log.w(tag, tr);
    }
    return 0;
  }


  public static int e(String msg) {
    return e(defaultTag(), msg);
  }

  /**
   * Send an {@link #ERROR} log message.
   *
   * @param tag Used to identify the source of a log message. It usually
   * identifies the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  public static int e(String tag, String msg) {
    if (LOG_LEVEL <= ERROR) {
      return android.util.Log.e(tag, msg);
    }
    return 0;
  }

  /**
   * Send a {@link #ERROR} log message and log the exception.
   *
   * @param tag Used to identify the source of a log message. It usually
   * identifies the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   * @param tr An exception to log
   */
  public static int e(String tag, String msg, Throwable tr) {
    if (LOG_LEVEL <= ERROR) {
      return android.util.Log.e(tag, msg, tr);
    }
    return 0;
  }

  /**
   * Handy function to get a loggable stack trace from a Throwable
   *
   * @param tr An exception to log
   */
  public static String getStackTraceString(Throwable tr) {
    return android.util.Log.getStackTraceString(tr);
  }

  /**
   * Low-level logging call.
   *
   * @param priority The priority/type of this log message
   * @param tag Used to identify the source of a log message. It usually
   * identifies the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   * @return The number of bytes written.
   */
  public static int println(int priority, String tag, String msg) {
    return android.util.Log.println(priority, tag, msg);
  }

  public static void BIGLOG(String text) {
    if (LOG_LEVEL<OFF) {
      String filename = String.format(BIGLOG_FILE, TimeUtils.format("MM-dd", new Date()));
      File logFile = new File(filename);
      if (!logFile.getParentFile().exists()) {
        logFile.getParentFile().mkdirs();
      }
      String time = TimeUtils.format("MM-dd:HH:mm:ss", new Date());
      if (!logFile.exists()) {
        try {
          d(BIGLOG_TAG, "log file not exist");
          logFile.createNewFile();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      appendLogFile = true;
      try {
        BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, appendLogFile));
        buf.append(time + "=>" + text);
        buf.newLine();
        buf.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      d(BIGLOG_TAG, text);
    }
  }

  public static void log2file(String filename, String msg, boolean append) {
    if (LOG_LEVEL<OFF) {
      File logFile = new File(filename);
      if (!logFile.getParentFile().exists()) {
        logFile.getParentFile().mkdirs();
      }
      String time = TimeUtils.format("MM-dd:HH:mm:ss", new Date());
      if (!logFile.exists()) {
        try {
          d(BIGLOG_TAG, "log file not exist");
          logFile.createNewFile();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      appendLogFile = true;
      try {
        BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, appendLogFile));
        buf.append(time + "=>" + msg);
        buf.newLine();
        buf.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      d(BIGLOG_TAG, msg);
    }
  }

  private static String defaultTag() {
    StackTraceElement caller = Thread.currentThread().getStackTrace()[4];
    String tag = "%s.%s(L:%d)";
    String callerClazzName = caller.getClassName();
    callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
    tag = String.format(tag, callerClazzName, caller.getMethodName(), caller.getLineNumber());
    return tag;
  }
}
