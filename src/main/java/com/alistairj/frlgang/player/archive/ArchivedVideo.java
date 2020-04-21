package com.alistairj.frlgang.player.archive;

/**
 * @author Alistair Jones (alistair@ohalo.co)
 */
public class ArchivedVideo {

  private String id;

  private long duration;

  public ArchivedVideo(String id, long duration) {
    this.id = id;
    this.duration = duration;
  }

  public String getId() {
    return id;
  }

  public long getDuration() {
    return duration;
  }
}
