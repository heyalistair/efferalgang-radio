package com.alistairj.frlgang.player;

import com.alistairj.frlgang.player.archive.ArchivePlayer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Radio player consists of an archive player and a live player. It controls the two and switches
 * between them.
 *
 * @author Alistair Jones (alistair@ohalo.co)
 */
@JsonSerialize(using = RadioPlayerSerializer.class)
public class RadioPlayer {

  private final ArchivePlayer archivePlayer;

  private final LivePlayer livePlayer;

  private BroadcastStatus status = BroadcastStatus.ARCHIVE;

  /**
   * Constructor for RadioPlayer.
   *
   * <p>
   * Build a new ArchivePlayer and LivePlayer and initialize to true.
   * </p>
   */
  public RadioPlayer() {
    archivePlayer = new ArchivePlayer();
    archivePlayer.play();
    livePlayer = new LivePlayer(this);
  }

  public synchronized BroadcastStatus getStatus() {
    return status;
  }

  synchronized void setStatusLive() {
    status = BroadcastStatus.LIVE;
    archivePlayer.stop();
  }

  synchronized void setStatusUpcoming() {
    status = BroadcastStatus.UPCOMING;
    archivePlayer.stop();
  }

  synchronized void setStatusArchivedPlay() {
    status = BroadcastStatus.ARCHIVE;
    archivePlayer.play();
  }

  public ArchivePlayer getArchivePlayer() {
    return archivePlayer;
  }

  public LivePlayer getLivePlayer() {
    return livePlayer;
  }
}
