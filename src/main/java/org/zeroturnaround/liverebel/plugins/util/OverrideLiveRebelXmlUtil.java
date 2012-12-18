package org.zeroturnaround.liverebel.plugins.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import org.zeroturnaround.zip.ZipUtil;
import com.zeroturnaround.liverebel.util.SanitizeHelper;

public class OverrideLiveRebelXmlUtil {
  public static final String TYPE_JAR = "JAR";
  public static final String TYPE_WAR = "WAR";
  public static final String APPLICATION_XML = "application.xml";
  public static final String TYPE_EAR = "EAR";

  private static final String META_INF = "META-INF";
  private static final String WEB_INF = "WEB-INF";
  private static final String CLASSES = "classes";
  public static final String WEB_INF_CLASSES_PREFIX = WEB_INF + "/" + CLASSES + "/";

  private static final String[] WAR_ARCHIVE_ENTRIES = new String[] {
      WEB_INF + "/",
      WEB_INF + "/web.xml",
      WEB_INF_CLASSES_PREFIX + LiveRebelXml.FILENAME };

  public static File addLiveRebelXml(File file, LiveRebelXml xml) {
    String destPath = file.getParentFile().getAbsolutePath()
      + "/"
      + SanitizeHelper.sanitize(xml.getApplicationId() + "-" + xml.getVersionId() + "-"
        + System.currentTimeMillis());
    File destFile = new File(destPath);
    byte[] bytes;
    try {
      bytes = xml.getAsXml().getBytes("UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    String liverebelXml = getLiveRebelXMLLocationByType(file);
    if (ZipUtil.containsEntry(file, liverebelXml)) {
      ZipUtil.replaceEntry(file, liverebelXml, bytes, destFile);
    }
    else {
      ZipUtil.addEntry(file, liverebelXml, bytes, destFile);
    }
    return destFile;
  }

  public static String getLiveRebelXMLLocationByType(File archive) throws IllegalStateException {
    String type = findApplicationType(archive);
    String liverebelXml = null;
    if (type.equals(TYPE_WAR)) {
      liverebelXml = "WEB-INF/classes/liverebel.xml";
    } else if (type.equals(TYPE_EAR)) {
      liverebelXml = "liverebel.xml";
    } else if (type.equals(TYPE_JAR)) {
      liverebelXml = "liverebel.xml";
    }
    if (liverebelXml == null)
      throw new IllegalStateException("Uknown file type: " +  type);
    return liverebelXml;
  }

  public static File overrideOrCreateXML(File file, String app, String ver) {
    if (!file.isFile())
      throw new IllegalArgumentException("File not found: " + file.getAbsolutePath());
    LiveRebelXml initialArchiveXml = getLiveRebelXml(file);
    if (initialArchiveXml == null) {
      return createLiveRebelXml(file, app, ver);
    } else {
      return overrideLiveRebelXml(file, app, ver, initialArchiveXml);
    }
  }

  public static LiveRebelXml getLiveRebelXml(File file) {
    byte[] fileEntry = ZipUtil.unpackEntry(file, "WEB-INF/classes/liverebel.xml");
    if (fileEntry == null) {
      // maybe it is an EAR file?
      fileEntry = ZipUtil.unpackEntry(file, "liverebel.xml");
      if (fileEntry == null) {
        throw new IllegalStateException("No liverebel.xml found in the archive " + file + " !");
      }
    }
    return LiveRebelXml.parse(fileEntry);
  }

  private static File overrideLiveRebelXml(File file, String app, String ver, LiveRebelXml initialArchiveXml) {
    LiveRebelXml newArchiveXml;
    if (app != null && ver != null){
      newArchiveXml = new LiveRebelXml(app, ver);
    } else if (app == null) {
      newArchiveXml = new LiveRebelXml(initialArchiveXml.getApplicationId(), ver);
    } else {
      newArchiveXml = new LiveRebelXml(app, initialArchiveXml.getVersionId());
    }
    return addLiveRebelXml(file, newArchiveXml);
  }

  private static File createLiveRebelXml(File file, String app, String ver) {
    if (app == null || ver == null) {
      throw new RuntimeException("ERROR! Both \"-app\" and \"-ver\" must be given to create new liverebel.xml!");
    }

    LiveRebelXml xml = new LiveRebelXml(app, ver);
    return addLiveRebelXml(file, xml);
  }

  /**
   * Detects the application type.
   *
   * @param application EAR, WAR or JAR file/directory (not <code>null</code>).
   * @return either EAR, WAR or JAR.
   */
  public static String findApplicationType(File application) {
    if (application == null)
      throw new IllegalArgumentException("Application must be provided.");
    if (!application.exists())
      throw new IllegalArgumentException("Application not found: " + application);

    if (application.isDirectory()) {
      File appXml = new File(new File(application, META_INF), APPLICATION_XML);
      if (appXml.isFile())
        return TYPE_EAR;

      // Maybe WAR ?
      File webInf = new File(application, WEB_INF);
      if (webInf.isDirectory())
        return TYPE_WAR;

      // JAR then
      return TYPE_JAR;
    }
    else {
      // Maybe EAR ?
      if (ZipUtil.containsEntry(application, META_INF + "/" + APPLICATION_XML))
        return TYPE_EAR;

      // Maybe WAR ?
      if (ZipUtil.containsAnyEntry(application, WAR_ARCHIVE_ENTRIES))
        return TYPE_WAR;

      // JAR then
      return TYPE_JAR;
    }
  }
}
