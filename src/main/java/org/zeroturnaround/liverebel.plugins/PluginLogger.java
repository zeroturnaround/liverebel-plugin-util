package org.zeroturnaround.liverebel.plugins;

/**
 * Universal logger used to log messages from plugins. Each plugin has to implement it's own wrapper.
 */
public interface PluginLogger {

  /**
   * Print the message to standard output
   */
  void log(String message);
}
