package org.zeroturnaround.liverebel.plugins.util;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import nanoxml.XMLElement;
import nanoxml.XMLParseException;

/**
 * <code>liverebel.xml</code> representation.
 * 
 * @author Rein Raudjärv
 * 
 * @see #parse(String)
 */
public class LiveRebelXml {

  public static final String FILENAME = "liverebel.xml";

  private final String applicationId;
  private volatile String versionId;

  public LiveRebelXml(String applicationId, String versionId) {
    this.applicationId = applicationId;
    this.versionId = versionId;
  }

  public String getApplicationId() {
    return applicationId;
  }

  public String getVersionId() {
    return versionId;
  }

  public void setVersionId(String versionId) {
    this.versionId = versionId;
  }
  
  public String getAsXml() {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<application>\n" +
        "  <name>" + applicationId + "</name>\n" +
        (versionId == null ? "" :
        "  <version>" + versionId + "</version>\n") +
        "</application>";
  }
  
  public String toString() {
    return versionId == null ? applicationId : (applicationId + "-" + versionId);
  }  
  
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((applicationId == null) ? 0 : applicationId.hashCode());
    result = prime * result
        + ((versionId == null) ? 0 : versionId.hashCode());
    return result;
  }

  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    LiveRebelXml other = (LiveRebelXml) obj;
    if (applicationId == null) {
      if (other.applicationId != null)
        return false;
    } else if (!applicationId.equals(other.applicationId))
      return false;
    if (versionId == null) {
      if (other.versionId != null)
        return false;
    } else if (!versionId.equals(other.versionId))
      return false;
    return true;
  }

  public static LiveRebelXml parse(byte[] xml) {
    if (xml == null)
      throw new IllegalArgumentException("XML must be provided.");

    String str;
    try {
      str = new String(xml, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    return parse(str);
  }

  public static LiveRebelXml parse(String xml) {
    if (xml == null)
      throw new IllegalArgumentException("XML must be provided.");
    if ("".equals(xml))
      throw new IllegalArgumentException("Non-empty XML must be provided.");

    XMLElement element = new XMLElement();
    try {
      element.parseString(xml);
    }
    catch (XMLParseException e) {
      throw new IllegalArgumentException("Could not parse " + FILENAME + ": " + xml);
    }
    return parse(element);
  }

  private static LiveRebelXml parse(XMLElement root) {
    expectName(root, "application");

    String appId = null;
    String moduelId = null;
    String deploymentId = null;
    String versionId = null;

    List children = root.getChildren();
    for (Iterator it = children.iterator(); it.hasNext();) {
      XMLElement child = (XMLElement) it.next();
      String name = child.getName();
      String value = child.getContent();

      if ("name".equals(name))
        appId = value;
      if ("module".equals(name))
        moduelId = value;
      if ("deployment".equals(name))
        deploymentId = value;
      if ("version".equals(name))
        versionId = value;
    }

    expectElementFound(appId, "name");

    return new LiveRebelXml(join(appId, moduelId, deploymentId), versionId);
  }

  private static String join(String a, String b, String c) {
    StringBuffer sb = new StringBuffer(a);
    if (b != null)
      sb.append("-").append(b);
    if (c != null)
      sb.append("-").append(c);
    return sb.toString();
  }

  private static void expectName(XMLElement element, String name) {
    if (!name.equals(element.getName()))
      throw new IllegalArgumentException("Element '" + name
          + "' expected instead of '" + element.getName() + "'.");
  }

  private static void expectElementFound(String value, String name) {
    if (value == null)
      throw new IllegalArgumentException("Element '" + name
          + "' expected but not found.");
  }

}
