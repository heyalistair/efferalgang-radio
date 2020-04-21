package com.alistairj.frlgang.player.archive;

import com.alistairj.frlgang.YouTubeService;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alistair Jones (alistair@ohalo.co)
 */
public class ArchivePlayer {

  private static final Logger logger = LoggerFactory.getLogger(ArchivePlayer.class);

  private ArchivedVideo current = null;

  private long currentPlayheadInSeconds = 0;

  private boolean isPlaying = false;

  private Timer playCounter = new Timer("Playhead");

  private ConcurrentLinkedQueue<ArchivedVideo> queue = new ConcurrentLinkedQueue<>();

  private void rebuildQueue() throws GeneralSecurityException, IOException {
    // get queue and duration
    logger.debug("REBUILDING QUEUE");

    queue.addAll(YouTubeService.getCompletedShows());
  }

  public synchronized void play() {

    if (isPlaying == false) {
      playCounter.scheduleAtFixedRate(task, 0, 1000);
    }

    isPlaying = true;
  }

  public synchronized void stop() {
    if (isPlaying) {
      playCounter.cancel();
    }
    isPlaying = false;
  }

  private TimerTask task = new TimerTask() {
    @Override
    public void run() {
      // step playhead forward 1 second
      currentPlayheadInSeconds++;

      // check if video is over
      if (currentPlayheadInSeconds > current.getDuration()) {
        ArchivedVideo video = queue.poll();
        if (video == null) {
          try {
            rebuildQueue();
          } catch (GeneralSecurityException | IOException e) {
            logger.error("Cannot rebuild queue");
          }

          video = queue.poll();
          if (video == null) {
            logger.error("We can't get any archived videos");
          }
        }

        // if so start a new video
        current = video;
        currentPlayheadInSeconds = 0;
      }
    }
  };
}
