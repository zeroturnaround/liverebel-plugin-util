package org.zeroturnaround.liverebel.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zeroturnaround.liverebel.api.CommandCenter;
import com.zeroturnaround.liverebel.api.SchemaInfo;
import com.zeroturnaround.liverebel.api.ServerGroup;
import com.zeroturnaround.liverebel.api.ServerGroupOperations;
import com.zeroturnaround.liverebel.api.ServerInfo;
import com.zeroturnaround.liverebel.util.ServerKind;

public class ServersUtil {

  private List<Server> servers;
  private CommandCenter commandCenter;

  public ServersUtil(CommandCenter commandCenter, List<Server> servers) {
    this.servers = servers;
    this.commandCenter = commandCenter;
  }

  public List<Server> getServers() {
    if (servers == null) {
      return getDefaultServers();
    }
    else {
      List<Server> newServers = getDefaultServers();
      Map<String, Server> oldServersMap = new HashMap<String, Server>();

      for (Server oldServer : servers) {
        oldServersMap.put(oldServer.getId(), oldServer);
      }

      servers.clear();
      for(Server newServer : newServers) {
        if (oldServersMap.containsKey(newServer.getId()))
          servers.add(new ServerImpl(newServer.getId(), newServer.getTitle(), newServer.getParentNames(), newServer.getIndentDepth(),
              oldServersMap.get(newServer.getId()).isChecked(), newServer.isConnected(), newServer.isGroup(), newServer.getType(),
              newServer.isVirtualHostsSupported(), newServer.getDefaultVirtualHostName(), newServer.getVirtualHostNames()));
        else
          servers.add(newServer);
      }
      return servers;
    }
  }

  public List<Server> getDatabaseServers() {
    return getServers(ServerKind.DATABASE);
  }

  public List<Server> getDefaultServers() {
    Set<ServerKind> kinds = new HashSet<ServerKind>(Arrays.asList(ServerKind.values()));
    //we do not want database servers in the list of servers where we deploy apps to
    kinds.remove(ServerKind.DATABASE);
    return getServers(kinds);
  }

  private List<Server> getServers(ServerKind... kinds) {
    return getServers(new HashSet<ServerKind>(Arrays.asList(kinds)));
  }
  private List<Server> getServers(Set<ServerKind> kinds) {
    List<Server> serversLoc = new ArrayList<Server>();
    if (commandCenter != null) {
      String currentVersion = commandCenter.getVersion();
      if (isServerGroupsSupported(currentVersion)) {
        serversLoc = showServerGroups(kinds);
      }
      else {
        serversLoc = showServers(commandCenter);
      }
    }
    return serversLoc;
  }

  public boolean isServerGroupsSupported(String currentVersion) {return !currentVersion.startsWith("2.0");}

  public List<Server> showServerGroups(Set<ServerKind> kinds) {
    ServerGroupOperations sgo = commandCenter.serverGroupOperations();
    List<ServerGroup> topLevelServerGroups = sgo.getAllGroups();
    List<Server> allCheckBoxes = new ArrayList<Server>();
    for (ServerGroup serverGroup : topLevelServerGroups) {
      if (hasServers(serverGroup, kinds)) //do not add empty groups
        allCheckBoxes.addAll(processSiblings(serverGroup, "", 0, kinds));
    }

    return allCheckBoxes;
  }

  public boolean hasServers(ServerGroup serverGroup, Set<ServerKind> kinds) {
    if (!serverGroup.getServers().isEmpty()) {
      for (ServerInfo server : serverGroup.getServers()) {
        if (kinds.contains(server.getType()))
          return true;
      }
    }
    for (ServerGroup child : serverGroup.getChildren()) {
      if (hasServers(child, kinds)) return true;
    }
    return false;
  }

  public List<Server> processSiblings(ServerGroup serverGroup, String parentNames, int indentDepth, Set<ServerKind> kinds) {
    Server serverCheckbox = new ServerImpl(serverGroup.getName(), serverGroup.getName(), parentNames, indentDepth, false, false, true, null, false, null, null);
    ArrayList<Server> serverCheckboxes = new ArrayList<Server>();
    serverCheckboxes.add(serverCheckbox);
    if (serverGroup.getChildren().size() != 0) {
      for (ServerGroup child : serverGroup.getChildren()) {
        if (hasServers(child, kinds)) //do not add empty groups
          serverCheckboxes.addAll(processSiblings(child, "lr-" + serverGroup.getName().replaceAll("[^A-Za-z0-9]", "_"), indentDepth + 1, kinds));
      }
    }

    if (serverGroup.getServers().size() != 0) {
      for (ServerInfo server : serverGroup.getServers()) {
        if (kinds.contains(server.getType()))
          serverCheckboxes.add(new ServerImpl(server.getId(), server.getName(), "lr-" + serverGroup.getName().replaceAll("[^A-Za-z0-9]", "_"), indentDepth + 1, false,
              server.isConnected(), false, server.getType(), server.isVirtualHostsSupported(), server.getDefaultVirtualHostName(), server.getVirtualHostNames()));
      }
    }

    return serverCheckboxes;
  }

  public List<Server> showServers(CommandCenter commandCenter) {
    List<Server> servers = new ArrayList<Server>();
    for (ServerInfo server : commandCenter.getServers().values()) {
      servers.add(new ServerImpl(server.getId(), server.getName(), "", 0, false, server.isConnected(), false,
          server.getType(), server.isVirtualHostsSupported(), server.getDefaultVirtualHostName(), server.getVirtualHostNames()));
    }
    return servers;
  }

  public List<SchemaInfo> getSchemas() {
    if (commandCenter == null)
      return null;
    Map<Long, SchemaInfo> schemaMap = commandCenter.getAllDatabaseSchemas();
    List<SchemaInfo> schemas = new ArrayList<SchemaInfo>();
    for (SchemaInfo schemaInfo : schemaMap.values()) {
      schemas.add(schemaInfo);
    }

    return schemas;
  }

}