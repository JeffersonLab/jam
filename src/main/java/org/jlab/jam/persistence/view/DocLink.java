package org.jlab.jam.persistence.view;

public class DocLink {
  private String label;
  private String url;

  public DocLink(String label, String url) {
    this.label = label;
    this.url = url;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
