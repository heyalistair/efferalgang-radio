package com.alistairj.frlgang.player.archive;

/**
 * Video object that also includes duration in seconds.
 *
 * @author Alistair Jones (alistair@ohalo.co)
 */
public class ArchivedVideo {

  private String id;

  private String title;

  private long durationInSeconds;

  public ArchivedVideo(String id, String title, long durationInSeconds) {
    this.id = id;
    this.title = title;
    this.durationInSeconds = durationInSeconds;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public long getDurationInSeconds() {
    return durationInSeconds;
  }
}
