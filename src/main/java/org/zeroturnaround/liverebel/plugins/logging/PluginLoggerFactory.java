package org.zeroturnaround.liverebel.plugins.logging;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.JDK14LoggerAdapter;
import org.slf4j.impl.JDK14LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class PluginLoggerFactory implements ILoggerFactory {

  private Map<String, PluginLoggerAdapter> loggerMap;
  private Deque<PluginBuildLogListener> listeners = new ArrayDeque<PluginBuildLogListener>(20);

  private final JDK14LoggerFactory mainLoggerFactory;
  private final LoggerContext buildLoggerFactory;

  public static PluginLoggerFactory getInstance() {
    return (PluginLoggerFactory) LoggerFactory.getILoggerFactory();
  }

  public PluginLoggerFactory() {
    this.loggerMap = new HashMap<String, PluginLoggerAdapter>(100);
    this.mainLoggerFactory = new JDK14LoggerFactory();
    this.buildLoggerFactory = new LoggerContext();
  }

  public synchronized Logger getLogger(String name) {
    PluginLoggerAdapter logger;
    synchronized (loggerMap) {
      logger = loggerMap.get(name);
      if (logger == null) {
        logger = new PluginLoggerAdapter((JDK14LoggerAdapter) mainLoggerFactory.getLogger(name), buildLoggerFactory.getLogger(name));
        synchronized (listeners) {
          if (listeners.size() == 0) {
            //no build loggers yet, disable build logging
            logger.setMainLoggerEnabled(true);
            logger.setBuildLoggerEnabled(false);
          } else {
            //there are build loggers already, enable build logging
            logger.setMainLoggerEnabled(false);
            logger.setBuildLoggerEnabled(true);
          }
        }
        loggerMap.put(name, logger);
      }
    }
    return logger;
  }

  public PluginBuildLogListener addBuildLogListener(PrintStream ps, String name, String prefix, String pluginLogName, boolean isDebugEnabled) {
    PrintStreamAppender<ILoggingEvent> appender = new PrintStreamAppender<ILoggingEvent>();
    appender.setContext(buildLoggerFactory);
    appender.setName("appender-" + name);
    PatternLayoutEncoder pl = new PatternLayoutEncoder();
    pl.setContext(buildLoggerFactory);
    String pattern;
    if (isDebugEnabled) {
      pattern = "%d{HH:mm:ss.SSS} %-5level %logger{15} - %msg%n";
    } else {
      pattern = "%level: %msg%n";
    }
    if (prefix != null) pattern = prefix + pattern;
    pl.setPattern(pattern);
    pl.start();

    appender.setEncoder(pl);
    appender.setOutputStream(ps);
    appender.start();
    //add appender
    ch.qos.logback.classic.Logger rootLogger = buildLoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    rootLogger.addAppender(appender);
    Level pluginLogLevel;
    if (isDebugEnabled) {
      rootLogger.setLevel(Level.INFO);
      pluginLogLevel = Level.DEBUG;
    } else {
      rootLogger.setLevel(Level.ERROR);
      pluginLogLevel = Level.INFO;
    }
    buildLoggerFactory.getLogger("com.zeroturnaround").setLevel(pluginLogLevel);
    buildLoggerFactory.getLogger("org.zeroturnaround").setLevel(pluginLogLevel);
    buildLoggerFactory.getLogger("com.zeroturnaround.liverebel.api.shaded").setLevel(Level.INFO);
    if (pluginLogName != null) {
      buildLoggerFactory.getLogger(pluginLogName).setLevel(pluginLogLevel);
      buildLoggerFactory.getLogger(pluginLogName + ".shaded").setLevel(Level.INFO);
    }
    PluginBuildLogListener listener = new PluginBuildLogListener(appender);
    synchronized (listeners) {
      if (listeners.size() == 0) {
        //this is the first build logger, enable build logging
        toggleBuildLogging(false, true);
      }
      listeners.add(listener);
    }
    return listener;
  }

  private void toggleBuildLogging(boolean isMainLoggerEnabled, boolean isBuildLoggerEnabled) {
    synchronized (loggerMap) {
      for (PluginLoggerAdapter l : loggerMap.values()) {
        l.setBuildLoggerEnabled(isBuildLoggerEnabled);
        l.setMainLoggerEnabled(isMainLoggerEnabled);
      }
    }
  }

  public void removeBuildLogListener(PluginBuildLogListener listener) {
    if (listener == null) return;
    ch.qos.logback.classic.Logger rootLogger = buildLoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    rootLogger.detachAppender(listener.appender);
    synchronized (listeners) {
      listeners.remove(listener);
      if (listeners.size() == 0) {
        //this is the last build logger, disable build logging
        toggleBuildLogging(true, false);
      }
    }
  }

}