package com.alistairj.frlgang.player;

import com.alistairj.frlgang.player.archive.ArchivePlayer;
import com.google.api.services.youtube.model.Video;

/**
 * Radio player consists of an archive player and a live player. It controls the two and switches
 * between them.
 *
 * @author Alistair Jones (alistair@ohalo.co)
 */
public class RadioPlayer {

  private ArchivePlayer archivePlayer;

  private LivePlayer livePlayer;

  private BroadcastStatus status = BroadcastStatus.ARCHIVE;

  public RadioPlayer() {
    archivePlayer = new ArchivePlayer();
    livePlayer = new LivePlayer(this);
  }

  public synchronized BroadcastStatus getStatus() {
    return status;
  }

  synchronized void setStatusLive(String liveVideo) {
    status = BroadcastStatus.LIVE;
    archivePlayer.stop();
  }

  synchronized void setStatusUpcoming(Video upcomingVideo) {
    status = BroadcastStatus.UPCOMING;
    archivePlayer.stop();
  }

  synchronized void setStatusArchivedPlay() {
    status = BroadcastStatus.ARCHIVE;
    archivePlayer.play();
  }
}
