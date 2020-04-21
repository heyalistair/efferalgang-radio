package com.alistairj.frlgang.player.archive;

import com.alistairj.frlgang.YouTubeService;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Archive player manages a queue of archived videos and tracks a playhead so that all web
 * players will start at the same time.
 *
 * @author Alistair Jones (alistair@ohalo.co)
 */
public class ArchivePlayer {

  private static final Logger logger = LoggerFactory.getLogger(ArchivePlayer.class);

  private ArchivedVideo currentVideo = null;

  private long currentPlayheadInSeconds = 0;

  private boolean isPlaying = false;

  private Timer playCounter = new Timer("Playhead");

  private ConcurrentLinkedQueue<ArchivedVideo> queue = new ConcurrentLinkedQueue<>();

  private ReentrantLock lock = new ReentrantLock();

  private TimerTask task = new TimerTask() {
    @Override
    public void run() {
      // step playhead forward 1 second
      currentPlayheadInSeconds++;

      // check if video is over
      if (currentVideo == null || currentPlayheadInSeconds > currentVideo.getDurationInSeconds()) {

        // if so start a new video from the queue
        ArchivedVideo video = queue.poll();

        if (video == null) {
          logger.error("ARCHIVE VIDEO QUEUE IS EMPTY");
        }

        if (queue.size() < 2) {
          rebuildQueue();
        }

        currentVideo = video;
        currentPlayheadInSeconds = 0;
      }
    }
  };

  public ArchivePlayer() {
    rebuildQueue();
  }

  private void rebuildQueue() {

    if (lock.tryLock()) {
      try {
        logger.debug("Rebuilding video archive queue...");
        queue.addAll(YouTubeService.getCompletedShows());
        logger.debug("Rebuilding video archive queue complete. (Video count: {})", queue.size());
      } catch (GeneralSecurityException | IOException e) {
        logger.error("Archived play is unavailable.", e);
      } finally {
        lock.unlock();
      }
    } else {
      logger.debug("Queue is already being rebuilt");
    }
  }

  /**
   * Start advancing the playhead of the archive player.
   *
   * <p>
   * If it's already playing it won't change anything to call it multiple times.
   * </p>
   */
  public synchronized void play() {

    if (isPlaying == false) {
      playCounter.scheduleAtFixedRate(task, 0, 1000);
    }

    isPlaying = true;
  }

  /**
   * Stop the playhead of the archive player.
   *
   * <p>
   * If it's already stopped it won't change anything to call it multiple times.
   * </p>
   */
  public synchronized void stop() {
    if (isPlaying) {
      playCounter.cancel();
    }
    isPlaying = false;
  }

  public ArchivedVideo getCurrentVideo() {
    return currentVideo;
  }

  public long getCurrentPlayheadInSeconds() {
    return currentPlayheadInSeconds;
  }

  public ArchivedVideo peekNextVideo() {
    return queue.peek();
  }
}
