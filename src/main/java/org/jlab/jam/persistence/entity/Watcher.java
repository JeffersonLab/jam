package org.jlab.jam.persistence.entity;

import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

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
