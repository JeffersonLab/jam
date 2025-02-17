package org.jlab.jam.persistence.enumeration;

public enum OperationsType {
  RF("RF"),
  BEAM("Beam");

  String label;

  OperationsType(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
