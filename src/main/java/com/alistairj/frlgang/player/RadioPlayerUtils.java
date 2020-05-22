package com.alistairj.frlgang.player;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.Video;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Radio play general utils.
 *
 * @author Alistair Jones (alistair@ohalo.co)
 */
public class RadioPlayerUtils {

  private static final Logger logger = LoggerFactory.getLogger(RadioPlayerUtils.class);

  private static final int UPCOMING_WAIT_START_BEFORE_MINUTES = 5;

  private static final int UPCOMING_WAIT_TIME_END_AFTER_MINUTES = 10;

  private RadioPlayerUtils() {
    // private constructor
  }

  /**
   * Print some nice log.
   *
   * @param upcomers The upcoming videos to print.
   */
  public static void printUpcomingShows(List<Video> upcomers) {
    Video upcoming;
    if (upcomers.size() > 0) {
      upcoming = upcomers.get(0);
      logger.debug("1st upcoming video scheduled at:{}, id:{}, name:{}",
          upcoming.getLiveStreamingDetails().getScheduledStartTime().toStringRfc3339(),
          upcoming.getId(), upcoming.getSnippet().getTitle());
    }

    if (upcomers.size() > 1) {
      upcoming = upcomers.get(1);
      logger.debug("2nd upcoming video scheduled at:{}, id:{}, name:{}",
          upcoming.getLiveStreamingDetails().getScheduledStartTime().toStringRfc3339(),
          upcoming.getId(), upcoming.getSnippet().getTitle());
    }

    if (upcomers.size() > 2) {
      upcoming = upcomers.get(2);
      logger.debug("3rd upcoming video scheduled at:{}, id:{}, name:{}",
          upcoming.getLiveStreamingDetails().getScheduledStartTime().toStringRfc3339(),
          upcoming.getId(), upcoming.getSnippet().getTitle());
    }
  }

  /**
   * Examines the scheduled times in the list to see if there is a video that is starting
   * imminently.
   *
   * @param upcomingList The list of videos sorted by scheduled time.
   * @return video Upcoming video, or null if there is none.
   */
  public static Video isUpcomingVideoPending(List<Video> upcomingList) {
    if (upcomingList == null) {
      return null;
    }

    if (upcomingList.size() > 0) {

      DateTime dt = upcomingList.get(0).getLiveStreamingDetails().getScheduledStartTime();
      ZonedDateTime scheduled = getDateTime(dt);
      ZonedDateTime now = ZonedDateTime.now();
      ZonedDateTime fiveMinutesAgo = now.minusMinutes(UPCOMING_WAIT_START_BEFORE_MINUTES);
      ZonedDateTime inTenMinutes = now.plusMinutes(UPCOMING_WAIT_TIME_END_AFTER_MINUTES);

      if (scheduled.isBefore(inTenMinutes) && scheduled.isAfter(fiveMinutesAgo)) {
        return upcomingList.get(0);
      }
    }

    return null;
  }

  public static boolean isFirstVideoScheduledAfterSecond(Video v1, Video v2) {

    ZonedDateTime d1 = getDateTime(v1.getLiveStreamingDetails().getScheduledStartTime());
    ZonedDateTime d2 = getDateTime(v2.getLiveStreamingDetails().getScheduledStartTime());

    return d1.isAfter(d2);
  }

  private static ZonedDateTime getDateTime(DateTime dt) {
    Instant instant = Instant.ofEpochSecond(dt.getValue() / 1000);
    return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
  }
}
