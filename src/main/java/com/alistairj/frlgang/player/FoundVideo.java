package com.alistairj.frlgang.player;

import com.google.api.services.youtube.model.Video;

/**
 * A found video is either one that is returned after a API look up, and either the LivePlayer
 * already new about it or not.
 *
 * @author Alistair Jones (alistair@ohalo.co)
 */
public class FoundVideo {

  private boolean isNew;

  private Video video;

  public FoundVideo(Video video, boolean isNew) {
    this.video = video;
    this.isNew = isNew;
  }

  public boolean isNew() {
    return isNew;
  }

  public Video getVideo() {
    return video;
  }
}
