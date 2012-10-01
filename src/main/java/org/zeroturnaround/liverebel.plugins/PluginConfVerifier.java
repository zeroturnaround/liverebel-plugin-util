package org.zeroturnaround.liverebel.plugins;

import static org.apache.commons.lang.StringUtils.trimToNull;

public class PluginConfVerifier {
  public static void verifyConf(PluginConf conf) {
    if (conf.getAction() == null) {
      throw new IllegalArgumentException("Please choose an action!");
    }
    switch (conf.getAction()) {
      case UPLOAD:
        verifyUploadConf(conf);
        break;
      case DEPLOY_OR_UPDATE:
        verifyDeployOrUpdateConf(conf);
        break;
      case UNDEPLOY:
        verifyUndeployConf(conf);
        break;
    }
  }

  private static void verifyUndeployConf(PluginConf conf) {
    if (conf.undeployId == null) {
      throw new IllegalArgumentException("Undeployable application ID must be given!");
    }
    verifyServers(conf);
  }

  private static void verifyDeployOrUpdateConf(PluginConf conf) {
    verifyUploadConf(conf);
    verifyServers(conf);
    verifyUpdateStrategies(conf);
  }

  private static void verifyUpdateStrategies(PluginConf conf) {
    if (conf.updateStrategies == null) {
      throw new IllegalArgumentException("Please specify update strategy!");
    }
    if (conf.updateStrategies.getPrimaryUpdateStrategy() == null) {
      throw new IllegalArgumentException("Please select primary update strategy!");
    }
    if (conf.updateStrategies.getPrimaryUpdateStrategy().equals(UpdateMode.HOTPATCH) &&
        conf.updateStrategies.getFallbackUpdateStrategy() == null) {
      throw new IllegalArgumentException("No fallback option specified for HOTPATCHING!");
    }
    if (conf.updateStrategies.getPrimaryUpdateStrategy().equals(UpdateMode.FAIL_BUILD))
      throw new IllegalArgumentException("Fail the build cannot be the primary update strategy!");

    if ((conf.updateStrategies.getPrimaryUpdateStrategy().equals(UpdateMode.ROLLING_RESTARTS) ||
        (conf.updateStrategies.getFallbackUpdateStrategy() != null && conf.updateStrategies.getFallbackUpdateStrategy().equals(UpdateMode.ROLLING_RESTARTS))) &&
        conf.serverIds.size() < 2)
      throw new IllegalArgumentException("You cannot use Rolling Restarts for less than 2 servers!");
  }

  private static void verifyServers(PluginConf conf) {
    if (conf.serverIds == null || conf.serverIds.isEmpty()) {
      throw new IllegalArgumentException("No servers specified for " + conf.getAction());
    }
  }

  private static void verifyUploadConf(PluginConf conf) {
    if (conf.deployable == null || !conf.deployable.exists())
      throw new IllegalArgumentException("Archive not given or not found from path " + conf.deployable);
    verifyOverride(conf);
    verifyMetadata(conf);
  }

  private static void verifyMetadata(PluginConf conf) {
    if (conf.metadata != null && !conf.metadata.exists()) {
      throw new IllegalArgumentException("Metadata file not found from path " + conf.metadata);
    }
  }

  private static void verifyOverride(PluginConf conf) {
    conf.overrideVer = trimToNull(conf.overrideVer);
    conf.overrideApp = trimToNull(conf.overrideApp);
    if (conf.isOverride && conf.overrideApp == null && conf.overrideVer == null)
      throw new IllegalArgumentException("Either new Application name or version (or both) must be given for overriding liverebel.xml");
  }
}
