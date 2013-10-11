package org.zeroturnaround.liverebel.plugins.logging;

import java.io.PrintStream;

import org.slf4j.Marker;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.impl.JDK14LoggerAdapter;
import org.slf4j.spi.LocationAwareLogger;

/**
 * SLF4J adapter to delegate to java.util.logging (always) and to logback when plugin is running.
 * Logback is set up before the plugin runs and shut down after it has ran. Logback output
 * is redirected to plugin console {@link PrintStream}.
 * 
 * @see LogbackConfigurer
 */
public final class PluginLoggerAdapter extends MarkerIgnoringBase implements
    LocationAwareLogger {

  private static final long serialVersionUID = 1L;

  private final JDK14LoggerAdapter mainLogger;
  private final ch.qos.logback.classic.Logger buildLogger;

  private transient boolean isMainLoggerEnabled = true;
  private transient boolean isBuildLoggerEnabled = true;

  PluginLoggerAdapter(JDK14LoggerAdapter mainLogger, ch.qos.logback.classic.Logger logger) {
    this.mainLogger = mainLogger;
    this.buildLogger = logger;
    this.name = logger.getName();
  }

  public void setMainLoggerEnabled(boolean isMainLoggerEnabled) {
    this.isMainLoggerEnabled = isMainLoggerEnabled;
  }

  public void setBuildLoggerEnabled(boolean isBuildLoggerEnabled) {
    this.isBuildLoggerEnabled = isBuildLoggerEnabled;
  }

  /**
   * Is this logger instance enabled for the FINEST level?
   * 
   * @return True if this Logger is enabled for level FINEST, false otherwise.
   */
  public boolean isTraceEnabled() {
    return (isMainLoggerEnabled && mainLogger.isTraceEnabled()) || (isBuildLoggerEnabled && buildLogger.isTraceEnabled());
  }

  /**
   * Log a message object at level FINEST.
   * 
   * @param msg
   *          - the message object to be logged
   */
  public void trace(String msg) {
    if (isMainLoggerEnabled) mainLogger.trace(msg);
    if (isBuildLoggerEnabled) buildLogger.trace(msg);
  }

  /**
   * Log a message at level FINEST according to the specified format and
   * argument.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for level FINEST.
   * </p>
   * 
   * @param format
   *          the format string
   * @param arg
   *          the argument
   */
  public void trace(String format, Object arg) {
    if (isMainLoggerEnabled) mainLogger.trace(format, arg);
    if (isBuildLoggerEnabled) buildLogger.trace(format, arg);
  }

  /**
   * Log a message at level FINEST according to the specified format and
   * arguments.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the FINEST level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param arg1
   *          the first argument
   * @param arg2
   *          the second argument
   */
  public void trace(String format, Object arg1, Object arg2) {
    if (isMainLoggerEnabled) mainLogger.trace(format, arg1, arg2);
    if (isBuildLoggerEnabled) buildLogger.trace(format, arg1, arg2);
  }

  /**
   * Log a message at level FINEST according to the specified format and
   * arguments.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the FINEST level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param argArray
   *          an array of arguments
   */
  public void trace(String format, Object[] argArray) {
    if (isMainLoggerEnabled) mainLogger.trace(format, argArray);
    if (isBuildLoggerEnabled) buildLogger.trace(format, argArray);
  }

  /**
   * Log an exception (throwable) at level FINEST with an accompanying message.
   * 
   * @param msg
   *          the message accompanying the exception
   * @param t
   *          the exception (throwable) to log
   */
  public void trace(String msg, Throwable t) {
    if (isMainLoggerEnabled) mainLogger.trace(msg, t);
    if (isBuildLoggerEnabled) buildLogger.trace(msg, t);
  }

  /**
   * Is this logger instance enabled for the FINE level?
   * 
   * @return True if this Logger is enabled for level FINE, false otherwise.
   */
  public boolean isDebugEnabled() {
    return (isMainLoggerEnabled && mainLogger.isDebugEnabled()) || (isBuildLoggerEnabled && buildLogger.isDebugEnabled());
  }

  /**
   * Log a message object at level FINE.
   * 
   * @param msg
   *          - the message object to be logged
   */
  public void debug(String msg) {
    if (isMainLoggerEnabled) mainLogger.debug(msg);
    if (isBuildLoggerEnabled) buildLogger.debug(msg);
  }

  /**
   * Log a message at level FINE according to the specified format and argument.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for level FINE.
   * </p>
   * 
   * @param format
   *          the format string
   * @param arg
   *          the argument
   */
  public void debug(String format, Object arg) {
    if (isMainLoggerEnabled) mainLogger.debug(format, arg);
    if (isBuildLoggerEnabled) buildLogger.debug(format, arg);
  }

  /**
   * Log a message at level FINE according to the specified format and
   * arguments.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the FINE level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param arg1
   *          the first argument
   * @param arg2
   *          the second argument
   */
  public void debug(String format, Object arg1, Object arg2) {
    if (isMainLoggerEnabled) mainLogger.debug(format, arg1, arg2);
    if (isBuildLoggerEnabled) buildLogger.debug(format, arg1, arg2);
  }

  /**
   * Log a message at level FINE according to the specified format and
   * arguments.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the FINE level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param argArray
   *          an array of arguments
   */
  public void debug(String format, Object[] argArray) {
    if (isMainLoggerEnabled) mainLogger.debug(format, argArray);
    if (isBuildLoggerEnabled) buildLogger.debug(format, argArray);
  }

  /**
   * Log an exception (throwable) at level FINE with an accompanying message.
   * 
   * @param msg
   *          the message accompanying the exception
   * @param t
   *          the exception (throwable) to log
   */
  public void debug(String msg, Throwable t) {
    if (isMainLoggerEnabled) mainLogger.debug(msg, t);
    if (isBuildLoggerEnabled) buildLogger.debug(msg, t);
  }

  /**
   * Is this logger instance enabled for the INFO level?
   * 
   * @return True if this Logger is enabled for the INFO level, false otherwise.
   */
  public boolean isInfoEnabled() {
    return (isMainLoggerEnabled && mainLogger.isInfoEnabled()) || (isBuildLoggerEnabled && buildLogger.isInfoEnabled());
  }

  /**
   * Log a message object at the INFO level.
   * 
   * @param msg
   *          - the message object to be logged
   */
  public void info(String msg) {
    if (isMainLoggerEnabled) mainLogger.info(msg);
    if (isBuildLoggerEnabled) buildLogger.info(msg);
  }

  /**
   * Log a message at level INFO according to the specified format and argument.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the INFO level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param arg
   *          the argument
   */
  public void info(String format, Object arg) {
    if (isMainLoggerEnabled) mainLogger.info(format, arg);
    if (isBuildLoggerEnabled) buildLogger.info(format, arg);
  }

  /**
   * Log a message at the INFO level according to the specified format and
   * arguments.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the INFO level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param arg1
   *          the first argument
   * @param arg2
   *          the second argument
   */
  public void info(String format, Object arg1, Object arg2) {
    if (isMainLoggerEnabled) mainLogger.info(format, arg1, arg2);
    if (isBuildLoggerEnabled) buildLogger.info(format, arg1, arg2);
  }

  /**
   * Log a message at level INFO according to the specified format and
   * arguments.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the INFO level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param argArray
   *          an array of arguments
   */
  public void info(String format, Object[] argArray) {
    if (isMainLoggerEnabled) mainLogger.info(format, argArray);
    if (isBuildLoggerEnabled) buildLogger.info(format, argArray);
  }

  /**
   * Log an exception (throwable) at the INFO level with an accompanying
   * message.
   * 
   * @param msg
   *          the message accompanying the exception
   * @param t
   *          the exception (throwable) to log
   */
  public void info(String msg, Throwable t) {
    if (isMainLoggerEnabled) mainLogger.info(msg, t);
    if (isBuildLoggerEnabled) buildLogger.info(msg, t);
  }

  /**
   * Is this logger instance enabled for the WARNING level?
   * 
   * @return True if this Logger is enabled for the WARNING level, false
   *         otherwise.
   */
  public boolean isWarnEnabled() {
    return (isMainLoggerEnabled && mainLogger.isWarnEnabled()) || (isBuildLoggerEnabled && buildLogger.isWarnEnabled());
  }

  /**
   * Log a message object at the WARNING level.
   * 
   * @param msg
   *          - the message object to be logged
   */
  public void warn(String msg) {
    if (isMainLoggerEnabled) mainLogger.warn(msg);
    if (isBuildLoggerEnabled) buildLogger.warn(msg);
  }

  /**
   * Log a message at the WARNING level according to the specified format and
   * argument.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the WARNING level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param arg
   *          the argument
   */
  public void warn(String format, Object arg) {
    if (isMainLoggerEnabled) mainLogger.warn(format, arg);
    if (isBuildLoggerEnabled) buildLogger.warn(format, arg);
  }

  /**
   * Log a message at the WARNING level according to the specified format and
   * arguments.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the WARNING level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param arg1
   *          the first argument
   * @param arg2
   *          the second argument
   */
  public void warn(String format, Object arg1, Object arg2) {
    if (isMainLoggerEnabled) mainLogger.warn(format, arg1, arg2);
    if (isBuildLoggerEnabled) buildLogger.warn(format, arg1, arg2);
  }

  /**
   * Log a message at level WARNING according to the specified format and
   * arguments.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the WARNING level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param argArray
   *          an array of arguments
   */
  public void warn(String format, Object[] argArray) {
    if (isMainLoggerEnabled) mainLogger.warn(format, argArray);
    if (isBuildLoggerEnabled) buildLogger.warn(format, argArray);
  }

  /**
   * Log an exception (throwable) at the WARNING level with an accompanying
   * message.
   * 
   * @param msg
   *          the message accompanying the exception
   * @param t
   *          the exception (throwable) to log
   */
  public void warn(String msg, Throwable t) {
    if (isMainLoggerEnabled) mainLogger.warn(msg, t);
    if (isBuildLoggerEnabled) buildLogger.warn(msg, t);
  }

  /**
   * Is this logger instance enabled for level SEVERE?
   * 
   * @return True if this Logger is enabled for level SEVERE, false otherwise.
   */
  public boolean isErrorEnabled() {
    return (isMainLoggerEnabled && mainLogger.isErrorEnabled()) || (isBuildLoggerEnabled && buildLogger.isErrorEnabled());
  }

  /**
   * Log a message object at the SEVERE level.
   * 
   * @param msg
   *          - the message object to be logged
   */
  public void error(String msg) {
    if (isMainLoggerEnabled) mainLogger.error(msg);
    if (isBuildLoggerEnabled) buildLogger.error(msg);
  }

  /**
   * Log a message at the SEVERE level according to the specified format and
   * argument.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the SEVERE level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param arg
   *          the argument
   */
  public void error(String format, Object arg) {
    if (isMainLoggerEnabled) mainLogger.error(format, arg);
    if (isBuildLoggerEnabled) buildLogger.error(format, arg);
  }

  /**
   * Log a message at the SEVERE level according to the specified format and
   * arguments.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the SEVERE level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param arg1
   *          the first argument
   * @param arg2
   *          the second argument
   */
  public void error(String format, Object arg1, Object arg2) {
    if (isMainLoggerEnabled) mainLogger.error(format, arg1, arg2);
    if (isBuildLoggerEnabled) buildLogger.error(format, arg1, arg2);
  }

  /**
   * Log a message at level SEVERE according to the specified format and
   * arguments.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the SEVERE level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param argArray
   *          an array of arguments
   */
  public void error(String format, Object[] argArray) {
    if (isMainLoggerEnabled) mainLogger.error(format, argArray);
    if (isBuildLoggerEnabled) buildLogger.error(format, argArray);
  }

  /**
   * Log an exception (throwable) at the SEVERE level with an accompanying
   * message.
   * 
   * @param msg
   *          the message accompanying the exception
   * @param t
   *          the exception (throwable) to log
   */
  public void error(String msg, Throwable t) {
    if (isMainLoggerEnabled) mainLogger.error(msg, t);
    if (isBuildLoggerEnabled) buildLogger.error(msg, t);
  }

  public void log(Marker marker, String callerFQCN, int level, String message,
      Object[] argArray, Throwable t) {
    if (isMainLoggerEnabled) mainLogger.log(marker, callerFQCN, level, message, argArray, t);
    if (isBuildLoggerEnabled) buildLogger.log(marker, callerFQCN, level, message, argArray, t);
  }

}
