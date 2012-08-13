package org.zeroturnaround.liverebel.plugins;

public interface Server {

  public String getTitle();

  /**
   * @param title the title to set
   */
  public void setTitle(String title);

  /**
   * @return the id
   */
  public String getId();

  /**
   * @param id the id to set
   */
  public void setId(String id);

  /**
   * @return the connected
   */
  public boolean isConnected();

  /**
   * @param connected the connected to set
   */
  public void setConnected(boolean connected);

  /**
   * @return the checked
   */
  public boolean isChecked();

  /**
   * @param checked the checked to set
   */
  public void setChecked(boolean checked);

  public String getIndentDepthAsCSSClass();

  public boolean isGroup();

  public String getParentNames();


  public int getIndentDepth();
}
