package org.zeroturnaround.liverebel.plugins;

/**
 * Universal logger used to log messages from plugins. Each plugin has to implement it's own wrapper.
 */
public interface PluginLogger {

  /**
   * Log the message
   */
  void log(String message);

  /**
   * Log the error. Some plugins may want to log errors differently
   */
  void error(String error);
}
