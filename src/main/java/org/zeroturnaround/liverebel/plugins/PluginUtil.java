package org.zeroturnaround.liverebel.plugins;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.zeroturnaround.liverebel.plugins.PluginLogger;

import com.zeroturnaround.liverebel.api.*;
import com.zeroturnaround.liverebel.api.deploy.ConfigurableDeploy;
import com.zeroturnaround.liverebel.api.deploy.ConfigurableUndeploy;
import com.zeroturnaround.liverebel.api.diff.DiffResult;
import com.zeroturnaround.liverebel.api.diff.Level;
import com.zeroturnaround.liverebel.api.update.ConfigurableUpdate;
import com.zeroturnaround.liverebel.util.LiveRebelXml;
import com.zeroturnaround.liverebel.util.OverrideLiveRebelXmlUtil;
import com.zeroturnaround.liverebel.util.ServerKind;

public class PluginUtil {
  private PluginLogger logger;
  private CommandCenter commandCenter;
  private CommandCenterFactory commandCenterFactory;
  public static final String ARTIFACT_DEPLOYED_AND_UPDATED = "SUCCESS. Artifact deployed and activated in all %d servers: %s\n";

  public final static int DEFAULT_SESSION_DRAIN = 3600;
  public final static int DEFAULT_REQUEST_PAUSE = 30;
  public final static boolean DEFAULT_DISABLE_SCRIPTS = false;

  public enum PluginActionResult {
    SUCCESS,
    CONNECTION_ERROR,
    ERROR
  }

  public PluginUtil(CommandCenterFactory commandCenterFactory, PluginLogger logger) {
    this.logger = logger;
    this.commandCenterFactory = commandCenterFactory;
  }

  public PluginActionResult perform(PluginConf conf) {
    if (!initCommandCenter(commandCenterFactory) || this.commandCenter == null) {
      return PluginActionResult.CONNECTION_ERROR;
    }
    boolean tempFileCreated = false;
    boolean success = false;
    try {
      PluginConfVerifier.verifyConf(conf);
      tempFileCreated = checkForLiveRebelXmlOverride(conf);
      doActions(conf);
      success = true;
    }
    catch (Conflict e) {
      logger.log("ERROR: " + e.getMessage());
    }
    catch (IllegalArgumentException e) {
      logger.log("ERROR: " + e.getMessage());
    }
    catch (IllegalStateException e) {
      logger.log("ERROR: " + e.getMessage());
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
            conf.deployable));
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
        FileUtils.deleteQuietly(conf.deployable);
      }
    }
    if (success) return PluginActionResult.SUCCESS;
    return PluginActionResult.ERROR;
  }

  private boolean checkForLiveRebelXmlOverride(PluginConf conf) {
    if (conf.isLiveRebelXmlOverride()) {
      conf.deployable = OverrideLiveRebelXmlUtil.overrideOrCreateXML(conf.deployable, conf.overrideApp, conf.overrideVer);
      return true;
    }
    return false;
  }

  private void doActions(PluginConf conf) throws IOException, InterruptedException {
    switch (conf.getAction()) {
      case UPLOAD:
        upload(conf);
        break;
      case DEPLOY_OR_UPDATE:
        deployOrUpdate(conf);
        break;
      case UNDEPLOY:
        undeploy(conf);
        break;
    }
  }

  private void deployOrUpdate(PluginConf conf) throws IOException, InterruptedException {
    upload(conf);
    LiveRebelXml lrXml = getLiveRebelXmlAndFailIfNotFound(conf);
    ApplicationInfo applicationInfo = getCommandCenter().getApplication(lrXml.getApplicationId());
    update(lrXml, applicationInfo, conf);
    logger.log(String.format(ARTIFACT_DEPLOYED_AND_UPDATED, conf.serverIds.size(), conf.deployable));
  }

  private List<Server> getServers(List<String> serverIds) {
    Map<String, ServerInfo> existingServers = getCommandCenter().getServers();
    List<Server> servers = new ArrayList<Server>();
    for (String serverId : serverIds) {
      ServerInfo serverinfo = existingServers.get(serverId);
      if (existingServers.containsKey(serverId) && serverinfo.isConnected()) {
        servers.add(new ServerImpl(serverId, serverinfo.getName(), "", 0, false, serverinfo.isConnected(), false, serverinfo.getType(),
            serverinfo.isVirtualHostsSupported(), serverinfo.getDefaultVirtualHostName(), serverinfo.getVirtualHostNames()));
      } else {
        logger.error("WARNING! Unknown or offline server with id: " + serverId);
      }
    }
    return servers;
  }

  private void upload(PluginConf conf) throws IOException, InterruptedException {
    LiveRebelXml lrXml = getLiveRebelXmlAndFailIfNotFound(conf);

    ApplicationInfo applicationInfo = getCommandCenter().getApplication(lrXml.getApplicationId());
    if (applicationInfo != null && applicationInfo.getVersions().contains(lrXml.getVersionId())) {
      logger.log("Current version of application is already uploaded. Skipping upload.");
    }
    else {
      uploadArtifact(conf.deployable);
    }

    if (conf.metadata != null) {
      uploadMetadata(lrXml, conf.metadata);
      logger.log(String.format("SUCCESS: Metadata for %s %s was uploaded.\n", lrXml.getApplicationId(), lrXml.getVersionId()));
    }
  }

  private LiveRebelXml getLiveRebelXmlAndFailIfNotFound(PluginConf conf) {
    LiveRebelXml lrXml = OverrideLiveRebelXmlUtil.getLiveRebelXml(conf.deployable);
    if (lrXml == null) throw new IllegalStateException(String.format("Could not find liverebel.xml from %s", conf.deployable));
    return lrXml;
  }

  private void undeploy(PluginConf conf) {
    if (conf.hasStaticContent) {
      conf.serverIds.addAll(conf.staticServerIds);
    }
    List<Server> selectedServers = getServers(conf.serverIds);
    logger.log(String.format("Undeploying application %s on %s.\n", conf.undeployId, selectedServers));

    if (selectedServers.isEmpty()) {
      throw new IllegalArgumentException("Undeploy selected with no online servers!");
    }
    ConfigurableUndeploy undeploy = getCommandCenter().undeploy(conf.undeployId);

    undeploy.on(toIds(selectedServers));

    if (conf.hasDatabaseMigrations) {
      undeploy.setSchemaIds(Collections.singleton(Long.valueOf(conf.schemaId)));
    }

    executeConfigurableAction(undeploy);
    logger.log(String.format("SUCCESS: Application undeploying from %s.\n", selectedServers));
  }

  private void uploadMetadata(LiveRebelXml liveRebelXml, File metadata) {
    commandCenter.uploadTrace(metadata, liveRebelXml.getApplicationId(), liveRebelXml.getVersionId());
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
        "ERROR! Access denied. Please, navigate to Plugin Configuration to specify right LiveRebel Authentication Token.");
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

  private void update(LiveRebelXml lrXml, ApplicationInfo applicationInfo, PluginConf conf) throws IOException, InterruptedException {
    List<Server> selectedServers = getServers(conf.serverIds);
    if (conf.hasStaticContent) {
      selectedServers.addAll(getServers(conf.staticServerIds));
    }
    if (selectedServers.isEmpty())
      throw new IllegalArgumentException("Deploy or update artifact was selected without any servers!");
    Set<Server> deployServers = getDeployServers(applicationInfo, selectedServers);

    if (!deployServers.isEmpty()) {
      deploy(lrXml, deployServers, conf);
    }

    if (deployServers.size() != selectedServers.size()) {
      Set<Server> activateServers = new HashSet<Server>(selectedServers);
      activateServers.removeAll(deployServers);

      Level diffLevel = getMaxDifferenceLevel(applicationInfo, lrXml, activateServers);

      activate(lrXml, activateServers, diffLevel, conf);
    }
  }

  private void deploy(LiveRebelXml lrXml, Set<Server> servers, PluginConf conf) {

    logger.log(String.format("Deploying new application on %s.\n", servers));
    if (conf.contextPath == null || conf.contextPath.length() == 0)
      conf.contextPath = null;

    // if this is static app and only fileservers and web proxies were selected, use context path value as file path
    if (onlyFileserversAndProxiesSelected(servers) && conf.contextPath != null) {
      conf.filePath = conf.contextPath;
    }

    if (conf.virtualHostName == null)
      conf.virtualHostName = "";

    ConfigurableDeploy deploy = getCommandCenter().deploy(lrXml.getApplicationId(), lrXml.getVersionId());

    deploy.on(toIds(servers));

    deploy.setContextPath(conf.contextPath);
    deploy.setVirtualHostName(conf.virtualHostName);

    if (conf.hasDatabaseMigrations) {
      deploy.setProxyTargetServerId(conf.targetProxyId);
      deploy.setSchemaIds(Collections.singleton(Long.valueOf(conf.schemaId)));
    }

    if (conf.filePath != null) {
      deploy.setFileServerDeploymentPath(conf.filePath);
    }

    if (conf.destinationFileName != null) {
      deploy.setDestinationFileName(conf.destinationFileName);
    }

    executeConfigurableAction(deploy);

    logger.log(String.format("SUCCESS: Application deployed to %s.\n", servers));
  }

  private boolean onlyFileserversAndProxiesSelected(Set<Server> servers) {
    for (Server server : servers) {
      if (!(server.getType() == null || server.getType() == ServerKind.FILE || server.getType() == ServerKind.WEB_PROXY))
        return false;
    }
    return true;
  }

  private Collection<String> toIds(Collection<Server> servers) {
    Collection<String> serverIds = new HashSet<String>();
    for (Server server : servers) {
      serverIds.add(server.getId());
    }
    return serverIds;
  }

  void activate(LiveRebelXml lrXml, Set<Server> servers, Level diffLevel, PluginConf conf) throws IOException,
      InterruptedException {
    logger.log("Beginning activation of " + lrXml.getApplicationId() + " " + lrXml.getVersionId() + " on servers: " + servers);
    ConfigurableUpdate update = getCommandCenter().update(lrXml.getApplicationId(), lrXml.getVersionId());

    update.on(toIds(servers)); // must be BEFORE calling enableAutoStrategy!!
    // we can change migration settings when updating
    if (conf.hasDatabaseMigrations) {
      update.setProxyTargetServerId(conf.targetProxyId);
      update.setSchemaIds(Collections.singleton(Long.valueOf(conf.schemaId)));
    }

    if (conf.updateStrategies.getPrimaryUpdateStrategy().equals(UpdateMode.LIVEREBEL_DEFAULT)) {
      update.enableAutoStrategy(conf.updateStrategies.updateWithWarnings());
    }
    else {
      manualUpdateConfiguration(diffLevel, conf.updateStrategies, update, servers.size());
    }

    executeConfigurableAction(update);
  }

  private void manualUpdateConfiguration(Level diffLevel, UpdateStrategies updateStrategies, ConfigurableUpdate update, int serversCount) {
    if (updateStrategies.getPrimaryUpdateStrategy().equals(UpdateMode.HOTPATCH)) {
      configureHotpatch(diffLevel, updateStrategies, update, serversCount);
    } else if (updateStrategies.getPrimaryUpdateStrategy().equals(UpdateMode.ROLLING_RESTARTS)) {
      if (serversCount < 2) throw new IllegalArgumentException("Rolling Restart is not possible with less than 2 servers!");
      update.enableRolling();
      update.withTimeout(updateStrategies.getSessionDrainTimeout());
    } else if (updateStrategies.getPrimaryUpdateStrategy().equals(UpdateMode.OFFLINE)) {
      update.enableOffline();
      update.withTimeout(updateStrategies.getConnectionPauseTimeout());
    }
    else if (updateStrategies.getPrimaryUpdateStrategy().equals(UpdateMode.ALL_AT_ONCE_UPDATE)) {
      update.enableOffline();
      update.withTimeout(updateStrategies.getConnectionPauseTimeout());
    }
  }

  private void configureHotpatch(Level diffLevel, UpdateStrategies updateStrategies, ConfigurableUpdate update, int serversCount) {
    if (diffLevel == Level.ERROR || (diffLevel == Level.WARNING  && !updateStrategies.updateWithWarnings()) || diffLevel == Level.REFACTOR) {
      if (updateStrategies.getFallbackUpdateStrategy().equals(UpdateMode.FAIL_BUILD))
        throw new IllegalArgumentException("Only hotpatching selected, but hotpatching not possible! FAILING BUILD!");
      else if (updateStrategies.getFallbackUpdateStrategy().equals(UpdateMode.ROLLING_RESTARTS) ||
        (updateStrategies.getFallbackUpdateStrategy().equals(UpdateMode.LIVEREBEL_DEFAULT) && serversCount > 1)) {
        update.enableRolling();
        update.withTimeout(updateStrategies.getSessionDrainTimeout());
      }
      else if (updateStrategies.getFallbackUpdateStrategy().equals(UpdateMode.OFFLINE) ||
        (updateStrategies.getFallbackUpdateStrategy().equals(UpdateMode.LIVEREBEL_DEFAULT) && serversCount == 1)) {
        update.enableOffline();
        update.withTimeout(updateStrategies.getConnectionPauseTimeout());
      }
    } else {
      update.withTimeout(updateStrategies.getRequestPauseTimeout());
      update.enableAutoStrategy(updateStrategies.updateWithWarnings());
    }
  }


  DiffResult getDifferences(LiveRebelXml lrXml, String activeVersion) {
    return getCommandCenter().compare(lrXml.getApplicationId(), activeVersion, lrXml.getVersionId(), false);
  }

  Set<Server> getDeployServers(ApplicationInfo applicationInfo, List<Server> selectedServers) {
    Set<Server> deployServers = new HashSet<Server>();

    if (isFirstRelease(applicationInfo)) {
      deployServers.addAll(selectedServers);
      return deployServers;
    }

    Map<String, LocalInfo> activeVersions = applicationInfo.getLocalInfosMap();

    for (Server server : selectedServers) {
      if (!activeVersions.containsKey(server.getId()) || activeVersions.get(server.getId()).getVersionId() == null)
        deployServers.add(server);
    }
    return deployServers;
  }

  boolean isFirstRelease(ApplicationInfo applicationInfo) {
    return applicationInfo == null;
  }

  private Level getMaxDifferenceLevel(ApplicationInfo applicationInfo, LiveRebelXml lrXml, Set<Server> serversToUpdate) {
    Map<String, LocalInfo> activeVersions = applicationInfo.getLocalInfosMap();
    Level diffLevel = Level.NOP;
    String versionToUpdateTo = lrXml.getVersionId();
    int serversWithSameVersion = 0;
    for (Map.Entry<String, LocalInfo> entry : activeVersions.entrySet()) {
      String server = entry.getKey();
      if (!serversToUpdate.contains(server)) {
        continue;
      }
      String versionInServer = entry.getValue().getVersionId();
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
    if (commandCenter == null && !initCommandCenter(commandCenterFactory))
      throw new IllegalStateException("Could not connect to Command Center!");
    return commandCenter;
  }

  public static boolean isMetadataSupported(CommandCenter commandCenter) {
    return commandCenter != null && !commandCenter.getVersion().equals("2.0");
  }

  private  String getStackTrace(Throwable aThrowable) {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }

  private void executeConfigurableAction(PreparedOperation action) {
    Long taskId = -1L;
    taskId = action.execute();

    showTaskLog(taskId);
  }

  /**
   * get and show task log from LR CC in job console
   * @param taskId
   */
  private void showTaskLog(Long taskId) {
    if (taskId > -1L) {
      Collection<String> logFile = commandCenter.getLogLines(taskId);
      printBanner(false);
      for (String line : logFile) {
        logger.log(line);
      }
      printBanner(true);
    }
  }

  private void printBanner(boolean isEnd) {
    logger.log("\n\n" +
        "******************************************************\n" +
        (isEnd ?
            "**              END OF TASK LOG                     **\n"
            : "**            START OF TASK LOG                     **\n") +
        "******************************************************\n" +
        "\n\n"
        );
  }
}
