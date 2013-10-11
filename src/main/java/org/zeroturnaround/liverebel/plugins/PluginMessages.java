package org.zeroturnaround.liverebel.plugins;

import java.io.File;

/**
 * A way to allow plugins to override user-visible texts, to make them more relevant for particular
 * CI server user interface.
 */
public class PluginMessages {

  public String liveRebelXmlNotFound = "liverebel.xml not found in provided archive (%s) and override information also not specified!";
  public String artifactDeployedAndUpdated = "Artifact %s successfully deployed and activated on all %s servers";

  public String getLiveRebelXmlNotFound(File archive) {
    return String.format(liveRebelXmlNotFound, archive);
  }

  public String getArtifactDeployedAndUpdated(int numOfServers, File archive) {
    return String.format(artifactDeployedAndUpdated, archive, numOfServers);
  }

}
