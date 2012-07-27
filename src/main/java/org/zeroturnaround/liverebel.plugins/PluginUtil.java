package org.zeroturnaround.liverebel.plugins;

import com.zeroturnaround.liverebel.api.ApplicationInfo;
import com.zeroturnaround.liverebel.api.CommandCenter;
import com.zeroturnaround.liverebel.api.CommandCenterFactory;
import com.zeroturnaround.liverebel.api.ConnectException;
import com.zeroturnaround.liverebel.api.DuplicationException;
import com.zeroturnaround.liverebel.api.Forbidden;
import com.zeroturnaround.liverebel.api.ParseException;
import com.zeroturnaround.liverebel.api.UploadInfo;
import com.zeroturnaround.liverebel.api.diff.DiffResult;
import com.zeroturnaround.liverebel.api.diff.Level;
import com.zeroturnaround.liverebel.api.update.ConfigurableUpdate;
import com.zeroturnaround.liverebel.util.LiveRebelXml;
import com.zeroturnaround.liverebel.util.OverrideLiveRebelXmlUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipException;

public class PluginUtil {
  private PluginLogger logger;
  private CommandCenter commandCenter;
  public static final String ARTIFACT_DEPLOYED_AND_UPDATED = "SUCCESS. Artifact deployed and activated in all %d servers: %s\n";

  public PluginUtil(PluginLogger logger) {
    this.logger = logger;
  }

  public boolean perform(File deployableFile, File metadata, String contextPath, UpdateStrategies updateStrategies, List<String> deployableServers, String app, String ver) {

    logger.log("Deploying artifacts.");

    boolean result = false;
    Boolean tempFileCreated = false;

    try {
      logger.log(String.format("Processing artifact: %s\n", deployableFile));

      if (app != null || ver != null) {
        deployableFile = OverrideLiveRebelXmlUtil.overrideOrCreateXML(deployableFile, app, ver);
        tempFileCreated = true;
      }

      LiveRebelXml lrXml = OverrideLiveRebelXmlUtil.getLiveRebelXml(deployableFile);
      ApplicationInfo applicationInfo = getCommandCenter().getApplication(lrXml.getApplicationId());
      uploadIfNeeded(applicationInfo, lrXml.getVersionId(), deployableFile);
      if (metadata != null)
        uploadMetadata(lrXml, metadata);

      if (updateStrategies != null) {
        update(lrXml, applicationInfo, deployableServers, contextPath, updateStrategies);
        logger.log(String.format(ARTIFACT_DEPLOYED_AND_UPDATED, deployableServers.size(), deployableFile));
      }
      result = true;
    }
    catch (IllegalArgumentException e) {
      logger.log("ERROR: " + e.getMessage());
      logger.log(getStackTrace(e));      
    }
    catch (com.zeroturnaround.liverebel.api.Error e) {
      logger.log("ERROR! Unexpected error received from server.");
      logger.log("");
      logger.log("URL: " + e.getURL());
      logger.log("Status code: " + e.getStatus());
      logger.log("Message: " + e.getMessage());
    }
    catch (ParseException e) {
      logger.log("ERROR! Unable to read server response.");
      logger.log("");
      logger.log("Response: " + e.getResponse());
      logger.log("Reason: " + e.getMessage());
    }
    catch (RuntimeException e) {
      if (e.getCause() instanceof ZipException) {
        logger.log(String.format(
          "ERROR! Unable to read artifact (%s). The file you trying to deploy is not an artifact or may be corrupted.\n",
          deployableFile));
      }
      else {
        logger.log("ERROR! Unexpected error occured:");
        logger.log("");
        logger.log(getStackTrace(e));
      }
    }
    catch (Throwable t) {
      logger.log("ERROR! Unexpected error occured:");
      logger.log("");
      logger.log(getStackTrace(t));
    }
    finally {
      if (tempFileCreated) {
        FileUtils.deleteQuietly(deployableFile);
      }
    }
    if (!result)
      return result;

    return true;
  }

  private void uploadMetadata(LiveRebelXml liveRebelXml, File metadata) {
    commandCenter.uploadMetadata(metadata, liveRebelXml.getApplicationId(), liveRebelXml.getVersionId());
  }

  public void uploadIfNeeded(ApplicationInfo applicationInfo, String currentVersion, File archive) throws IOException,
    InterruptedException {
    if (applicationInfo != null && applicationInfo.getVersions().contains(currentVersion)) {
      logger.log("Current version of application is already uploaded. Skipping upload.");
    }
    else {
      uploadArtifact(archive);
      logger.log(String.format("Artifact uploaded: %s\n", archive));
    }
  }

  private boolean uploadArtifact(File artifact) throws IOException, InterruptedException {
    try {
      UploadInfo upload = commandCenter.upload(artifact);
      logger.log(String.format("SUCCESS: %s %s was uploaded.\n", upload.getApplicationId(), upload.getVersionId()));
      return true;
    }
    catch (DuplicationException e) {
      logger.log(e.getMessage());
      return false;
    }
  }

  public boolean initCommandCenter(CommandCenterFactory commandCenterFactory) {
    try {
      this.commandCenter = commandCenterFactory.newCommandCenter();
      return true;
    }
    catch (Forbidden e) {
      logger.log(
        "ERROR! Access denied. Please, navigate to Plugin Configuration to specify LiveRebel Authentication Token.");
      return false;
    }
    catch (ConnectException e) {
      logger.log("ERROR! Unable to connect to server.");
      logger.log("");
      logger.log("URL: " + e.getURL());
      if (e.getURL().equals("https://")) {
        logger.log("Please, navigate to Plugin Configuration to specify running LiveRebel Url.");
      }
      else {
        logger.log("Reason: " + e.getMessage());
      }
      return false;
    }
  }

  void update(LiveRebelXml lrXml, ApplicationInfo applicationInfo, List<String> selectedServers, String contextPath, UpdateStrategies updateStrategies) throws IOException,
    InterruptedException {

    if (selectedServers.isEmpty())
      throw new IllegalArgumentException("Deploy or update artifact was selected without any servers!");

    Set<String> deployServers = getDeployServers(applicationInfo, selectedServers);
    logger.log("Starting updating application on servers: " + deployServers.toString());
    if (!deployServers.isEmpty()) {
      deploy(lrXml, deployServers, contextPath);
    }

    if (deployServers.size() != selectedServers.size()) {
      Set<String> activateServers = new HashSet<String>(selectedServers);
      activateServers.removeAll(deployServers);

      Level diffLevel = getMaxDifferenceLevel(applicationInfo, lrXml, activateServers);

      activate(lrXml, activateServers, diffLevel, updateStrategies);
    }
  }

  void deploy(LiveRebelXml lrXml, Set<String> serverIds, String contextPath) {
    logger.log(String.format("Deploying new application on %s.\n", serverIds));
    if (contextPath.equals(""))
      contextPath = null;
    getCommandCenter().deploy(lrXml.getApplicationId(), lrXml.getVersionId(), contextPath, serverIds);
    logger.log(String.format("SUCCESS: Application deployed to %s.\n", serverIds));
  }

  void activate(LiveRebelXml lrXml, Set<String> serverIds, Level diffLevel, UpdateStrategies updateStrategies) throws IOException,
    InterruptedException {
    ConfigurableUpdate update = getCommandCenter().update(lrXml.getApplicationId(), lrXml.getVersionId());

    if (updateStrategies.isDefault()) {
      update.enableAutoStrategy(updateStrategies.updateWithWarnings());
    } else {
      manualUpdateConfiguration(diffLevel, updateStrategies, update);
    }
    update.on(serverIds);
    update.execute();
  }

  private void manualUpdateConfiguration(Level diffLevel, UpdateStrategies updateStrategies, ConfigurableUpdate update) {
    if (updateStrategies.isHotpatch()) {
      configureHotpatch(diffLevel, updateStrategies, update);
    } else if (updateStrategies.isRolling()) {
      update.enableRolling();
      update.withTimeout(updateStrategies.getSessionDrainTimeout());
    } else if (updateStrategies.isFullRestart()) {
      update.enableOffline();
    }
  }

  private void configureHotpatch(Level diffLevel, UpdateStrategies updateStrategies, ConfigurableUpdate update) {
    if (diffLevel == Level.ERROR || (diffLevel == Level.WARNING  && !updateStrategies.updateWithWarnings()) || diffLevel == Level.REFACTOR) {
      if (!updateStrategies.isFullRestart() && !updateStrategies.isRolling())
        throw new IllegalArgumentException("Only hotpatching selected, but hotpatching not possible!");
      else if (updateStrategies.isRolling())
        update.enableRolling();
      else if (updateStrategies.isFullRestart())
        update.enableOffline();
    } else {
      update.withTimeout(updateStrategies.getRequestPauseTimeout());
      update.enableAutoStrategy(updateStrategies.updateWithWarnings());
    }
  }


  DiffResult getDifferences(LiveRebelXml lrXml, String activeVersion) {
    DiffResult diffResult = getCommandCenter().compare(lrXml.getApplicationId(), activeVersion, lrXml.getVersionId(), false);

    return diffResult;
  }

  Set<String> getDeployServers(ApplicationInfo applicationInfo, List<String> selectedServers) {
    Set<String> deployServers = new HashSet<String>();

    if (isFirstRelease(applicationInfo)) {
      logger.log("IS FIRST RELEASE");
      deployServers.addAll(selectedServers);
      return deployServers;
    }

    Map<String, String> activeVersions = applicationInfo.getActiveVersionPerServer();
    logger.log("activeVersions: " + activeVersions);

    for (String server : selectedServers) {
      if (!activeVersions.containsKey(server))
        deployServers.add(server);
    }
    return deployServers;
  }

  boolean isFirstRelease(ApplicationInfo applicationInfo) {
    return applicationInfo == null;
  }

  private Level getMaxDifferenceLevel(ApplicationInfo applicationInfo, LiveRebelXml lrXml, Set<String> serversToUpdate) {
    Map<String, String> activeVersions = applicationInfo.getActiveVersionPerServer();
    Level diffLevel = Level.NOP;
    String versionToUpdateTo = lrXml.getVersionId();
    int serversWithSameVersion = 0;
    for (Map.Entry<String, String> entry : activeVersions.entrySet()) {
      String server = entry.getKey();
      if (!serversToUpdate.contains(server)) {
        continue;
      }
      String versionInServer = entry.getValue();
      if (StringUtils.equals(versionToUpdateTo, versionInServer)) {
        serversWithSameVersion++;
        serversToUpdate.remove(server);
        logger.log(
          "Server " + server + " already contains active version " + lrXml.getVersionId() + " of application "
            + lrXml.getApplicationId());
      }
      else {
        DiffResult differences = getDifferences(lrXml, versionInServer);
        Level maxLevel = differences.getMaxLevel();
        if (maxLevel.compareTo(diffLevel) > 0) {
          diffLevel = maxLevel;
        }
      }
    }
    if (serversWithSameVersion > 0) {
      String msg = "Cancelling update - version " + lrXml.getVersionId() + " of application "
        + lrXml.getApplicationId() + " is already deployed to " + serversWithSameVersion + " servers";
      if (!serversToUpdate.isEmpty()) {
        msg += " out of " + (serversToUpdate.size() + serversWithSameVersion) + " servers.";
      }
      throw new RuntimeException(msg);
    }
    return diffLevel;
  }

  public CommandCenter getCommandCenter() {
    return commandCenter;
  }
  
  private  String getStackTrace(Throwable aThrowable) {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }
}
