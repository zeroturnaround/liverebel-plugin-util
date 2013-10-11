package org.zeroturnaround.liverebel.plugins.logging;

import ch.qos.logback.core.OutputStreamAppender;

/**
 * Extension of Logback {@link OutputStreamAppender}, to make it suitable for 
 * usage inside Jenkins plugin.
 */
public class PrintStreamAppender<E> extends OutputStreamAppender<E> {

  @Override
  protected void closeOutputStream() {
    //do not close the stream, it is not our responsibility
  }

}
