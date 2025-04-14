package org.jlab.jam.persistence.enumeration;

public enum OperationsType {
  RF("RF"),
  BEAM("Beam");

  final String label;

  OperationsType(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
