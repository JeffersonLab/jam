package org.jlab.jam.persistence.view;

import java.math.BigInteger;
import java.util.List;

/**
 * @author ryans
 */
public interface Node {
  String getName();

  BigInteger getId();

  List<? extends Node> getChildren();
}
