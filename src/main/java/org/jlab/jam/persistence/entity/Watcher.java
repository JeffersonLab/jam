package org.jlab.jam.persistence.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * @author ryans
 */
@Entity
@Table(name = "WATCHER", schema = "JAM_OWNER")
public class Watcher implements Serializable {

  private static final long serialVersionUID = 1L;
  @EmbeddedId protected WatcherPK watcherPK;

  public WatcherPK getWatcherPK() {
    return watcherPK;
  }

  public void setWatcherPK(WatcherPK watcherPK) {
    this.watcherPK = watcherPK;
  }
}
