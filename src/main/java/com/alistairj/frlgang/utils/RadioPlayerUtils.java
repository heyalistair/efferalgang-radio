package com.alistairj.frlgang.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.Video;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

  public static void writeVideoInfo(JsonGenerator g, Video v) throws IOException {
    g.writeStringField("id", v.getId());
    g.writeStringField("title", v.getSnippet().getTitle());
    g.writeStringField("scheduled_at",
        v.getLiveStreamingDetails().getScheduledStartTime().toStringRfc3339());
    g.writeObjectFieldStart("thumbnail");
    g.writeStringField("url", v.getSnippet().getThumbnails().getStandard().getUrl());
    g.writeNumberField("w", v.getSnippet().getThumbnails().getStandard().getWidth());
    g.writeNumberField("h", v.getSnippet().getThumbnails().getStandard().getHeight());
    g.writeEndObject();
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

  public static String printDurationInHours(long seconds) {
    return String.format("%.2f", (seconds / 3600f));
  }

  public static String YOUTUBE_URL_PATTERN = "(?<=watch\\?v=|/videos/|/video/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
  public static Pattern COMPILED_PATTERN = Pattern.compile(YOUTUBE_URL_PATTERN);

  /**
   * Returns the video id part of a youtube url.
   *
   * <p>
   * It is based on
   *
   * Should handle:
   * http://www.youtube.com/watch?v=dQw4w9WgXcQ&a=GxdCwVVULXctT2lYDEPllDR0LRTutYfW
   * http://www.youtube.com/watch?v=dQw4w9WgXcQ
   * http://youtu.be/dQw4w9WgXcQ
   * http://www.youtube.com/embed/dQw4w9WgXcQ
   * http://www.youtube.com/v/dQw4w9WgXcQ
   * http://www.youtube.com/e/dQw4w9WgXcQ
   * http://www.youtube.com/watch?v=dQw4w9WgXcQ
   * http://www.youtube.com/watch?feature=player_embedded&v=dQw4w9WgXcQ
   * http://www.youtube-nocookie.com/v/6L3ZvIMwZFM?version=3&hl=en_US&rel=0
   * https://studio.youtube.com/video/WNMfvKiYtiw/livestreaming
   * </p>
   *
   * @param urlOrId
   * @return String
   */
  public static String parseVideoId(String urlOrId) {

    if (urlOrId == null) {
      return "";
    }

    urlOrId = urlOrId.trim();

    // example of an id is "qAvSXHxE_SM"
    if (urlOrId.length() == 11) {
      return urlOrId;
    }

    Matcher matcher = COMPILED_PATTERN.matcher(urlOrId); //url is youtube url for which you want to extract the id.
    if (matcher.find()) {
      return matcher.group().substring(0,11);
    }

    return "";
  }
}
