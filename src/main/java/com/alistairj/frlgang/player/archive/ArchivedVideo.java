package com.alistairj.frlgang.player.archive;

import com.google.api.services.youtube.model.Thumbnail;

/**
 * Video object that also includes duration in seconds.
 *
 * @author Alistair Jones (alistair@ohalo.co)
 */
public class ArchivedVideo {

  private final String id;

  private final String title;

  private final long durationInSeconds;

  private final Thumbnail thumbnail;

  public ArchivedVideo(String id, String title, long durationInSeconds, Thumbnail thumbnail) {
    this.id = id;
    this.title = title;
    this.durationInSeconds = durationInSeconds;
    this.thumbnail = thumbnail;
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

  public Thumbnail getThumbnail() {
    return thumbnail;
  }
}
