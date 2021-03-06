package com.alistairj.frlgang.player.archive;

import com.alistairj.frlgang.YouTubeService;
import com.alistairj.frlgang.player.ArchivePlayerSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
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
@JsonSerialize(using = ArchivePlayerSerializer.class)
public class ArchivePlayer {

  private static final Logger logger = LoggerFactory.getLogger(ArchivePlayer.class);

  private ArchivedVideo currentVideo = null;

  private long currentPlayheadInSeconds = 0;

  private boolean isPlaying = false;

  private Timer playCounter;

  private final ConcurrentLinkedQueue<ArchivedVideo> queue = new ConcurrentLinkedQueue<>();

  private final ReentrantLock lock = new ReentrantLock();

  private TimerTask getNewTimerTask() {
    return new TimerTask() {
      @Override
      public void run() {
        // step playhead forward 1 second
        currentPlayheadInSeconds++;

        // check if video is over
        if (currentVideo == null
            || currentPlayheadInSeconds > currentVideo.getDurationInSeconds()) {

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
  }

  public ArchivePlayer() {
    rebuildQueue();
  }

  private void rebuildQueue() {

    if (lock.tryLock()) {
      try {
        logger.debug("Rebuilding video archive queue...");
        queue.addAll(YouTubeService.getCompletedShows());
        logger.debug("Rebuilding video archive queue complete. (Video count: {})", queue.size());
      } catch (IOException e) {
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
      if (playCounter == null) {
        playCounter = new Timer("playhead-timer");
        playCounter.scheduleAtFixedRate(getNewTimerTask(), 0, 1000);
      }
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
      playCounter = null;
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

  public void generateQueueJson(JsonGenerator g) throws IOException {
    g.writeArrayFieldStart("archive_queue");

    for (ArchivedVideo v : queue) {
      g.writeStartObject();
      g.writeStringField("id", v.getId());
      g.writeStringField("title", v.getTitle());
      g.writeEndObject();
    }

    g.writeEndArray();
  }
}
