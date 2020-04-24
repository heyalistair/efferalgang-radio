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

  private RadioPlayerUtils() {
    // private constructor
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
      ZonedDateTime tenMinutesAgo = now.minusMinutes(10);
      ZonedDateTime inTenMinutes = now.plusMinutes(10);

      if (scheduled.isBefore(inTenMinutes) && scheduled.isAfter(tenMinutesAgo)) {
        return upcomingList.get(0);
      }
    }

    return null;
  }

  private static ZonedDateTime getDateTime(DateTime dt) {
    Instant instant = Instant.ofEpochSecond(dt.getValue() / 1000);
    return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
  }
}
