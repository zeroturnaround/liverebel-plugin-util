package org.zeroturnaround.liverebel.plugins;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mockito;
import org.zeroturnaround.liverebel.plugins.logging.PluginBuildLogListener;
import org.zeroturnaround.liverebel.plugins.logging.PluginLoggerFactory;
import org.zeroturnaround.liverebel.test.utils.TestConfigurableDeployImpl;
import org.zeroturnaround.liverebel.test.utils.TestConfigurableUpdateImpl;
import org.zeroturnaround.liverebel.test.utils.TestLocalInfoImpl;
import org.zeroturnaround.liverebel.test.utils.TestSchemaInfoImpl;
import org.zeroturnaround.liverebel.test.utils.TestServerInfoImpl;
import org.zeroturnaround.liverebel.test.utils.TestUpdateStrategiesImpl;
import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.transform.ZipEntryTransformerEntry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zeroturnaround.liverebel.api.ApplicationInfo;
import com.zeroturnaround.liverebel.api.CommandCenter;
import com.zeroturnaround.liverebel.api.CommandCenterFactory;
import com.zeroturnaround.liverebel.api.LocalInfo;
import com.zeroturnaround.liverebel.api.SchemaInfo;
import com.zeroturnaround.liverebel.api.ServerInfo;
import com.zeroturnaround.liverebel.api.UploadInfo;
import com.zeroturnaround.liverebel.api.VersionInfo;
import com.zeroturnaround.liverebel.api.diff.DiffResult;
import com.zeroturnaround.liverebel.api.diff.Level;
import com.zeroturnaround.liverebel.util.LiveRebelXml;
import com.zeroturnaround.liverebel.util.ServerKind;

public class PluginUtilTest {
  private static final File archivesDir = new File(PluginUtilTest.class.getResource("archives").getFile());
  private static final Map<String, ServerInfo> mockedServers = createMockedServer();
  private static final Map<Long, SchemaInfo> mockedSchemas = createMockedSchemas();

  @Rule public TestName testName = new TestName();

  private ByteArrayOutputStream testOutput;
  private PluginBuildLogListener logListener;

  @Before
  public void configureLogging() {
    this.testOutput = new ByteArrayOutputStream(1024);
    PrintStream ps = new PrintStream(this.testOutput);
    logListener = PluginLoggerFactory.getInstance().addBuildLogListener(ps, "test", getTestName() + " ", null, true);
  }

  private String getTestName() {
    return PluginUtilTest.class.getSimpleName() + "." + testName.getMethodName();
  }

  private String getTestBuildLog() {
    try {
      return new String(testOutput.toByteArray(), "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("should never happen", e);
    }
  }

  @After
  public void closeLogging() throws UnsupportedEncodingException {
    PluginLoggerFactory.getInstance().removeBuildLogListener(logListener);
    System.out.println("Output from '" + getTestName() + "' build logger:\n" + getTestBuildLog());
  }

  private static Map<String, ServerInfo> createMockedServer() {
    Map<String, ServerInfo> mockedServers = new HashMap<String, ServerInfo>();
    mockedServers.put("dummy", new TestServerInfoImpl("dummy", ServerKind.APPSERVER));
    mockedServers.put("dummy2", new TestServerInfoImpl("dummy2", ServerKind.APPSERVER));
    mockedServers.put("db-standalone", new TestServerInfoImpl("db-standalone", ServerKind.DATABASE));
    mockedServers.put("db-proxy", new TestServerInfoImpl("db-proxy", ServerKind.DATABASE));
    mockedServers.put("fileserver", new TestServerInfoImpl("fileserver", ServerKind.FILE));
    return mockedServers;  //To change body of created methods use File | Settings | File Templates.
  }

  private static Map<Long, SchemaInfo> createMockedSchemas() {
    Map<Long, SchemaInfo> mockedSchemas = new HashMap<Long, SchemaInfo>();
    mockedSchemas.put(1L, new TestSchemaInfoImpl(1L, "db-standalone"));
    mockedSchemas.put(2L, new TestSchemaInfoImpl(2L, "db-proxy", "dummy2"));
    return mockedSchemas;
  }

  @Test
  public void testDefaultUpdate() {
    CommandCenterFactory commandCenterFactory = Mockito.mock(CommandCenterFactory.class);
    CommandCenter commandCenter = mockCC(commandCenterFactory);
    TestConfigurableUpdateImpl testConfigurableUpdateSpy = spyConfigurableUpdate(commandCenter);
    PluginUtil pluginUtilSpy = spyPluginUtil(commandCenterFactory);

    PluginConf conf = new PluginConf(PluginConf.Action.DEPLOY_OR_UPDATE);
    conf.deployable = new File(archivesDir, "lr-demo-ver1.war");
    conf.contextPath = "testDefaultUpdate";
    conf.updateStrategies = new TestUpdateStrategiesImpl(UpdateMode.LIVEREBEL_DEFAULT, UpdateMode.LIVEREBEL_DEFAULT, 30, true, 30);
    conf.serverIds = Lists.newArrayList("dummy");

    assertEquals(PluginUtil.PluginActionResult.SUCCESS, pluginUtilSpy.perform(conf));
    assertEquals("HOTPATCH", testConfigurableUpdateSpy.updateMode);
    assertFalse(testConfigurableUpdateSpy.isOffline());
    assertFalse(testConfigurableUpdateSpy.isRolling());
    verify(testConfigurableUpdateSpy).enableAutoStrategy(true);
    assertBuildLogDoesNotContainExceptions();
    assertBuildLogDoesNotContainErrors();
  }

  @Test
  public void testWarUpdateWithoutLiverebelXml() {
    CommandCenterFactory commandCenterFactory = Mockito.mock(CommandCenterFactory.class);
    mockCC(commandCenterFactory);
    PluginUtil pluginUtilSpy = spyPluginUtil(commandCenterFactory);

    PluginConf conf = new PluginConf(PluginConf.Action.DEPLOY_OR_UPDATE);
    File appWarWithLiverebelXml = new File(archivesDir, "lr-demo-ver1.war");
    File appWarWithoutLiverebelXml = new File(FileUtils.getTempDirectory(), "lr-demo-ver1-without-liverebel-xml.war");
    ZipUtil.removeEntry(appWarWithLiverebelXml, "WEB-INF/classes/liverebel.xml", appWarWithoutLiverebelXml);
    conf.deployable = appWarWithoutLiverebelXml;
    try {
      conf.contextPath = "testDefaultUpdate";
      conf.updateStrategies = new TestUpdateStrategiesImpl(UpdateMode.LIVEREBEL_DEFAULT, UpdateMode.LIVEREBEL_DEFAULT, 30, true, 30);
      conf.serverIds = Lists.newArrayList("dummy");

      assertEquals(PluginUtil.PluginActionResult.ERROR, pluginUtilSpy.perform(conf));
      assertBuildLogDoesNotContainExceptions();
    }
    finally {
      FileUtils.deleteQuietly(conf.deployable);
    }
  }

  private void assertBuildLogDoesNotContainExceptions() {
    assertTrue("log should not contain exceptions", !getTestBuildLog().contains("Exception"));
  }
  private void assertBuildLogDoesNotContainErrors() {
    assertTrue("log should not contain error level rows", !getTestBuildLog().contains("ERROR"));
  }

  private TestConfigurableUpdateImpl spyConfigurableUpdate(CommandCenter commandCenter) {
    TestConfigurableUpdateImpl testConfigurableUpdateSpy = spy(new TestConfigurableUpdateImpl());
    Mockito.when(commandCenter.update((String) any(), (String) any())).thenReturn(testConfigurableUpdateSpy);
    return testConfigurableUpdateSpy;
  }

  private TestConfigurableDeployImpl spyConfigurableDeploy(CommandCenter commandCenter) {
    TestConfigurableDeployImpl testConfigurableDeploySpy = spy(new TestConfigurableDeployImpl());
    Mockito.when(commandCenter.deploy((String) any(), (String) any())).thenReturn(testConfigurableDeploySpy);
    return testConfigurableDeploySpy;
  }

  private CommandCenter mockCC(CommandCenterFactory commandCenterFactory) {
    CommandCenter commandCenter = Mockito.mock(CommandCenter.class);
    Mockito.when(commandCenterFactory.newCommandCenter()).thenReturn(commandCenter);
    Mockito.when(commandCenter.getApplication("lr-demo")).thenReturn(createDummyApplicationInfo("lr-demo"));
    Mockito.when(commandCenter.getApplication("testapp")).thenReturn(createDummySchemaApplicationInfo("testapp"));
    Mockito.when(commandCenter.getApplication("test-db-app-not-exists")).thenReturn(null);
    Mockito.when(commandCenter.getServers()).thenReturn(mockedServers);
    Mockito.when(commandCenter.getAllDatabaseSchemas()).thenReturn(mockedSchemas);
    Mockito.when(commandCenter.upload(new File(archivesDir, "lr-demo-ver1.war"))).thenReturn(createDummyUploadInfo());
    Mockito.when(commandCenter.upload(new File(archivesDir, "test-db-app-ver1.war"))).thenReturn(createDummySchemaUploadInfo());
    Mockito.when(commandCenter.upload(new File(archivesDir, "test-db-app-not-exists.war"))).thenReturn(createDummyNotExistingUploadInfo());
    List<String> logLines = new ArrayList<String>();
    logLines.add("This is the log line");
    Mockito.when(commandCenter.getLogLines(anyLong())).thenReturn(logLines);
    return commandCenter;
  }

  @Test
  public void testRolling() {
    CommandCenterFactory commandCenterFactory = Mockito.mock(CommandCenterFactory.class);
    CommandCenter commandCenter = mockCC(commandCenterFactory);
    TestConfigurableUpdateImpl testConfigurableUpdateSpy = spy(new TestConfigurableUpdateImpl());
    Mockito.when(commandCenter.update(anyString(), anyString())).thenReturn(testConfigurableUpdateSpy);
    PluginUtil pluginUtilSpy = spyPluginUtil(commandCenterFactory);

    PluginConf conf = new PluginConf(PluginConf.Action.DEPLOY_OR_UPDATE);
    conf.deployable = new File(archivesDir, "lr-demo-ver1.war");
    conf.contextPath = "testRolling";
    conf.updateStrategies = new TestUpdateStrategiesImpl(UpdateMode.ROLLING_RESTARTS, UpdateMode.LIVEREBEL_DEFAULT, 30, true, 30);
    conf.serverIds = Lists.newArrayList("dummy", "dummy2");

    assertEquals(PluginUtil.PluginActionResult.SUCCESS, pluginUtilSpy.perform(conf));
    assertEquals("ROLLING", testConfigurableUpdateSpy.updateMode);
    assertTrue(testConfigurableUpdateSpy.isRolling());
    assertFalse(testConfigurableUpdateSpy.isOffline());
    verify(testConfigurableUpdateSpy).enableRolling();
    assertBuildLogDoesNotContainExceptions();
    assertBuildLogDoesNotContainErrors();
  }

  private PluginUtil spyPluginUtil(CommandCenterFactory commandCenterFactory) {
    PluginUtil pluginUtil = new PluginUtil(commandCenterFactory, new PluginMessages());
    PluginUtil pluginUtilSpy =  spy(pluginUtil);
    DiffResult diffResult = mock(DiffResult.class);
    Mockito.when(diffResult.getMaxLevel()).thenReturn(Level.INFO);
    doReturn(diffResult).when(pluginUtilSpy).getDifferences((LiveRebelXml)any(), anyString());
    pluginUtilSpy.initCommandCenter(commandCenterFactory);
    return pluginUtilSpy;
  }

  @Test
  public void testOffline() {
    CommandCenterFactory commandCenterFactory = Mockito.mock(CommandCenterFactory.class);
    CommandCenter commandCenter = mockCC(commandCenterFactory);
    TestConfigurableUpdateImpl testConfigurableUpdateSpy = spy(new TestConfigurableUpdateImpl());
    Mockito.when(commandCenter.update(anyString(), anyString())).thenReturn(testConfigurableUpdateSpy);
    PluginUtil pluginUtilSpy = spyPluginUtil(commandCenterFactory);

    PluginConf conf = new PluginConf(PluginConf.Action.DEPLOY_OR_UPDATE);
    conf.deployable = new File(archivesDir, "lr-demo-ver1.war");
    conf.contextPath = "testOffline";
    conf.updateStrategies = new TestUpdateStrategiesImpl(UpdateMode.OFFLINE, UpdateMode.LIVEREBEL_DEFAULT, 30, true, 30);
    conf.serverIds = Lists.newArrayList("dummy");

    assertEquals(PluginUtil.PluginActionResult.SUCCESS, pluginUtilSpy.perform(conf));
    assertEquals("OFFLINE", testConfigurableUpdateSpy.updateMode);
    assertTrue(testConfigurableUpdateSpy.isOffline());
    assertFalse(testConfigurableUpdateSpy.isRolling());
    verify(testConfigurableUpdateSpy).enableOffline();
    assertBuildLogDoesNotContainExceptions();
    assertBuildLogDoesNotContainErrors();
  }

  @Test
  public void testZipDeployOrUpdate() throws IOException {
    CommandCenterFactory commandCenterFactory = Mockito.mock(CommandCenterFactory.class);
    CommandCenter commandCenter = mockCC(commandCenterFactory);
    TestConfigurableDeployImpl testConfigurableDeploySpy = spy(new TestConfigurableDeployImpl());
    Mockito.when(commandCenter.deploy(anyString(), anyString())).thenReturn(testConfigurableDeploySpy);
    PluginUtil pluginUtilSpy = spyPluginUtil(commandCenterFactory);

    PluginConf conf = new PluginConf(PluginConf.Action.DEPLOY_OR_UPDATE);
    File appZip = new File(FileUtils.getTempDirectory(), "lr-demo-ver1.zip");
    FileUtils.copyFile(new File(archivesDir, "lr-demo-ver1.war"), appZip);
    conf.deployable = appZip;
    try {
      conf.contextPath = "/var/www/testOffline";
      conf.updateStrategies = new TestUpdateStrategiesImpl(UpdateMode.LIVEREBEL_DEFAULT, null, 30, false, 3600);
      conf.serverIds = Lists.newArrayList("fileserver");

      assertEquals(PluginUtil.PluginActionResult.SUCCESS, pluginUtilSpy.perform(conf));
      assertBuildLogDoesNotContainExceptions();
      assertBuildLogDoesNotContainErrors();
    }
    finally {
      FileUtils.deleteQuietly(appZip);
    }
  }

  @Test
  public void testZipDeployWithoutLiverebelXml() throws IOException {
    CommandCenterFactory commandCenterFactory = Mockito.mock(CommandCenterFactory.class);
    CommandCenter commandCenter = mockCC(commandCenterFactory);
    TestConfigurableDeployImpl testConfigurableDeploySpy = spy(new TestConfigurableDeployImpl());
    Mockito.when(commandCenter.deploy(anyString(), anyString())).thenReturn(testConfigurableDeploySpy);
    PluginUtil pluginUtilSpy = spyPluginUtil(commandCenterFactory);

    PluginConf conf = new PluginConf(PluginConf.Action.DEPLOY_OR_UPDATE);
    File appZipWithoutLiverebelXml = new File(FileUtils.getTempDirectory(), "lr-demo-ver1-without-liverebel-xml.zip");
    ZipUtil.removeEntry(new File(archivesDir, "lr-demo-ver1.war"), "WEB-INF/classes/liverebel.xml", appZipWithoutLiverebelXml);
    conf.deployable = appZipWithoutLiverebelXml;
    try {
      conf.contextPath = "/var/www/testOffline";
      conf.updateStrategies = new TestUpdateStrategiesImpl(UpdateMode.LIVEREBEL_DEFAULT, null, 30, false, 3600);
      conf.serverIds = Lists.newArrayList("fileserver");

      assertEquals(PluginUtil.PluginActionResult.ERROR, pluginUtilSpy.perform(conf));
      assertBuildLogDoesNotContainExceptions();
    }
    finally {
      FileUtils.deleteQuietly(appZipWithoutLiverebelXml);
    }
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

  private UploadInfo createDummySchemaUploadInfo() {
    return new UploadInfo() {
      public String getApplicationId() {
        return "testapp";
      }

      public String getVersionId() {
        return "1.0";
      }
    };
  }

  private UploadInfo createDummyNotExistingUploadInfo() {
    return new UploadInfo() {
      public String getApplicationId() {
        return "test-db-app-not-exists";
      }

      public String getVersionId() {
        return "1.0";
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
        return null;
      }

      public List<LocalInfo> getLocalInfos() {
        return null;
      }

      public Map<String, LocalInfo> getLocalInfosMap() {
        HashMap<String, LocalInfo> map = Maps.newHashMap();
        map.put("dummy", new TestLocalInfoImpl("dummy", "ver0"));
        map.put("dummy2", new TestLocalInfoImpl("dummy", "ver0"));
        return map;
      }

      public Set<String> getUrls() {
        return null;
      }

      public VersionInfo getLatestVersion() {
        return null;
      }
    };
  }

  private ApplicationInfo createDummySchemaApplicationInfo(final String id) {
    return new ApplicationInfo() {
      public String getId() {
        return id;
      }

      public Set<String> getVersions() {
        return Sets.newHashSet("1.0");
      }

      public Map<String, VersionInfo> getVersionsMap() {
        return null;
      }

      public Set<String> getActiveVersions() {
        return null;
      }

      public Map<String, String> getActiveVersionPerServer() {
        return null;
      }

      public List<LocalInfo> getLocalInfos() {
        return null;
      }

      public Map<String, LocalInfo> getLocalInfosMap() {
        HashMap<String, LocalInfo> map = Maps.newHashMap();
        map.put("dummy", new TestLocalInfoImpl("dummy", "ver0"));
        return map;
      }

      public Set<String> getUrls() {
        return null;
      }

      public VersionInfo getLatestVersion() {
        return null;
      }
    };
  }

  @Test
  public void testSchemaUpdate() {
    CommandCenterFactory commandCenterFactory = Mockito.mock(CommandCenterFactory.class);
    CommandCenter commandCenter = mockCC(commandCenterFactory);
    TestConfigurableUpdateImpl testConfigurableUpdateSpy = spyConfigurableUpdate(commandCenter);
    PluginUtil pluginUtilSpy = spyPluginUtil(commandCenterFactory);

    PluginConf conf = new PluginConf(PluginConf.Action.DEPLOY_OR_UPDATE);
    conf.deployable = new File(archivesDir, "test-db-app-ver1.war");
    conf.contextPath = "testSchemaUpdate";
    conf.updateStrategies = new TestUpdateStrategiesImpl(UpdateMode.LIVEREBEL_DEFAULT, UpdateMode.LIVEREBEL_DEFAULT, 30, true, 30);
    conf.serverIds = Lists.newArrayList("dummy");

    conf.hasDatabaseMigrations = true;
    conf.schemaId = Long.toString(1);

    assertEquals(PluginUtil.PluginActionResult.SUCCESS, pluginUtilSpy.perform(conf));
    assertEquals("HOTPATCH", testConfigurableUpdateSpy.updateMode);
    assertEquals(1L, testConfigurableUpdateSpy.getSchemaIds().toArray()[0]);
    assertFalse(testConfigurableUpdateSpy.isOffline());
    assertFalse(testConfigurableUpdateSpy.isRolling());
    verify(testConfigurableUpdateSpy).enableAutoStrategy(true);
    assertBuildLogDoesNotContainExceptions();
    assertBuildLogDoesNotContainErrors();
  }

  @Test
  public void testSchemaUpdateFailing() {
    CommandCenterFactory commandCenterFactory = Mockito.mock(CommandCenterFactory.class);
    CommandCenter commandCenter = mockCC(commandCenterFactory);
    TestConfigurableUpdateImpl testConfigurableUpdateSpy = spyConfigurableUpdate(commandCenter);
    PluginUtil pluginUtilSpy = spyPluginUtil(commandCenterFactory);

    PluginConf conf = new PluginConf(PluginConf.Action.DEPLOY_OR_UPDATE);
    conf.deployable = new File(archivesDir, "test-db-app-ver1.war");
    conf.contextPath = "testSchemaUpdate";
    conf.updateStrategies = new TestUpdateStrategiesImpl(UpdateMode.LIVEREBEL_DEFAULT, UpdateMode.LIVEREBEL_DEFAULT, 30, true, 30);
    conf.serverIds = Lists.newArrayList("dummy");

    conf.hasDatabaseMigrations = true;
    // no schema selected, must fail
    assertEquals(PluginUtil.PluginActionResult.ERROR, pluginUtilSpy.perform(conf));
    assertBuildLogDoesNotContainExceptions();
  }

  @Test
  public void testStaticContentDeploy() {
    CommandCenterFactory commandCenterFactory = Mockito.mock(CommandCenterFactory.class);
    CommandCenter commandCenter = mockCC(commandCenterFactory);
    TestConfigurableDeployImpl testConfigurableDeploySpy = spyConfigurableDeploy(commandCenter);

    PluginUtil pluginUtilSpy = spyPluginUtil(commandCenterFactory);

    final String FILE_PATH = "c:\\temp\\testStaticContentDeploy";

    PluginConf conf = new PluginConf(PluginConf.Action.DEPLOY_OR_UPDATE);
    conf.deployable = new File(archivesDir, "test-db-app-not-exists.war");

    conf.contextPath = "testStaticContentDeploy";
    conf.updateStrategies = new TestUpdateStrategiesImpl(UpdateMode.LIVEREBEL_DEFAULT, UpdateMode.LIVEREBEL_DEFAULT, 30, true, 30);
    conf.serverIds = Lists.newArrayList("dummy");

    conf.hasStaticContent = true;
    conf.staticServerIds = new ArrayList<String>();
    conf.staticServerIds.add("fileserver");
    conf.filePath = FILE_PATH;

    assertEquals(PluginUtil.PluginActionResult.SUCCESS, pluginUtilSpy.perform(conf));
    assertEquals("HOTPATCH", testConfigurableDeploySpy.updateMode);
    assertEquals(FILE_PATH, testConfigurableDeploySpy.getFileDeploymentPath());
    assertFalse(testConfigurableDeploySpy.isOffline());
    assertFalse(testConfigurableDeploySpy.isRolling());
    assertBuildLogDoesNotContainExceptions();
    assertBuildLogDoesNotContainErrors();
  }

  @Test
  public void testStaticContentDeployFailsNoFilePath() {
    CommandCenterFactory commandCenterFactory = Mockito.mock(CommandCenterFactory.class);
    CommandCenter commandCenter = mockCC(commandCenterFactory);
    TestConfigurableDeployImpl testConfigurableDeploySpy = spyConfigurableDeploy(commandCenter);
    PluginUtil pluginUtilSpy = spyPluginUtil(commandCenterFactory);

    final String FILE_PATH = "c:\\temp\\testStaticContentDeploy";

    PluginConf conf = new PluginConf(PluginConf.Action.DEPLOY_OR_UPDATE);
    conf.deployable = new File(archivesDir, "test-db-app-not-exists.war");

    conf.contextPath = "testStaticContentDeploy";
    conf.updateStrategies = new TestUpdateStrategiesImpl(UpdateMode.LIVEREBEL_DEFAULT, UpdateMode.LIVEREBEL_DEFAULT, 30, true, 30);
    conf.serverIds = Lists.newArrayList("dummy");

    conf.hasStaticContent = true;
    conf.staticServerIds = new ArrayList<String>();
    conf.staticServerIds.add("fileserver");

    assertEquals(PluginUtil.PluginActionResult.ERROR, pluginUtilSpy.perform(conf));
    assertBuildLogDoesNotContainExceptions();
  }

  @Test
  public void testStaticContentDeployFailsNoServers() {
    CommandCenterFactory commandCenterFactory = Mockito.mock(CommandCenterFactory.class);
    CommandCenter commandCenter = mockCC(commandCenterFactory);
    TestConfigurableDeployImpl testConfigurableDeploySpy = spyConfigurableDeploy(commandCenter);
    PluginUtil pluginUtilSpy = spyPluginUtil(commandCenterFactory);

    final String FILE_PATH = "c:\\temp\\testStaticContentDeploy";

    PluginConf conf = new PluginConf(PluginConf.Action.DEPLOY_OR_UPDATE);
    conf.deployable = new File(archivesDir, "test-db-app-not-exists.war");

    conf.contextPath = "testStaticContentDeploy";
    conf.updateStrategies = new TestUpdateStrategiesImpl(UpdateMode.LIVEREBEL_DEFAULT, UpdateMode.LIVEREBEL_DEFAULT, 30, true, 30);
    conf.serverIds = Lists.newArrayList("dummy");

    conf.hasStaticContent = true;
    conf.filePath = FILE_PATH;

    assertEquals(PluginUtil.PluginActionResult.ERROR, pluginUtilSpy.perform(conf));
    assertBuildLogDoesNotContainExceptions();
  }

  @Test
  public void testFilePathSetFromContextPathWhenNonJavaApp() {
    CommandCenterFactory commandCenterFactory = Mockito.mock(CommandCenterFactory.class);
    CommandCenter commandCenter = mockCC(commandCenterFactory);
    TestConfigurableDeployImpl testConfigurableDeploySpy = spyConfigurableDeploy(commandCenter);
    PluginUtil pluginUtilSpy = spyPluginUtil(commandCenterFactory);

    final String FILE_PATH = "c:\\temp\\testStaticContentDeploy";

    PluginConf conf = new PluginConf(PluginConf.Action.DEPLOY_OR_UPDATE);
    conf.deployable = new File(archivesDir, "test-db-app-not-exists.war");

    conf.contextPath = FILE_PATH;
    conf.updateStrategies = new TestUpdateStrategiesImpl(UpdateMode.LIVEREBEL_DEFAULT, UpdateMode.LIVEREBEL_DEFAULT, 30, true, 30);
    conf.serverIds = Lists.newArrayList("fileserver");

    assertEquals(PluginUtil.PluginActionResult.SUCCESS, pluginUtilSpy.perform(conf));
    assertEquals(FILE_PATH, testConfigurableDeploySpy.getFileDeploymentPath());
    assertEquals(FILE_PATH, conf.filePath);
    assertBuildLogDoesNotContainExceptions();
    assertBuildLogDoesNotContainErrors();
  }

}
