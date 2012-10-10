package org.zeroturnaround.liverebel.plugins;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zeroturnaround.liverebel.api.ApplicationInfo;
import com.zeroturnaround.liverebel.api.CommandCenter;
import com.zeroturnaround.liverebel.api.CommandCenterFactory;
import com.zeroturnaround.liverebel.api.LocalInfo;
import com.zeroturnaround.liverebel.api.UploadInfo;
import com.zeroturnaround.liverebel.api.VersionInfo;
import com.zeroturnaround.liverebel.api.diff.DiffResult;
import com.zeroturnaround.liverebel.api.diff.Level;
import com.zeroturnaround.liverebel.util.LiveRebelXml;
import org.junit.Test;
import org.mockito.Mockito;
import org.omg.PortableInterceptor.SUCCESSFUL;
import org.zeroturnaround.liverebel.test.utils.TestConfigurableUpdateImpl;
import org.zeroturnaround.liverebel.test.utils.TestPluginLogger;
import org.zeroturnaround.liverebel.test.utils.TestUpdateStrategiesImpl;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class PluginUtilTest {
  private static final File archivesDir = new File(PluginUtilTest.class.getResource("archives").getFile());

  @Test
  public void testDefaultUpdate() {
    UpdateStrategies defaultUpdateStrategies = new TestUpdateStrategiesImpl(UpdateMode.LIVEREBEL_DEFAULT, UpdateMode.LIVEREBEL_DEFAULT, 30, true, 30);
    CommandCenterFactory commandCenterFactory = Mockito.mock(CommandCenterFactory.class);
    CommandCenter commandCenter = Mockito.mock(CommandCenter.class);
    Mockito.when(commandCenterFactory.newCommandCenter()).thenReturn(commandCenter);
    Mockito.when(commandCenter.getApplication("lr-demo")).thenReturn(createDummyApplicationInfo("lr-demo"));
    Mockito.when(commandCenter.upload(new File(archivesDir, "lr-demo-ver1.war"))).thenReturn(createDummyUploadInfo());

    TestConfigurableUpdateImpl testConfigurableUpdateSpy = spy(new TestConfigurableUpdateImpl());
    Mockito.when(commandCenter.update((String) any(), (String) any())).thenReturn(testConfigurableUpdateSpy);

    PluginUtil pluginUtil = new PluginUtil(commandCenterFactory, new TestPluginLogger());
    PluginUtil pluginUtilSpy = spy(pluginUtil);
    DiffResult diffResult = mock(DiffResult.class);
    Mockito.when(diffResult.getMaxLevel()).thenReturn(Level.INFO);
    doReturn(diffResult).when(pluginUtilSpy).getDifferences((LiveRebelXml)any(), anyString());
    pluginUtilSpy.initCommandCenter(commandCenterFactory);

    PluginConf conf = new PluginConf(PluginConf.Action.DEPLOY_OR_UPDATE);
    conf.deployable = new File(archivesDir, "lr-demo-ver1.war");
    conf.contextPath = "testDefaultUpdate";
    conf.updateStrategies = defaultUpdateStrategies;
    conf.serverIds = Lists.newArrayList("dummy");

    assertEquals(PluginUtil.PluginActionResult.SUCCESS, pluginUtilSpy.perform(conf));
    assertEquals("HOTPATCH", testConfigurableUpdateSpy.updateMode);
    assertFalse(testConfigurableUpdateSpy.isOffline());
    assertFalse(testConfigurableUpdateSpy.isRolling());
    verify(testConfigurableUpdateSpy).enableAutoStrategy(true);
  }

  @Test
  public void testRolling() {
    UpdateStrategies defaultUpdateStrategies = new TestUpdateStrategiesImpl(UpdateMode.ROLLING_RESTARTS, UpdateMode.LIVEREBEL_DEFAULT, 30, true, 30);
    CommandCenterFactory commandCenterFactory = Mockito.mock(CommandCenterFactory.class);
    CommandCenter commandCenter = Mockito.mock(CommandCenter.class);
    Mockito.when(commandCenterFactory.newCommandCenter()).thenReturn(commandCenter);
    Mockito.when(commandCenter.getApplication("lr-demo")).thenReturn(createDummyApplicationInfo("lr-demo"));
    Mockito.when(commandCenter.upload(new File(archivesDir, "lr-demo-ver1.war"))).thenReturn(createDummyUploadInfo());

    TestConfigurableUpdateImpl testConfigurableUpdateSpy = spy(new TestConfigurableUpdateImpl());
    Mockito.when(commandCenter.update(anyString(), anyString())).thenReturn(testConfigurableUpdateSpy);

    PluginUtil pluginUtil = new PluginUtil(commandCenterFactory, new TestPluginLogger());
    PluginUtil pluginUtilSpy =  spy(pluginUtil);
    DiffResult diffResult = mock(DiffResult.class);
    Mockito.when(diffResult.getMaxLevel()).thenReturn(Level.INFO);
    doReturn(diffResult).when(pluginUtilSpy).getDifferences((LiveRebelXml)any(), anyString());
    pluginUtilSpy.initCommandCenter(commandCenterFactory);

    PluginConf conf = new PluginConf(PluginConf.Action.DEPLOY_OR_UPDATE);
    conf.deployable = new File(archivesDir, "lr-demo-ver1.war");
    conf.contextPath = "testRolling";
    conf.updateStrategies = defaultUpdateStrategies;
    conf.serverIds = Lists.newArrayList("dummy", "dummy2");

    assertEquals(PluginUtil.PluginActionResult.SUCCESS, pluginUtilSpy.perform(conf));
    assertEquals("ROLLING", testConfigurableUpdateSpy.updateMode);
    assertTrue(testConfigurableUpdateSpy.isRolling());
    assertFalse(testConfigurableUpdateSpy.isOffline());
    verify(testConfigurableUpdateSpy).enableRolling();
  }

  @Test
  public void testOffline() {
    UpdateStrategies defaultUpdateStrategies = new TestUpdateStrategiesImpl(UpdateMode.OFFLINE, UpdateMode.LIVEREBEL_DEFAULT, 30, true, 30);
    CommandCenterFactory commandCenterFactory = Mockito.mock(CommandCenterFactory.class);
    CommandCenter commandCenter = Mockito.mock(CommandCenter.class);
    Mockito.when(commandCenterFactory.newCommandCenter()).thenReturn(commandCenter);
    Mockito.when(commandCenter.getApplication("lr-demo")).thenReturn(createDummyApplicationInfo("lr-demo"));
    Mockito.when(commandCenter.upload(new File(archivesDir, "lr-demo-ver1.war"))).thenReturn(createDummyUploadInfo());

    TestConfigurableUpdateImpl testConfigurableUpdateSpy = spy(new TestConfigurableUpdateImpl());
    Mockito.when(commandCenter.update(anyString(), anyString())).thenReturn(testConfigurableUpdateSpy);

    PluginUtil pluginUtil = new PluginUtil(commandCenterFactory, new TestPluginLogger());
    PluginUtil pluginUtilSpy = spy(pluginUtil);
    DiffResult diffResult = mock(DiffResult.class);
    Mockito.when(diffResult.getMaxLevel()).thenReturn(Level.INFO);
    doReturn(diffResult).when(pluginUtilSpy).getDifferences((LiveRebelXml)any(), anyString());
    pluginUtilSpy.initCommandCenter(commandCenterFactory);

    PluginConf conf = new PluginConf(PluginConf.Action.DEPLOY_OR_UPDATE);
    conf.deployable = new File(archivesDir, "lr-demo-ver1.war");
    conf.contextPath = "testOffline";
    conf.updateStrategies = defaultUpdateStrategies;
    conf.serverIds = Lists.newArrayList("dummy");

    assertEquals(PluginUtil.PluginActionResult.SUCCESS, pluginUtilSpy.perform(conf));
    assertEquals("OFFLINE", testConfigurableUpdateSpy.updateMode);
    assertTrue(testConfigurableUpdateSpy.isOffline());
    assertFalse(testConfigurableUpdateSpy.isRolling());
    verify(testConfigurableUpdateSpy).enableOffline();
  }

  private UploadInfo createDummyUploadInfo() {
    return new UploadInfo() {
      public String getApplicationId() {
        return "lr-demo";
      }

      public String getVersionId() {
        return "ver1";
      }
    };
  }

  private ApplicationInfo createDummyApplicationInfo(final String id) {
    return new ApplicationInfo() {
      public String getId() {
        return id;
      }

      public Set<String> getVersions() {
        return Sets.newHashSet("ver1");
      }

      public Map<String, VersionInfo> getVersionsMap() {
        return null;
      }

      public Set<String> getActiveVersions() {
        return null;
      }

      public Map<String, String> getActiveVersionPerServer() {
        HashMap<String, String> map = Maps.newHashMap();
        map.put("dummy", "ver0");
        map.put("dummy2", "ver0");
        return map;
      }

      public List<LocalInfo> getLocalInfos() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
      }

      public Map<String, LocalInfo> getLocalInfosMap() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
      }

      public Set<String> getUrls() {
        return null;
      }
    };
  }
}
