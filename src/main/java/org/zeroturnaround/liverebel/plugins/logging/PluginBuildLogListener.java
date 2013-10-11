package org.zeroturnaround.liverebel.plugins.logging;


import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Wrapper class to hide the Logback API.
 */
public class PluginBuildLogListener {

  final PrintStreamAppender<ILoggingEvent> appender;

  public PluginBuildLogListener(PrintStreamAppender<ILoggingEvent> appender) {
    this.appender = appender;
  }

}
